package com.motogallery.app.ui.screens.seasonal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motogallery.app.ui.theme.LocalHalloweenConfig
import com.motogallery.app.ui.theme.MotoHalloweenAmber
import com.motogallery.app.ui.theme.MotoHalloweenBorder
import com.motogallery.app.ui.theme.MotoHalloweenGlow
import com.motogallery.app.ui.theme.MotoHalloweenOrange
import com.motogallery.app.ui.theme.MotoHalloweenPurple
import com.motogallery.app.ui.theme.MotoHalloweenSurface
import com.motogallery.app.ui.theme.MotoTextPrimary
import com.motogallery.app.ui.theme.MotoTextSecondary
import kotlinx.coroutines.delay
import kotlin.math.sin

/**
 * Session memory for the Settings pumpkin easter egg so it only triggers
 * once per app session automatically, but can also re-trigger when manually enabled.
 */
object EasterEggSessionState {
    var hasShownPumpkinInSession: Boolean = false
    
    fun reset() {
        hasShownPumpkinInSession = false
    }
}

/**
 * Ambient Halloween decorative overlay placed over gallery and screens.
 * Contains subtle cobweb corners and optional flying bats.
 */
@Composable
fun HalloweenAmbientOverlay(
    modifier: Modifier = Modifier
) {
    val config = LocalHalloweenConfig.current
    if (!config.isEnabled) return

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 1. Subtle Cobweb in Top-Right Corner
        CobwebCorner(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(72.dp)
        )

        // 2. Subtle Cobweb in Top-Left Corner
        CobwebCorner(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(54.dp)
                .graphicsLayer(scaleX = -1f)
        )

        // 3. Graceful Flying Bats (if special animations & bats are enabled)
        if (config.specialAnimationsEnabled && config.flyingBatsEnabled) {
            FlyingBatsHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .align(Alignment.TopCenter)
            )
        }
    }
}

/**
 * Delicate geometric cobweb drawn on Canvas
 */
