package com.app.pixelcraft.presentation.components.dialogs

import com.app.pixelcraft.presentation.i18n.localizedLabel
import com.app.pixelcraft.R
import androidx.compose.ui.res.stringResource
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.data.model.ShapeCategory
import com.app.pixelcraft.data.model.ShapeType
import com.app.pixelcraft.domain.ShapePathBuilder

@Composable
fun PixelCraftShapeDialog(
    onSelectShape: (ShapeType) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(ShapeCategory.BASIC) }
    val filteredShapes = remember(selectedCategory) {
        ShapeType.entries.filter { it.category == selectedCategory }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E222B),
        title = {
            Text(
                text = stringResource(R.string.ui_shapes_word_balloons),
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF),
                fontSize = 17.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (cat in ShapeCategory.entries) {
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.localizedLabel(), fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(270.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredShapes) { shape ->
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF181A20))
                                .clickable { onSelectShape(shape) }
                                .padding(vertical = 10.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            ShapeVectorPreview(
                                shapeType = shape,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = shape.localizedLabel(),
                                color = Color.White,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = Color.White)
            }
        }
    )
}

@Composable
internal fun ShapeVectorPreview(
    shapeType: ShapeType,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val aspect = (shapeType.defaultWidth / shapeType.defaultHeight.coerceAtLeast(1f)).coerceIn(0.6f, 1.8f)
        val boxW = size.width * 0.84f
        val boxH = size.height * 0.84f
        val drawW: Float
        val drawH: Float
        if (aspect >= 1f) {
            drawW = boxW
            drawH = (boxW / aspect).coerceAtMost(boxH)
        } else {
            drawH = boxH
            drawW = (boxH * aspect).coerceAtMost(boxW)
        }

        val path = ShapePathBuilder.buildPath(
            shapeType = shapeType,
            width = drawW,
            height = drawH,
            cornerRadius = 6.dp.toPx()
        )

        val native = drawContext.canvas.nativeCanvas
        native.save()
        native.translate(size.width / 2f, size.height / 2f)

        val isBalloon = shapeType.category == ShapeCategory.BALLOONS
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = if (isBalloon) 0xFFEFF8FF.toInt() else 0x3800E5FF
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.dp.toPx()
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            color = 0xFF00E5FF.toInt()
        }
        native.drawPath(path, fillPaint)
        native.drawPath(path, strokePaint)
        native.restore()
    }
}
