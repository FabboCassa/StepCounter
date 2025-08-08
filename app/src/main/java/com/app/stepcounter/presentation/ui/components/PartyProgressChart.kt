package com.app.stepcounter.presentation.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.stepcounter.domain.model.Participant
import com.app.stepcounter.ui.theme.StepCounterTheme

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun PartyProgressChart(participants: List<Participant>) {
    val lineColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.inversePrimary
    )

    val icon = rememberVectorPainter(image = Icons.Default.Person)
    val iconSizePx = with(LocalDensity.current) { 24.dp.toPx() }

    // We calculate the total steps to check for the zero state
    val totalSteps = remember(participants) { participants.sumOf { it.steps } }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(vertical = 16.dp)
    ) {
        // ✅ ADD THIS CHECK: If total steps are 0, show a message.
        if (totalSteps == 0) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Il grafico apparirà quando qualcuno inizierà a camminare!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Otherwise, draw the canvas as before.
            val canvasWidth = constraints.maxWidth.toFloat()
            val canvasHeight = constraints.maxHeight.toFloat()
            val maxSteps = participants.maxOfOrNull { it.steps }?.toFloat() ?: 1f

            Canvas(modifier = Modifier.fillMaxSize()) {
                participants.forEachIndexed { index, participant ->
                    val color = lineColors[index % lineColors.size]
                    val startX = 0f
                    val startY = canvasHeight
                    val endX = canvasWidth - (iconSizePx / 2)
                    val endY = canvasHeight - (participant.steps / maxSteps * canvasHeight)

                    val path = Path().apply {
                        moveTo(startX, startY)
                        cubicTo(
                            startX + (endX - startX) / 3, startY,
                            endX - (endX - startX) / 3, endY,
                            endX, endY
                        )
                    }

                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 8f, cap = StrokeCap.Round, pathEffect = PathEffect.cornerPathEffect(32f))
                    )

                    with(icon) {
                        translate(
                            left = endX - (iconSizePx / 2),
                            top = endY - iconSizePx
                        ) {
                            draw(size = Size(iconSizePx, iconSizePx))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PartyProgressChartPreview() {
    // ✅ Wrap the component in your app's theme for the preview
    StepCounterTheme {
        val sampleParticipants = listOf(
            Participant(userId = "1", name = "Fabbo", steps = 1200),
            Participant(userId = "2", name = "Cassa", steps = 800),
            Participant(userId = "3", name = "Test", steps = 1500)
        )
        PartyProgressChart(participants = sampleParticipants)
    }
}