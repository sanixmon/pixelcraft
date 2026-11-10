package com.app.pixelcraft.presentation
import com.app.pixelcraft.presentation.i18n.localizedLabel
import com.app.pixelcraft.presentation.i18n.asString
import androidx.compose.ui.platform.LocalContext
import com.app.pixelcraft.R
import androidx.compose.ui.res.stringResource
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.pixelcraft.data.model.AspectRatioPreset
import com.app.pixelcraft.data.model.BackgroundType
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.PresetCategory
import com.app.pixelcraft.data.model.ProjectSummary
import com.app.pixelcraft.data.model.StylePreset
import com.app.pixelcraft.data.presets.DefaultPresets
import com.app.pixelcraft.presentation.components.dialogs.PixelCraftAboutDialog
import com.app.pixelcraft.presentation.components.dialogs.PixelCraftImageSizeDialog
import com.app.pixelcraft.presentation.components.dialogs.PixelCraftRenameDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun HomeTopBar(onAbout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF141822))
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF00E5FF), Color(0xFF651FFF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("P", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.ui_typography_graphic_studio),
                    color = Color(0xFF8E97AA),
                    fontSize = 11.sp
                )
            }
        }

        IconButton(onClick = { onAbout() }) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = stringResource(R.string.ui_about_pixelcraft),
                tint = Color(0xFFB8C0D0)
            )
        }
    }
}

/**
 * Confirms deleting a saved project. [onConfirm] receives the localized snackbar message and
 * undo label so the caller can offer "Undo" after the dialog has closed.
 */
@Composable
internal fun DeleteProjectDialog(
    projectName: String,
    onConfirm: (deletedMessage: String, undoLabel: String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E222B),
        title = {
            Text(stringResource(R.string.ui_delete_project), color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Text(
                text = stringResource(R.string.ui_remove_project_confirm, projectName),
                color = Color.White,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            val deletedMessage = stringResource(R.string.msg_project_deleted, projectName)
            val undoLabel = stringResource(R.string.ui_undo)
            Button(
                onClick = { onConfirm(deletedMessage, undoLabel) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
            ) {
                Text(stringResource(R.string.action_delete), color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = Color.White)
            }
        }
    )
}

@Composable
internal fun DraftRecoveryCard(
    projectName: String,
    timestamp: Long?,
    onResume: () -> Unit,
    onDiscard: () -> Unit
) {
    val recentlyLabel = stringResource(R.string.ui_recently)
    val formattedTime = remember(timestamp, recentlyLabel) {
        if (timestamp == null) recentlyLabel
        else SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF18283B), Color(0xFF1F1D36))
                )
            )
            .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.ui_unsaved_draft_ready, projectName),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(R.string.ui_autosaved_at, formattedTime),
                    color = Color(0xFFA5B1C6),
                    fontSize = 11.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onResume,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.ui_resume_draft), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            OutlinedButton(onClick = onDiscard) {
                Text(stringResource(R.string.action_discard), color = Color(0xFFFF8A80), fontSize = 12.sp)
            }
        }
    }
}

@Composable
internal fun QuickStartActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF181C27))
            .border(1.dp, Color(0xFF272E40), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
        Text(subtitle, color = Color(0xFF8E97AA), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun HomeTemplateCard(
    preset: StylePreset,
    onClick: () -> Unit
) {
    val bgBrush = remember(preset.id) {
        when (preset.canvasConfig.backgroundType) {
            BackgroundType.GRADIENT -> {
                val cols = preset.canvasConfig.gradientColors.map { Color(it.toInt()) }
                Brush.linearGradient(if (cols.size >= 2) cols else listOf(Color(0xFF1F1C2C), Color(0xFF928DAB)))
            }
            else -> {
                val c = Color(preset.canvasConfig.backgroundColor.toInt())
                Brush.linearGradient(listOf(c, c.copy(alpha = 0.85f)))
            }
        }
    }
    val primaryText = remember(preset.id) {
        preset.initialLayers.firstOrNull { it.type == LayerType.TEXT }
    }

    Column(
        modifier = Modifier
            .width(146.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF171B26))
            .border(1.dp, Color(0xFF283044), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp)
                .background(bgBrush)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = primaryText?.text?.lineSequence()?.firstOrNull()?.take(14) ?: preset.title,
                color = Color((primaryText?.textColor ?: 0xFFFFFFFFL).toInt()),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = preset.title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${preset.category.localizedLabel()} • ${preset.canvasConfig.width}×${preset.canvasConfig.height}",
                color = Color(0xFF8E97AA),
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
internal fun ProjectThumbnailCard(
    summary: ProjectSummary,
    thumbnailsDir: File,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onExportArchive: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    var thumbImage by remember(summary.thumbnailFileName, summary.timestamp) {
        mutableStateOf<ImageBitmap?>(null)
    }

    LaunchedEffect(summary.thumbnailFileName, summary.timestamp) {
        val fileName = summary.thumbnailFileName
        if (fileName != null) {
            val bmp = withContext(Dispatchers.IO) {
                val file = File(thumbnailsDir, fileName)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                } else {
                    null
                }
            }
            thumbImage = bmp
        } else {
            thumbImage = null
        }
    }

    val dateStr = remember(summary.timestamp) {
        SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(summary.timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF171B26))
            .border(1.dp, Color(0xFF262D40), RoundedCornerShape(14.dp))
            .clickable(onClick = onOpen)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.2f)
                .background(Color(0xFF10131C)),
            contentAlignment = Alignment.Center
        ) {
            val bmp = thumbImage
            if (bmp != null) {
                Image(
                    bitmap = bmp,
                    contentDescription = summary.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = "${summary.width}×${summary.height}",
                    color = Color(0xFF5D667A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = summary.name,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${summary.layerCount} layers • $dateStr",
                    color = Color(0xFF8E97AA),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.ui_project_actions),
                        tint = Color(0xFFB8C0D0),
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(Color(0xFF1E2330))
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_rename), color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF00E5FF)) },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_duplicate), color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White) },
                        onClick = {
                            menuExpanded = false
                            onDuplicate()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.ui_share_pixelcraft), color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF00E676)) },
                        onClick = {
                            menuExpanded = false
                            onExportArchive()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_delete), color = Color(0xFFFF5252)) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252)) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}
