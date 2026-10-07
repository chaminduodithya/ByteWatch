package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.DataUsageManager

@Composable
fun UsageGauge(
    usedBytes: Long,
    limitBytes: Long,
    modifier: Modifier = Modifier,
    isBits: Boolean = false,
    rolloverMessage: String = ""
) {
    val percentageRatio = if (limitBytes > 0) (usedBytes.toDouble() / limitBytes.toDouble()) else 0.0
    val percentageInt = (percentageRatio * 100).toInt()
    val isOverLimit = percentageRatio > 1.0
    val isNearLimit = percentageRatio >= 0.8 && !isOverLimit

    // Material 3 Gauge Colors
    val activeColor = when {
        isOverLimit -> MaterialTheme.colorScheme.error
        isNearLimit -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    val progressSweepRatio = percentageRatio.coerceIn(0.0, 1.0).toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progressSweepRatio,
        animationSpec = tween(durationMillis = 1000),
        label = "gauge_progress"
    )

    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(1.25f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidthPx = 20.dp.toPx()
                val diameter = size.minDimension - strokeWidthPx
                val topLeftX = (size.width - diameter) / 2
                val topLeftY = (size.height - diameter) / 2

                val startAngle = 150f
                val maxSweepAngle = 240f

                // Background track arc
                drawArc(
                    color = trackColor,
                    startAngle = startAngle,
                    sweepAngle = maxSweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                    topLeft = Offset(topLeftX, topLeftY),
                    size = Size(diameter, diameter)
                )

                // Active progress arc
                drawArc(
                    color = activeColor,
                    startAngle = startAngle,
                    sweepAngle = maxSweepAngle * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                    topLeft = Offset(topLeftX, topLeftY),
                    size = Size(diameter, diameter)
                )
            }

            // Gauge Center Text Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = "$percentageInt% of limit",
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isOverLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                val formattedUsed = DataUsageManager.formatBytes(usedBytes, isBits)
                val formattedLimit = DataUsageManager.formatBytes(limitBytes, isBits)

                Text(
                    text = "$formattedUsed / $formattedLimit",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Status Assist Badge
        val (badgeText, badgeContainerColor, badgeContentColor) = when {
            isOverLimit -> {
                val excess = usedBytes - limitBytes
                Triple(
                    "+${DataUsageManager.formatBytes(excess, isBits)} over limit",
                    MaterialTheme.colorScheme.errorContainer,
                    MaterialTheme.colorScheme.onErrorContainer
                )
            }
            isNearLimit -> {
                val remaining = limitBytes - usedBytes
                Triple(
                    "${DataUsageManager.formatBytes(remaining, isBits)} remaining (80%+ used)",
                    MaterialTheme.colorScheme.tertiaryContainer,
                    MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            else -> {
                val remaining = (limitBytes - usedBytes).coerceAtLeast(0L)
                Triple(
                    "${DataUsageManager.formatBytes(remaining, isBits)} remaining",
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Surface(
            color = badgeContainerColor,
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = badgeContentColor,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
            )
        }

        // Material 3 Rollover Audit Banner
        if (rolloverMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Savings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = rolloverMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}
