package com.app.pixelcraft.data.update

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.app.pixelcraft.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/** A GitHub release that carries an installable APK. */
data class AppRelease(
    val version: String,
    val title: String,
    val notes: String,
    val htmlUrl: String,
    val apkUrl: String,
    val apkName: String,
    val apkSizeBytes: Long,
    val checksumsUrl: String?
)

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    data class Available(val release: AppRelease) : UpdateState
    data class Downloading(val release: AppRelease, val progress: Float) : UpdateState
    data class ReadyToInstall(val release: AppRelease, val apk: File) : UpdateState
    data class Failed(val message: String, val release: AppRelease? = null) : UpdateState
}

/**
 * In-app updater for builds distributed through GitHub Releases: checks the latest
 * release, downloads its `*-release.apk` with progress, verifies it against the
 * release's `.sha256` checksums file, and hands it to the system installer.
 *
 * Disabled for debug builds (different application id) and for installs that came
 * from Google Play, which must be updated through Play.
 */
object AppUpdater {
    private const val OWNER = "sanixmon"
    private const val REPO = "pixelcraft"
    private const val LATEST_API = "https://api.github.com/repos/$OWNER/$REPO/releases/latest"
    private const val PREFS = "app_updater"
    private const val PREF_SKIPPED = "skipped_version"
    private const val PREF_LAST_CHECK = "last_check"
    private const val AUTO_CHECK_INTERVAL_MS = 6L * 60 * 60 * 1000

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()
    private var job: Job? = null

