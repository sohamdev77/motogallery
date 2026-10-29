package com.motogallery.app.media.editor

import android.graphics.ColorMatrix

enum class PhotoFilter(val displayName: String) {
    ORIGINAL("Original"),
    VIVID("Vivid"),
    MOTO_NEON("Moto Neon"),
    GOLDEN_HOUR("Golden"),
    COOL_ICE("Cool"),
    DEEP_BW("B&W"),
    SEPIA("Sepia"),
    NOIR("Noir");

    fun getColorMatrix(): ColorMatrix {
        val cm = ColorMatrix()
        when (this) {
            ORIGINAL -> cm.reset()
            VIVID -> {
                // Boost saturation and contrast slightly
                cm.setSaturation(1.35f)
                val scale = 1.15f
                val translate = (-0.5f * scale + 0.5f) * 255f
                val contrastMatrix = ColorMatrix(
                    floatArrayOf(
                        scale, 0f, 0f, 0f, translate,
                        0f, scale, 0f, 0f, translate,
                        0f, 0f, scale, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(contrastMatrix)
            }
            MOTO_NEON -> {
                // Signature violet/pink and cyan Motorola Edge neon grade
                cm.set(
                    floatArrayOf(
                        1.25f, 0.0f, 0.15f, 0f, 10f,  // Red / Magenta
                        0.0f, 0.95f, 0.20f, 0f, 5f,   // Green
                        0.25f, 0.15f, 1.35f, 0f, 25f,  // Blue / Violet boost
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            GOLDEN_HOUR -> {
                // Warm sunset amber glow
                cm.set(
                    floatArrayOf(
                        1.25f, 0f, 0f, 0f, 25f,
                        0f, 1.1f, 0f, 0f, 15f,
                        0f, 0f, 0.85f, 0f, -10f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            COOL_ICE -> {
                // Crisp cold high-key tone
                cm.set(
                    floatArrayOf(
                        0.85f, 0f, 0f, 0f, -10f,
                        0f, 1.05f, 0f, 0f, 5f,
                        0f, 0f, 1.25f, 0f, 25f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            DEEP_BW -> {
                cm.setSaturation(0f)
                val scale = 1.3f
                val translate = (-0.5f * scale + 0.5f) * 255f
                val contrastMatrix = ColorMatrix(
                    floatArrayOf(
                        scale, 0f, 0f, 0f, translate,
                        0f, scale, 0f, 0f, translate,
                        0f, 0f, scale, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(contrastMatrix)
            }
            SEPIA -> {
                val sepiaMatrix = ColorMatrix(
                    floatArrayOf(
                        0.393f, 0.769f, 0.189f, 0f, 0f,
                        0.349f, 0.686f, 0.168f, 0f, 0f,
                        0.272f, 0.534f, 0.131f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.set(sepiaMatrix)
            }
            NOIR -> {
                // High contrast cinematic monochrome with deep crushed shadows
                cm.setSaturation(0f)
                val scale = 1.55f
                val translate = (-0.5f * scale + 0.5f) * 255f - 15f
                val contrastMatrix = ColorMatrix(
                    floatArrayOf(
                        scale, 0f, 0f, 0f, translate,
                        0f, scale, 0f, 0f, translate,
                        0f, 0f, scale, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(contrastMatrix)
            }
        }
        return cm
    }
}
