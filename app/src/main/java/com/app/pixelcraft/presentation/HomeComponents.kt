package com.app.pixelcraft.presentation

import com.app.pixelcraft.presentation.i18n.localizedLabel
import com.app.pixelcraft.ui.theme.PixelCraftColors
import androidx.compose.ui.platform.LocalContext
import com.app.pixelcraft.R
import androidx.compose.ui.res.stringResource
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.HorizontalDivider
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PixelCraftColors.Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PixelCraftColors.Accent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "P",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        color = PixelCraftColors.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.ui_typography_graphic_studio),
                        color = PixelCraftColors.TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(onClick = { onAbout() }) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(R.string.ui_about_pixelcraft),
                    tint = PixelCraftColors.TextSecondary
                )
            }
        }
        HorizontalDivider(color = PixelCraftColors.Border, thickness = 1.dp)
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
        containerColor = PixelCraftColors.DialogSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = stringResource(R.string.ui_delete_project),
                color = PixelCraftColors.Danger,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Text(
                text = stringResource(R.string.ui_remove_project_confirm, projectName),
                color = PixelCraftColors.TextPrimary,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            val deletedMessage = stringResource(R.string.msg_project_deleted, projectName)
            val undoLabel = stringResource(R.string.ui_undo)
            Button(
                onClick = { onConfirm(deletedMessage, undoLabel) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PixelCraftColors.Danger,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.action_delete), color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = PixelCraftColors.TextSecondary)
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
            .clip(RoundedCornerShape(16.dp))
            .background(PixelCraftColors.Surface)
            .border(2.dp, PixelCraftColors.Accent, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = PixelCraftColors.Accent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.ui_unsaved_draft_ready, projectName),
                    color = PixelCraftColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = stringResource(R.string.ui_autosaved_at, formattedTime),
                    color = PixelCraftColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onResume,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PixelCraftColors.Accent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.ui_resume_draft),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
            OutlinedButton(
                onClick = onDiscard,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, PixelCraftColors.Danger)
            ) {
                Text(
                    text = stringResource(R.string.action_discard),
                    color = PixelCraftColors.Danger,
                    fontSize = 13.sp
                )
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
            .clip(RoundedCornerShape(16.dp))
            .background(PixelCraftColors.Surface)
            .border(1.dp, PixelCraftColors.Border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            color = PixelCraftColors.TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            maxLines = 1
        )
        Text(
            text = subtitle,
            color = PixelCraftColors.TextSecondary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
                Brush.linearGradient(if (cols.size >= 2) cols else listOf(PixelCraftColors.SurfaceElevated, PixelCraftColors.Border))
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
            .width(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PixelCraftColors.Surface)
            .border(1.dp, PixelCraftColors.Border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(bgBrush)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = primaryText?.text?.lineSequence()?.firstOrNull()?.take(14) ?: preset.title,
                color = Color((primaryText?.textColor ?: 0xFFFFFFFFL).toInt()),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = preset.title,
                color = PixelCraftColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${preset.category.localizedLabel()} • ${preset.canvasConfig.width}×${preset.canvasConfig.height}",
                color = PixelCraftColors.TextMuted,
                fontSize = 11.sp,
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
            .clip(RoundedCornerShape(16.dp))
            .background(PixelCraftColors.Surface)
            .border(1.dp, PixelCraftColors.Border, RoundedCornerShape(16.dp))
            .clickable(onClick = onOpen)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.2f)
                .background(PixelCraftColors.Background),
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
                    color = PixelCraftColors.TextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = summary.name,
                    color = PixelCraftColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${summary.layerCount} layers • $dateStr",
                    color = PixelCraftColors.TextMuted,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.ui_project_actions),
                        tint = PixelCraftColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_rename)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                tint = PixelCraftColors.Accent
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_duplicate)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onDuplicate()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.ui_share_pixelcraft)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = null,
                                tint = PixelCraftColors.Success
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onExportArchive()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(R.string.action_delete),
                                color = PixelCraftColors.Danger
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = PixelCraftColors.Danger
                            )
                        },
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
