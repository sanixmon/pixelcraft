package com.app.pixelcraft.presentation.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.BuildConfig
import com.app.pixelcraft.R
import com.app.pixelcraft.data.update.AppRelease
import com.app.pixelcraft.data.update.AppUpdater
import com.app.pixelcraft.data.update.UpdateState
import com.app.pixelcraft.ui.theme.PixelCraftColors
import java.util.Locale

/**
 * App-level host for the in-app updater. Runs a quiet check on launch and shows the
 * update prompt whenever the updater has something to say (also after a manual
 * "Check for updates" from the About dialog).
 */
@Composable
fun UpdateHost() {
    val context = LocalContext.current
    val state by AppUpdater.state.collectAsState()
    LaunchedEffect(Unit) { AppUpdater.check(context, manual = false) }

    var needsInstallPermission by remember { mutableStateOf(false) }

    when (val s = state) {
        is UpdateState.Available -> UpdateDialog(
            release = s.release,
            progress = null,
            message = null,
            primaryLabel = stringResource(R.string.ui_update_now),
            onPrimary = { AppUpdater.download(context, s.release) },
            onLater = { AppUpdater.skip(context, s.release) }
        )

        is UpdateState.Downloading -> UpdateDialog(
            release = s.release,
            progress = s.progress,
            message = null,
            primaryLabel = null,
            onPrimary = {},
            onLater = null
        )

        is UpdateState.ReadyToInstall -> UpdateDialog(
            release = s.release,
            progress = null,
            message = if (needsInstallPermission) stringResource(R.string.msg_update_allow_install) else null,
            primaryLabel = stringResource(R.string.ui_install),
            onPrimary = { needsInstallPermission = !AppUpdater.install(context, s.apk) },
            onLater = { AppUpdater.dismiss() }
        )

        is UpdateState.Failed -> if (s.release != null) {
            UpdateDialog(
                release = s.release,
                progress = null,
                message = stringResource(R.string.msg_update_failed, s.message),
                primaryLabel = stringResource(R.string.ui_retry),
                onPrimary = { AppUpdater.download(context, s.release) },
                onLater = { AppUpdater.dismiss() }
            )
        } else {
            SimpleUpdateNotice(stringResource(R.string.msg_update_failed, s.message))
        }

        UpdateState.UpToDate -> SimpleUpdateNotice(stringResource(R.string.msg_update_up_to_date, BuildConfig.VERSION_NAME))

        UpdateState.Checking, UpdateState.Idle -> Unit
    }
}

@Composable
private fun SimpleUpdateNotice(text: String) {
    AlertDialog(
        onDismissRequest = { AppUpdater.dismiss() },
        containerColor = PixelCraftColors.DialogSurface,
        shape = RoundedCornerShape(20.dp),
        text = { Text(text, color = PixelCraftColors.TextPrimary, fontSize = 14.sp) },
        confirmButton = {
            TextButton(onClick = { AppUpdater.dismiss() }) {
                Text(stringResource(R.string.ui_done), color = PixelCraftColors.Accent)
            }
        }
    )
}

@Composable
private fun UpdateDialog(
    release: AppRelease,
    progress: Float?,
    message: String?,
    primaryLabel: String?,
    onPrimary: () -> Unit,
    onLater: (() -> Unit)?
) {
    AlertDialog(
        // A running download can't be dismissed by tapping outside.
        onDismissRequest = { onLater?.invoke() },
        containerColor = PixelCraftColors.DialogSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column {
                Text(
                    text = stringResource(R.string.ui_update_available),
                    color = PixelCraftColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "${BuildConfig.VERSION_NAME} → ${release.version}" +
                        if (release.apkSizeBytes > 0) " · ${formatSize(release.apkSizeBytes)}" else "",
                    color = PixelCraftColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.ui_whats_new),
                    color = PixelCraftColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = plainChangelog(release.notes).ifBlank { release.title },
                    color = PixelCraftColors.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PixelCraftColors.Border.copy(alpha = 0.35f))
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                )
                if (progress != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = PixelCraftColors.Accent,
                        trackColor = PixelCraftColors.Border
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.ui_downloading_percent, (progress * 100).toInt()),
                        color = PixelCraftColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }
                if (message != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(message, color = PixelCraftColors.Danger, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            if (primaryLabel != null) {
                Button(
                    onClick = onPrimary,
                    colors = ButtonDefaults.buttonColors(containerColor = PixelCraftColors.Accent, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(primaryLabel, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (onLater != null) {
                TextButton(onClick = onLater) {
                    Text(stringResource(R.string.ui_later), color = PixelCraftColors.TextSecondary)
                }
            }
        }
    )
}

/** Release notes are Markdown; show them as readable plain text. */
private fun plainChangelog(markdown: String): String =
    markdown.lineSequence()
        .map { line ->
            line.trim()
                .replace(Regex("^#{1,6}\\s*"), "")
                .replace(Regex("^[-*]\\s+"), "• ")
                .replace("**", "")
                .replace("`", "")
                .replace(Regex("\\[([^\\]]+)]\\([^)]*\\)"), "$1")
        }
        .joinToString("\n")
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()

private fun formatSize(bytes: Long): String =
    if (bytes >= 1024 * 1024) String.format(Locale.US, "%.1f MB", bytes / 1048576f)
    else String.format(Locale.US, "%d KB", bytes / 1024)