@Composable
fun CobwebCorner(
    modifier: Modifier = Modifier,
    tint: Color = MotoHalloweenAmber.copy(alpha = 0.28f)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Radial anchor lines from the very corner
        val startCorner = Offset(w, 0f)
        val radii = listOf(
            Offset(0f, 0f),
            Offset(w * 0.15f, h * 0.45f),
            Offset(w * 0.45f, h * 0.85f),
            Offset(w, h)
        )

        for (pt in radii) {
            drawLine(
                color = tint,
                start = startCorner,
                end = pt,
                strokeWidth = 1.2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Concentric connecting web arcs
        val steps = 4
        for (step in 1..steps) {
            val factor = step / steps.toFloat()
            val path = Path().apply {
                val p0 = Offset(w - (w * factor), 0f)
                val p1 = Offset(w - (w * 0.85f * factor), h * 0.45f * factor)
                val p2 = Offset(w - (w * 0.55f * factor), h * 0.85f * factor)
                val p3 = Offset(w, h * factor)

                moveTo(p0.x, p0.y)
                quadraticBezierTo(
                    w - (w * 0.95f * factor * 0.9f),
                    h * 0.25f * factor * 1.15f,
                    p1.x,
                    p1.y
                )
                quadraticBezierTo(
                    w - (w * 0.65f * factor * 0.85f),
                    h * 0.65f * factor * 1.15f,
                    p2.x,
                    p2.y
                )
                quadraticBezierTo(
                    w - (w * 0.20f * factor * 0.85f),
                    h * 0.95f * factor * 1.15f,
                    p3.x,
                    p3.y
                )
            }

            drawPath(
                path = path,
                color = tint,
                style = Stroke(width = 1.0.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * Lightweight, graceful flying bats floating across the top screen area.
 */
@Composable
fun FlyingBatsHeader(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BatsTransition")

    // Bat 1 flight
    val bat1X by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 1100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bat1X"
    )
    val bat1WingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bat1Wing"
    )

    // Bat 2 flight (opposite direction / delayed drift)
    val bat2X by infiniteTransition.animateFloat(
        initialValue = 1050f,
        targetValue = -60f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9200, delayMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bat2X"
    )
    val bat2WingScale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 320, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bat2Wing"
    )

    Box(modifier = modifier) {
        // Bat 1
        val bat1Y = (24 + 14 * sin(bat1X * 0.015f)).dp
        SingleBatSilhouette(
            modifier = Modifier
                .graphicsLayer {
                    translationX = bat1X
                    translationY = bat1Y.toPx()
                    scaleY = bat1WingScale
                    rotationZ = 4f * sin(bat1X * 0.02f)
                }
                .size(26.dp)
        )

        // Bat 2 (smaller, slightly lower altitude)
        val bat2Y = (42 + 10 * sin(bat2X * 0.018f)).dp
        SingleBatSilhouette(
            modifier = Modifier
                .graphicsLayer {
                    translationX = bat2X
                    translationY = bat2Y.toPx()
                    scaleX = -1f // flying leftwards
                    scaleY = bat2WingScale
                    rotationZ = -5f * sin(bat2X * 0.02f)
                }
                .size(19.dp)
        )
    }
}

/**
 * Stylized vector silhouette of a bat drawn with high-contrast smooth curves
 */
@Composable
fun SingleBatSilhouette(
    modifier: Modifier = Modifier,
    color: Color = MotoHalloweenOrange.copy(alpha = 0.65f)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            // Bat center head & ears
            moveTo(w * 0.5f, h * 0.35f)
            lineTo(w * 0.44f, h * 0.15f) // left ear
            lineTo(w * 0.47f, h * 0.40f)
            // Left wing top curve
            cubicTo(w * 0.35f, h * 0.10f, w * 0.15f, h * 0.12f, 0f, h * 0.40f)
            // Left wing bottom scalloped edges
            cubicTo(w * 0.10f, h * 0.65f, w * 0.20f, h * 0.70f, w * 0.28f, h * 0.55f)
            cubicTo(w * 0.35f, h * 0.75f, w * 0.42f, h * 0.65f, w * 0.46f, h * 0.85f) // body bottom
            // Right wing bottom scalloped edges
            cubicTo(w * 0.54f, h * 0.65f, w * 0.65f, h * 0.75f, w * 0.72f, h * 0.55f)
            cubicTo(w * 0.80f, h * 0.70f, w * 0.90f, h * 0.65f, w, h * 0.40f)
            // Right wing top curve
            cubicTo(w * 0.85f, h * 0.12f, w * 0.65f, h * 0.10f, w * 0.53f, h * 0.40f)
            lineTo(w * 0.56f, h * 0.15f) // right ear
            close()
        }

        drawPath(path = path, color = color)
    }
}

/**
 * Playful, scary smiling pumpkin (Jack-o'-lantern) Easter Egg.
 * Pops up in Settings with a funny/creepy smile animation, glows, and auto-dismisses.
 */
@Composable
fun PumpkinEasterEggPopup(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var smileProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            // Animate smile widening
            delay(250)
            smileProgress = 1f
            // Auto dismiss after ~2.6 seconds
            delay(2600)
            onDismiss()
        } else {
            smileProgress = 0f
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.3f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(300)),
        exit = scaleOut(
            targetScale = 0.5f,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(250)),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MotoHalloweenSurface.copy(alpha = 0.96f),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, MotoHalloweenOrange),
            shadowElevation = 16.dp,
            modifier = Modifier
                .padding(24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp)
            ) {
                // Glowing Animated Jack-o'-Lantern
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(110.dp)
                ) {
                    // Spooky glowing halo
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MotoHalloweenGlow.copy(alpha = 0.45f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Vector Pumpkin Face
                    ScaryPumpkinCanvas(
                        smileProgress = smileProgress,
                        modifier = Modifier.size(92.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Boo! Happy Halloween! 🎃",
                    color = MotoHalloweenOrange,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "MotoGallery seasonal easter egg unlocked!",
                    color = MotoTextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Detailed procedural Jack-o'-lantern canvas with glowing triangular eyes,
 * carved nose, scary smiling mouth with jagged teeth, and pumpkin stem.
 */
@Composable
fun ScaryPumpkinCanvas(
    smileProgress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Green Stem on Top
        val stemPath = Path().apply {
            moveTo(w * 0.48f, h * 0.18f)
            cubicTo(w * 0.48f, h * 0.05f, w * 0.58f, h * 0.02f, w * 0.62f, h * 0.04f)
            cubicTo(w * 0.60f, h * 0.10f, w * 0.54f, h * 0.12f, w * 0.53f, h * 0.18f)
            close()
        }
        drawPath(path = stemPath, color = Color(0xFF4E7C32))

        // 2. Pumpkin Outer Lobes / Ribs (layered for 3D depth)
        val outerLeft = Path().apply {
            moveTo(w * 0.5f, h * 0.18f)
            cubicTo(w * 0.15f, h * 0.18f, w * 0.04f, h * 0.52f, w * 0.08f, h * 0.78f)
            cubicTo(w * 0.12f, h * 0.95f, w * 0.38f, h * 0.96f, w * 0.5f, h * 0.92f)
            close()
        }
        val outerRight = Path().apply {
            moveTo(w * 0.5f, h * 0.18f)
            cubicTo(w * 0.85f, h * 0.18f, w * 0.96f, h * 0.52f, w * 0.92f, h * 0.78f)
            cubicTo(w * 0.88f, h * 0.95f, w * 0.62f, h * 0.96f, w * 0.5f, h * 0.92f)
            close()
        }
        val centerLobe = Path().apply {
            moveTo(w * 0.5f, h * 0.16f)
            cubicTo(w * 0.30f, h * 0.18f, w * 0.22f, h * 0.55f, w * 0.25f, h * 0.82f)
            cubicTo(w * 0.28f, h * 0.96f, w * 0.72f, h * 0.96f, w * 0.75f, h * 0.82f)
            cubicTo(w * 0.78f, h * 0.55f, w * 0.70f, h * 0.18f, w * 0.5f, h * 0.16f)
            close()
        }

        // Draw pumpkin body with warm gradient
        val pumpkinBrush = Brush.verticalGradient(
            colors = listOf(MotoHalloweenAmber, MotoHalloweenOrange, Color(0xFFC44B00))
        )
        drawPath(outerLeft, pumpkinBrush)
        drawPath(outerRight, pumpkinBrush)
        drawPath(centerLobe, pumpkinBrush)

        // Subtle dark rib creases
        drawArc(
            color = Color(0xFF9E3A00).copy(alpha = 0.5f),
            startAngle = 100f,
            sweepAngle = 160f,
            useCenter = false,
            topLeft = Offset(w * 0.15f, h * 0.22f),
            size = androidx.compose.ui.geometry.Size(w * 0.35f, h * 0.68f),
            style = Stroke(width = 1.5.dp.toPx())
        )
        drawArc(
            color = Color(0xFF9E3A00).copy(alpha = 0.5f),
            startAngle = -80f,
            sweepAngle = 160f,
            useCenter = false,
            topLeft = Offset(w * 0.50f, h * 0.22f),
            size = androidx.compose.ui.geometry.Size(w * 0.35f, h * 0.68f),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // 3. Spooky Carved Features (Glowing Warm Golden-Yellow / Eerie Dark Interior)
        val carveGlowColor = Color(0xFFFFD54F) // Radiant candle glow inside pumpkin
        val carveShadowColor = Color(0xFF2E0C00) // Deep carved depth

        // Left Eye (Angular Spooky Triangle)
        val leftEye = Path().apply {
            moveTo(w * 0.34f, h * 0.42f)
            lineTo(w * 0.44f, h * 0.46f)
            lineTo(w * 0.38f, h * 0.54f)
            close()
        }
        drawPath(leftEye, carveShadowColor)
        drawPath(leftEye, carveGlowColor, style = Stroke(width = 1.2.dp.toPx()))

        // Right Eye (Angular Spooky Triangle tilted symmetrically)
        val rightEye = Path().apply {
            moveTo(w * 0.66f, h * 0.42f)
            lineTo(w * 0.56f, h * 0.46f)
            lineTo(w * 0.62f, h * 0.54f)
            close()
        }
        drawPath(rightEye, carveShadowColor)
        drawPath(rightEye, carveGlowColor, style = Stroke(width = 1.2.dp.toPx()))

        // Nose (Small triangle)
        val nose = Path().apply {
            moveTo(w * 0.50f, h * 0.52f)
            lineTo(w * 0.45f, h * 0.59f)
            lineTo(w * 0.55f, h * 0.59f)
            close()
        }
        drawPath(nose, carveGlowColor)

        // 4. Scary Carved Smile with Jagged Teeth (animates wider as smileProgress reaches 1f)
        val smileWidth = w * (0.50f + 0.16f * smileProgress)
        val mouthLeft = w * 0.5f - smileWidth / 2f
        val mouthRight = w * 0.5f + smileWidth / 2f
        val mouthTopY = h * 0.66f
        val mouthBottomY = h * (0.80f + 0.05f * smileProgress)

        val mouthPath = Path().apply {
            // Upper lip with jagged upper teeth
            moveTo(mouthLeft, mouthTopY)
            lineTo(mouthLeft + smileWidth * 0.22f, mouthTopY + 2f)
            lineTo(mouthLeft + smileWidth * 0.28f, mouthTopY + h * 0.05f) // tooth pointing down
            lineTo(mouthLeft + smileWidth * 0.36f, mouthTopY + 2f)
            lineTo(mouthLeft + smileWidth * 0.64f, mouthTopY + 2f)
            lineTo(mouthLeft + smileWidth * 0.72f, mouthTopY + h * 0.05f) // tooth pointing down
            lineTo(mouthLeft + smileWidth * 0.78f, mouthTopY + 2f)
            lineTo(mouthRight, mouthTopY)

            // Lower lip with bottom jagged teeth pointing up
            lineTo(mouthLeft + smileWidth * 0.85f, mouthBottomY)
            lineTo(mouthLeft + smileWidth * 0.58f, mouthBottomY - 1f)
            lineTo(mouthLeft + smileWidth * 0.50f, mouthBottomY - h * 0.05f) // center lower tooth pointing up
            lineTo(mouthLeft + smileWidth * 0.42f, mouthBottomY - 1f)
            lineTo(mouthLeft + smileWidth * 0.15f, mouthBottomY)
            close()
        }

        drawPath(mouthPath, carveShadowColor)
        drawPath(mouthPath, carveGlowColor, style = Stroke(width = 1.8.dp.toPx()))
    }
}
