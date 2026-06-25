package com.lockedin.feature.tools.timer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    onBack: () -> Unit,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val progress = if (uiState.totalTimeMs > 0) {
        uiState.timeRemainingMs.toFloat() / uiState.totalTimeMs.toFloat()
    } else 0f

    val minutes = (uiState.timeRemainingMs / 1000 / 60).toInt()
    val seconds = ((uiState.timeRemainingMs / 1000) % 60).toInt()

    val stateLabel = when (uiState.state) {
        TimerState.IDLE -> "Ready"
        TimerState.WORKING -> "Focus"
        TimerState.SHORT_BREAK -> "Short Break"
        TimerState.LONG_BREAK -> "Long Break"
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val progressColor = when (uiState.state) {
        TimerState.WORKING -> MaterialTheme.colorScheme.primary
        TimerState.SHORT_BREAK -> MaterialTheme.colorScheme.secondary
        TimerState.LONG_BREAK -> MaterialTheme.colorScheme.tertiary
        TimerState.IDLE -> MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = { Text("Pomodoro Timer", style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // State label
        Text(
            text = stateLabel,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = progressColor
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Circular progress ring
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(260.dp)
        ) {
            Canvas(modifier = Modifier.size(260.dp)) {
                val strokeWidth = 12.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2f
                val topLeft = Offset(
                    (size.width - radius * 2) / 2f,
                    (size.height - radius * 2) / 2f
                )
                val arcSize = Size(radius * 2, radius * 2)

                // Background track
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Progress arc
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Time display
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cycle counter
        Text(
            text = "Cycle ${uiState.currentCycle} of ${uiState.totalCycles}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset button
            OutlinedIconButton(
                onClick = { viewModel.reset() },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.Stop,
                    contentDescription = "Reset",
                    modifier = Modifier.size(28.dp)
                )
            }

            // Start/Pause button
            FilledIconButton(
                onClick = { viewModel.startPause() },
                modifier = Modifier.size(72.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = progressColor
                )
            ) {
                Icon(
                    imageVector = if (uiState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (uiState.isRunning) "Pause" else "Start",
                    modifier = Modifier.size(36.dp),
                    tint = Color.White
                )
            }

            // Spacer for symmetry
            Spacer(modifier = Modifier.size(56.dp))
        }
    }
}
