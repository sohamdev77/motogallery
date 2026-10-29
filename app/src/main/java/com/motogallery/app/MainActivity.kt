package com.motogallery.app

import android.app.Activity
import android.content.IntentSender
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.motogallery.app.ui.navigation.AppNavGraph
import com.motogallery.app.ui.theme.MotoBackground
import com.motogallery.app.ui.theme.MotoGalleryTheme
import com.motogallery.app.util.PermissionUtils

class MainActivity : FragmentActivity() {

    private val app by lazy { applicationContext as MotoGalleryApp }

    // System consent launcher for MediaStore trash, delete, and write operations
    private var pendingConsentCallback: ((Boolean) -> Unit)? = null

    private val intentSenderLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val isGranted = result.resultCode == Activity.RESULT_OK
        pendingConsentCallback?.invoke(isGranted)
        pendingConsentCallback = null
    }

    // Runtime permissions launcher for media access
    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // MediaStore observer will automatically reload media upon permission grant
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge transparent system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Check and prompt initial media permissions if needed
        if (!PermissionUtils.hasAnyMediaAccess(this)) {
            permissionsLauncher.launch(PermissionUtils.getRequiredMediaPermissions())
        }

        setContent {
            val isDarkTheme by app.preferences.isDarkTheme.collectAsState()
            val useDynamicColors by app.preferences.useDynamicColors.collectAsState()

            MotoGalleryTheme(
                darkTheme = isDarkTheme,
                dynamicColor = useDynamicColors
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MotoBackground
                ) {
                    val navController = rememberNavController()

                    AppNavGraph(
                        navController = navController,
                        onRequestConsent = { intentSender ->
                            launchSystemConsentDialog(intentSender)
                        },
                        onRequestPermissions = {
                            permissionsLauncher.launch(PermissionUtils.getRequiredMediaPermissions())
                        }
                    )
                }
            }
        }
    }

    fun launchSystemConsentDialog(
        intentSender: IntentSender,
        onResult: ((Boolean) -> Unit)? = null
    ) {
        pendingConsentCallback = onResult
        val request = IntentSenderRequest.Builder(intentSender).build()
        intentSenderLauncher.launch(request)
    }

    override fun onStop() {
        super.onStop()
        app.vaultLockManager.onAppBackgrounded()
    }

    override fun onResume() {
        super.onResume()
        val delay = app.preferences.vaultAutoLockDelay.value
        app.vaultLockManager.onAppForegrounded(delay)
    }
}