    /** Whether this install is one the in-app updater may replace. */
    fun isSupported(context: Context): Boolean {
        if (BuildConfig.DEBUG) return false
        val installer = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(context.packageName)
            }
        }.getOrNull()
        return installer != "com.android.vending"
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /**
     * Checks GitHub for a newer release. Automatic checks ([manual] = false) are throttled
     * and stay quiet about versions the user chose to skip.
     */
    fun check(context: Context, manual: Boolean) {
        val app = context.applicationContext
        if (!isSupported(app)) return
        val current = _state.value
        if (current is UpdateState.Checking || current is UpdateState.Downloading) return
        if (!manual) {
            val last = prefs(app).getLong(PREF_LAST_CHECK, 0L)
            if (System.currentTimeMillis() - last < AUTO_CHECK_INTERVAL_MS) return
        }
        job = scope.launch {
            _state.value = UpdateState.Checking
            _state.value = runCatching { fetchLatest() }.fold(
                onSuccess = { release ->
                    prefs(app).edit().putLong(PREF_LAST_CHECK, System.currentTimeMillis()).apply()
                    val skipped = prefs(app).getString(PREF_SKIPPED, null)
                    when {
                        release == null || !isNewer(release.version, BuildConfig.VERSION_NAME) ->
                            if (manual) UpdateState.UpToDate else UpdateState.Idle
                        !manual && release.version == skipped -> UpdateState.Idle
                        else -> existingDownload(app, release)?.let { UpdateState.ReadyToInstall(release, it) }
                            ?: UpdateState.Available(release)
                    }
                },
                onFailure = { if (manual) UpdateState.Failed(it.message ?: "Couldn't reach GitHub") else UpdateState.Idle }
            )
        }
    }

    /** "Later": hide this version from automatic checks until a newer one ships. */
    fun skip(context: Context, release: AppRelease) {
        prefs(context).edit().putString(PREF_SKIPPED, release.version).apply()
        dismiss()
    }

    fun dismiss() {
        if (_state.value !is UpdateState.Downloading) _state.value = UpdateState.Idle
    }

    fun download(context: Context, release: AppRelease) {
        val app = context.applicationContext
        if (_state.value is UpdateState.Downloading) return
        job = scope.launch {
            _state.value = UpdateState.Downloading(release, 0f)
            _state.value = runCatching { downloadApk(app, release) }.fold(
                onSuccess = { UpdateState.ReadyToInstall(release, it) },
                onFailure = { UpdateState.Failed(it.message ?: "Download failed", release) }
            )
        }
    }

    /**
     * Opens the system installer. Returns false when the user first has to allow
     * "Install unknown apps" for PixelCraft; that settings page is opened instead.
     */
    fun install(context: Context, apk: File): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
            val settings = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { context.startActivity(settings) }
            return false
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apk)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }

    // --- Internals ---

    private fun open(url: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 12_000
            readTimeout = 20_000
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "PixelCraft-Android")
        }

    private suspend fun fetchLatest(): AppRelease? = withContext(Dispatchers.IO) {
        val conn = open(LATEST_API)
        try {
            if (conn.responseCode == HttpURLConnection.HTTP_NOT_FOUND) return@withContext null
            check(conn.responseCode == HttpURLConnection.HTTP_OK) { "GitHub responded ${conn.responseCode}" }
            val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            if (json.optBoolean("draft") || json.optBoolean("prerelease")) return@withContext null
            val assets = json.optJSONArray("assets") ?: return@withContext null
            var apk: JSONObject? = null
            var sums: JSONObject? = null
            for (i in 0 until assets.length()) {
                val a = assets.getJSONObject(i)
                val name = a.optString("name")
                when {
                    name.endsWith(".apk") && (apk == null || name.endsWith("-release.apk")) -> apk = a
                    name.endsWith(".sha256") -> sums = a
                }
            }
            apk ?: return@withContext null
            val tag = json.optString("tag_name")
            AppRelease(
                version = tag.removePrefix("v").removePrefix("V"),
                title = json.optString("name").ifBlank { tag },
                notes = json.optString("body"),
                htmlUrl = json.optString("html_url"),
                apkUrl = apk.getString("browser_download_url"),
                apkName = apk.getString("name"),
                apkSizeBytes = apk.optLong("size", -1L),
                checksumsUrl = sums?.optString("browser_download_url")
            )
        } finally {
            conn.disconnect()
        }
    }

    private fun updatesDir(context: Context) = File(context.cacheDir, "updates").apply { mkdirs() }

    private fun existingDownload(context: Context, release: AppRelease): File? =
        File(updatesDir(context), release.apkName).takeIf { it.exists() && it.length() == release.apkSizeBytes }

    private suspend fun downloadApk(context: Context, release: AppRelease): File = withContext(Dispatchers.IO) {
        val dir = updatesDir(context)
        // Old downloads are useless once a newer one starts.
        dir.listFiles()?.filter { it.name != release.apkName }?.forEach { it.delete() }
        val target = File(dir, release.apkName)
        val part = File(dir, release.apkName + ".part")
        val digest = MessageDigest.getInstance("SHA-256")

        val conn = open(release.apkUrl).apply { setRequestProperty("Accept", "application/octet-stream") }
        try {
            check(conn.responseCode == HttpURLConnection.HTTP_OK) { "Download failed (${conn.responseCode})" }
            val total = conn.contentLengthLong.takeIf { it > 0 } ?: release.apkSizeBytes
            conn.inputStream.use { input ->
                part.outputStream().use { output ->
                    val buf = ByteArray(64 * 1024)
                    var read = 0L
                    var lastEmit = 0L
                    while (true) {
                        val n = input.read(buf)
                        if (n < 0) break
                        output.write(buf, 0, n)
                        digest.update(buf, 0, n)
                        read += n
                        if (total > 0 && read - lastEmit > 128 * 1024) {
                            lastEmit = read
                            _state.value = UpdateState.Downloading(release, (read.toFloat() / total).coerceIn(0f, 1f))
                        }
                    }
                }
            }
        } finally {
            conn.disconnect()
        }

        val expected = release.checksumsUrl?.let { expectedSha256(it, release.apkName) }
        if (expected != null) {
            val actual = digest.digest().joinToString("") { "%02x".format(it) }
            if (!actual.equals(expected, ignoreCase = true)) {
                part.delete()
                error("Checksum mismatch, download discarded")
            }
        }
        if (target.exists()) target.delete()
        check(part.renameTo(target)) { "Couldn't save the update" }
        target
    }

    /** Reads the `sha256sum` line for [fileName] from the release's checksums asset. */
    private fun expectedSha256(url: String, fileName: String): String? = runCatching {
        val conn = open(url).apply { setRequestProperty("Accept", "application/octet-stream") }
        try {
            conn.inputStream.bufferedReader().readLines()
                .map { it.trim().split(Regex("\\s+"), limit = 2) }
                .firstOrNull { it.size == 2 && it[1].trimStart('*') == fileName }
                ?.get(0)
        } finally {
            conn.disconnect()
        }
    }.getOrNull()

    /** True when [candidate] (x.y.z) is a later version than [installed]. */
    internal fun isNewer(candidate: String, installed: String): Boolean {
        fun parts(v: String) = Regex("""\d+""").findAll(v.substringBefore('-')).map { it.value.toInt() }.toList()
        val a = parts(candidate)
        val b = parts(installed)
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }
}
