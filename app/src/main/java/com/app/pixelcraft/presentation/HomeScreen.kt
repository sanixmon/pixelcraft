package com.app.pixelcraft.presentation

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
import androidx.compose.runtime.produceState
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
fun HomeScreen(
    viewModel: PixelCraftViewModel,
    onNavigateToEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showNewCanvasSizeDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var renamingProject by remember { mutableStateOf<ProjectSummary?>(null) }
    var deletingProject by remember { mutableStateOf<ProjectSummary?>(null) }
    var pendingArchiveProjectId by remember { mutableStateOf<String?>(null) }
    var projectSearchQuery by remember { mutableStateOf("") }
    var selectedTemplateCategory by remember { mutableStateOf<PresetCategory?>(null) }

    val pickPhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            viewModel.startCanvasFromPhoto(uri)
            onNavigateToEditor()
        }
    }

    val importArchiveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            viewModel.importProjectArchive(uri) { importedId ->
                viewModel.loadProject(importedId)
                onNavigateToEditor()
            }
        }
    }

    val exportArchiveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        val projectId = pendingArchiveProjectId
        pendingArchiveProjectId = null
        if (uri != null && projectId != null) {
            viewModel.exportProjectArchive(projectId, uri)
        }
    }

    LaunchedEffect(state.userMessage) {
        val msg = state.userMessage ?: return@LaunchedEffect
        try {
            snackbarHostState.showSnackbar(msg)
        } finally {
            viewModel.clearUserMessage()
        }
    }

    val filteredProjects = remember(state.projectSummaries, projectSearchQuery) {
        if (projectSearchQuery.isBlank()) {
            state.projectSummaries
        } else {
            val q = projectSearchQuery.trim()
            state.projectSummaries.filter { it.name.contains(q, ignoreCase = true) }
        }
    }

    val filteredPresets = remember(selectedTemplateCategory) {
        if (selectedTemplateCategory == null) {
            DefaultPresets.presets
        } else {
            DefaultPresets.presets.filter { it.category == selectedTemplateCategory }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF10131A),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
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
                            text = "PixelCraft",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Typography & Graphic Studio",
                            color = Color(0xFF8E97AA),
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(onClick = { showAboutDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About PixelCraft",
                        tint = Color(0xFFB8C0D0)
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Recoverable Autosave Draft Banner (§ 2.2 & § 5.2a)
            if (state.hasRecoverableDraft) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    DraftRecoveryCard(
                        projectName = state.draftProjectName ?: "Untitled",
                        timestamp = state.draftTimestamp,
                        onResume = {
                            viewModel.resumeSavedDraft()
                            onNavigateToEditor()
                        },
                        onDiscard = { viewModel.discardRecoverableDraft() }
                    )
                }
            }

            // 2. Quick Start Actions
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    Text(
                        text = "Start Creating",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickStartActionCard(
                            icon = Icons.Default.Add,
                            title = "New Canvas",
                            subtitle = "Pick ratio & size",
                            accentColor = Color(0xFF00E5FF),
                            onClick = { showNewCanvasSizeDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                        QuickStartActionCard(
                            icon = Icons.Default.AddPhotoAlternate,
                            title = "From Photo",
                            subtitle = "Gallery picture",
                            accentColor = Color(0xFF00E676),
                            onClick = {
                                pickPhotoLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        QuickStartActionCard(
                            icon = Icons.Default.FileOpen,
                            title = "Import",
                            subtitle = ".pixelcraft file",
                            accentColor = Color(0xFFFFAB40),
                            onClick = {
                                importArchiveLauncher.launch(
                                    arrayOf("application/octet-stream", "application/zip", "*/*")
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 3. Curated Starter Templates (20 templates across 6 categories, § 6.3)
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Starter Templates (${DefaultPresets.presets.size})",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedTemplateCategory == null,
                            onClick = { selectedTemplateCategory = null },
                            label = { Text("All", fontSize = 12.sp) }
                        )
                        for (cat in PresetCategory.entries) {
                            FilterChip(
                                selected = selectedTemplateCategory == cat,
                                onClick = { selectedTemplateCategory = cat },
                                label = { Text(cat.label, fontSize = 12.sp) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(filteredPresets, key = { it.id }) { preset ->
                            HomeTemplateCard(
                                preset = preset,
                                onClick = {
                                    viewModel.applyPreset(preset, recordHistory = false, markDirty = false)
                                    onNavigateToEditor()
                                }
                            )
                        }
                    }
                }
            }

            // 4. My Saved Projects Header & Search
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Projects (${state.projectSummaries.size})",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (state.projectSummaries.size >= 2) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = projectSearchQuery,
                            onValueChange = { projectSearchQuery = it },
                            placeholder = { Text("Search saved projects...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            if (filteredProjects.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF171B26))
                            .border(1.dp, Color(0xFF252B3B), RoundedCornerShape(14.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (state.projectSummaries.isEmpty()) {
                                "No saved projects yet"
                            } else {
                                "No matching projects found"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pick a template above or start a New Canvas — saved designs appear here with instant previews.",
                            color = Color(0xFF8E97AA),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                items(filteredProjects, key = { it.id }) { summary ->
                    ProjectThumbnailCard(
                        summary = summary,
                        thumbnailsDir = viewModel.thumbnailsDirectory,
                        onOpen = {
                            viewModel.loadProject(summary.id)
                            onNavigateToEditor()
                        },
                        onRename = { renamingProject = summary },
                        onDuplicate = { viewModel.duplicateSavedProject(summary.id) },
                        onExportArchive = {
                            pendingArchiveProjectId = summary.id
                            val safeFile = summary.name
                                .replace(Regex("[^A-Za-z0-9_-]+"), "_")
                                .trim('_')
                                .ifBlank { "project" }
                            exportArchiveLauncher.launch("$safeFile.pixelcraft")
                        },
                        onDelete = { deletingProject = summary }
                    )
                }
            }
        }
    }

    if (showNewCanvasSizeDialog) {
        PixelCraftImageSizeDialog(
            currentPreset = AspectRatioPreset.SQUARE,
            currentWidth = 1080,
            currentHeight = 1080,
            onApply = { preset, w, h ->
                showNewCanvasSizeDialog = false
                viewModel.createNewBlankCanvas(preset, w, h)
                onNavigateToEditor()
            },
            onDismiss = { showNewCanvasSizeDialog = false }
        )
    }

    if (showAboutDialog) {
        PixelCraftAboutDialog(onDismiss = { showAboutDialog = false })
    }

    renamingProject?.let { target ->
        PixelCraftRenameDialog(
            title = "Rename Project",
            initialName = target.name,
            onConfirm = { nextName ->
                viewModel.renameSavedProject(target.id, nextName)
                renamingProject = null
            },
            onDismiss = { renamingProject = null }
        )
    }

    deletingProject?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingProject = null },
            containerColor = Color(0xFF1E222B),
            title = {
                Text("Delete Project?", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Text(
                    text = "Remove \"${target.name}\" from saved projects?",
                    color = Color.White,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val deletedName = target.name
                        deletingProject = null
                        viewModel.deleteProject(target.id)
                        scope.launch {
                            val res = snackbarHostState.showSnackbar(
                                message = "Deleted \"$deletedName\"",
                                actionLabel = "Undo"
                            )
                            if (res == SnackbarResult.ActionPerformed) {
                                viewModel.undoDeleteProject()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingProject = null }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun DraftRecoveryCard(
    projectName: String,
    timestamp: Long?,
    onResume: () -> Unit,
    onDiscard: () -> Unit
) {
    val formattedTime = remember(timestamp) {
        if (timestamp == null) "Recently"
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
                    text = "Unsaved Draft Ready: $projectName",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Autosaved $formattedTime",
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
                Text("Resume Draft", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            OutlinedButton(onClick = onDiscard) {
                Text("Discard", color = Color(0xFFFF8A80), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun QuickStartActionCard(
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
private fun HomeTemplateCard(
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
                text = "${preset.category.label} • ${preset.canvasConfig.width}×${preset.canvasConfig.height}",
                color = Color(0xFF8E97AA),
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ProjectThumbnailCard(
    summary: ProjectSummary,
    thumbnailsDir: File,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onExportArchive: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val thumbImage by produceState<ImageBitmap?>(initialValue = null, summary.thumbnailFileName, summary.timestamp) {
        val fileName = summary.thumbnailFileName
        value = if (fileName != null) {
            withContext(Dispatchers.IO) {
                val file = File(thumbnailsDir, fileName)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                } else {
                    null
                }
            }
        } else {
            null
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
                        contentDescription = "Project Actions",
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
                        text = { Text("Rename", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF00E5FF)) },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White) },
                        onClick = {
                            menuExpanded = false
                            onDuplicate()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share .pixelcraft", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF00E676)) },
                        onClick = {
                            menuExpanded = false
                            onExportArchive()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color(0xFFFF5252)) },
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
