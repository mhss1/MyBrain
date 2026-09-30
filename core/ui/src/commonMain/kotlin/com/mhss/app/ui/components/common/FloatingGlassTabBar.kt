package com.mhss.app.ui.components.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.LiquidState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FloatingGlassTabBar(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    liquidState: LiquidState,
    modifier: Modifier = Modifier
) {
    val shape = FloatingToolbarDefaults.ContainerShape
    val selectionPosition by animateFloatAsState(
        targetValue = selectedTabIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tabSelectionPosition"
    )
    val selectionColor by animateColorAsState(
        targetValue = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) {
            Color.White.copy(alpha = 0.12f)
        } else {
            Color.Black.copy(alpha = 0.08f)
        },
        label = "tabSelectionColor"
    )
    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier
            .height(56.dp)
            .frostedGlass(
                liquidState = liquidState,
                shape = shape,
                tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.30f),
                edge = 0.02f
            ),
        colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
            toolbarContainerColor = Color.Transparent,
            toolbarContentColor = MaterialTheme.colorScheme.onSurface
        ),
        contentPadding = PaddingValues(4.dp),
        expandedShadowElevation = 0.dp,
        collapsedShadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .height(48.dp)
                .selectableGroup()
                .drawBehind {
                    if (tabs.isNotEmpty()) {
                        val tabWidth = size.width / tabs.size
                        val position = if (layoutDirection == LayoutDirection.Rtl) {
                            tabs.lastIndex - selectionPosition
                        } else {
                            selectionPosition
                        }
                        drawRoundRect(
                            color = selectionColor,
                            topLeft = Offset(tabWidth * position, 0f),
                            size = Size(tabWidth, size.height),
                            cornerRadius = CornerRadius(size.height / 2)
                        )
                    }
                }
        ) {
            tabs.forEachIndexed { index, title ->
                val contentColor by animateColorAsState(
                    targetValue = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = if (selectedTabIndex == index) 1f else 0.6f
                    ),
                    label = "tabContentColor"
                )
                Tab(
                    modifier = Modifier.width(96.dp).clip(RoundedCornerShape(percent = 50)),
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    selected = selectedTabIndex == index,
                    onClick = { onTabSelected(index) },
                    selectedContentColor = contentColor,
                    unselectedContentColor = contentColor
                )
            }
        }
    }
}
