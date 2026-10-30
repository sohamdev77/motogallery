package com.motogallery.app.ui.screens.vault

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.motogallery.app.data.vault.BiometricAuthManager
import com.motogallery.app.ui.theme.MotoBackground
import com.motogallery.app.ui.theme.MotoBorder
import com.motogallery.app.ui.theme.MotoError
import com.motogallery.app.ui.theme.MotoOrange
import com.motogallery.app.ui.theme.MotoPink
import com.motogallery.app.ui.theme.MotoSurface
import com.motogallery.app.ui.theme.MotoSurfaceVariant
import com.motogallery.app.ui.theme.MotoTextPrimary
import com.motogallery.app.ui.theme.MotoTextSecondary
import com.motogallery.app.ui.theme.MotoTextTertiary
import com.motogallery.app.ui.theme.MotoViolet
import com.motogallery.app.ui.theme.AppleSprings
import com.motogallery.app.ui.theme.MotoGlassBorderBrush
import com.motogallery.app.ui.theme.MotoGlassSurface
import com.motogallery.app.ui.theme.MotoGlassSheenBrush
import com.motogallery.app.ui.theme.applePressable
import com.motogallery.app.ui.theme.glassmorphic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch

@Composable
fun VaultAuthScreen(
    isConfigured: Boolean,
    isBiometricAvailable: Boolean,
    isBiometricEnabled: Boolean,
    errorMessage: String?,
    onSetupPin: (String) -> Unit,
    onVerifyPin: (String) -> Boolean,
    onBiometricSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var enteredPin by remember { mutableStateOf("") }
    var setupFirstPin by remember { mutableStateOf<String?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }

    val shakeOffset = remember { Animatable(0f) }

    fun triggerShake() {
        scope.launch {
            shakeOffset.animateTo(20f, tween(50))
            shakeOffset.animateTo(-20f, tween(50))
            shakeOffset.animateTo(10f, tween(50))
            shakeOffset.animateTo(-10f, tween(50))
            shakeOffset.animateTo(0f, tween(50))
        }
    }

    fun promptBiometrics() {
        val activity = context as? FragmentActivity ?: return
        val bioManager = BiometricAuthManager(context)
        bioManager.promptBiometric(
            activity = activity,
            onSuccess = onBiometricSuccess,
            onError = { localError = it }
        )
    }

    // Auto-prompt biometrics if configured and enabled
    LaunchedEffect(Unit) {
        if (isConfigured && isBiometricEnabled && isBiometricAvailable) {
            promptBiometrics()
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            localError = errorMessage
            triggerShake()
            enteredPin = ""
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MotoBackground)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MotoViolet.copy(alpha = 0.15f))
                    .border(1.5.dp, MotoViolet.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MotoViolet,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (!isConfigured) {
                    if (setupFirstPin == null) "Create Vault PIN" else "Confirm Vault PIN"
                } else "Unlock Vault",
                color = MotoTextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (!isConfigured) {
                    if (setupFirstPin == null) "Set a 4 to 6 digit PIN to protect private files" else "Re-enter your PIN to confirm"
                } else "Enter your PIN to access encrypted photos and videos",
                color = MotoTextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp).padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // PIN Dots Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.offset { IntOffset(shakeOffset.value.toInt(), 0) }
            ) {
                repeat(4) { index ->
                    val isFilled = index < enteredPin.length
                    val dotScale by animateFloatAsState(
                        targetValue = if (isFilled) 1.15f else 1.0f,
                        animationSpec = AppleSprings.BouncySpring,
                        label = "pinDotScale"
                    )
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .graphicsLayer {
                                scaleX = dotScale
                                scaleY = dotScale
                            }
                            .clip(CircleShape)
                            .background(
                                if (isFilled) MotoViolet else MotoGlassSurface
                            )
                            .border(
                                width = 1.5.dp,
                                brush = if (isFilled) androidx.compose.ui.graphics.SolidColor(MotoViolet) else MotoGlassBorderBrush,
                                shape = CircleShape
                            )
                    )
                }
            }

            // Error display
            if (localError != null) {
                Text(
                    text = localError!!,
                    color = MotoError,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 14.dp)
                )
            }
        }

        // Security Warning Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(shape = RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MotoOrange.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MotoOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "AES-256-GCM hardware encryption. Decrypted media stays in RAM only. Backup risk: uninstalling the app deletes encrypted vault files permanently.",
                    color = MotoTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        // Numeric Keypad
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("bio", "0", "del")
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { key ->
                        when (key) {
                            "bio" -> {
                                if (isConfigured && isBiometricAvailable) {
                                    KeypadButton(
                                        onClick = { promptBiometrics() }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Fingerprint,
                                            contentDescription = "Biometrics",
                                            tint = MotoViolet,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(68.dp))
                                }
                            }
                            "del" -> {
                                KeypadButton(
                                    onClick = {
                                        if (enteredPin.isNotEmpty()) {
                                            enteredPin = enteredPin.dropLast(1)
                                            localError = null
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Backspace,
                                        contentDescription = "Delete",
                                        tint = MotoTextSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            else -> {
                                KeypadButton(
                                    onClick = {
                                        if (enteredPin.length < 6) {
                                            localError = null
                                            enteredPin += key
                                            if (enteredPin.length == 4) {
                                                // Trigger verification or setup
                                                if (!isConfigured) {
                                                    if (setupFirstPin == null) {
                                                        setupFirstPin = enteredPin
                                                        enteredPin = ""
                                                    } else {
                                                        if (enteredPin == setupFirstPin) {
                                                            onSetupPin(enteredPin)
                                                        } else {
                                                            localError = "PINs do not match. Try again."
                                                            triggerShake()
                                                            enteredPin = ""
                                                            setupFirstPin = null
                                                        }
                                                    }
                                                } else {
                                                    val verified = onVerifyPin(enteredPin)
                                                    if (!verified) {
                                                        triggerShake()
                                                        enteredPin = ""
                                                    }
                                                }
                                            }
                                        }
                                    }
                                ) {
                                    Text(
                                        text = key,
                                        color = MotoTextPrimary,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MotoGlassSurface)
            .border(1.dp, MotoGlassBorderBrush, CircleShape)
            .applePressable(pressScale = 0.88f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
