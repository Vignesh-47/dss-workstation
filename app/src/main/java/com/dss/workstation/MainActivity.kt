package com.dss.workstation

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dss.workstation.ui.components.StaffPinDialog
import com.dss.workstation.ui.learner.LearnerScreen
import com.dss.workstation.ui.learner.LearnerViewModel
import com.dss.workstation.ui.staff.StaffDashboardScreen
import com.dss.workstation.ui.staff.StaffViewModel
import com.dss.workstation.ui.theme.DssWorkstationTheme

class MainActivity : ComponentActivity() {

    private val app by lazy { application as DssApplication }

    private val learnerViewModel by viewModels<LearnerViewModel> {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LearnerViewModel(
                    scheduleRepository = app.scheduleRepository,
                    taskRepository = app.taskRepository,
                    ttsHelper = app.ttsHelper
                ) as T
            }
        }
    }

    private val staffViewModel by viewModels<StaffViewModel> {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StaffViewModel(
                    taskRepository = app.taskRepository,
                    scheduleRepository = app.scheduleRepository,
                    securityManager = app.securityManager,
                    backupRestoreManager = app.backupRestoreManager
                ) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // Enable Immersive Sticky Mode (hide status & navigation bars for kiosk experience)
        hideSystemBars()

        setContent {
            DssWorkstationTheme {
                var isStaffDashboardOpen by remember { mutableStateOf(false) }
                var isPinDialogOpen by remember { mutableStateOf(false) }

                Surface(modifier = Modifier.fillMaxSize()) {
                    if (isStaffDashboardOpen) {
                        StaffDashboardScreen(
                            viewModel = staffViewModel,
                            ttsHelper = app.ttsHelper,
                            onExitStaff = { isStaffDashboardOpen = false }
                        )
                    } else {
                        LearnerScreen(
                            viewModel = learnerViewModel,
                            onOpenStaff = { isPinDialogOpen = true }
                        )
                    }

                    // PIN Verification Pad Dialog
                    if (isPinDialogOpen) {
                        StaffPinDialog(
                            onPinEntered = { enteredPin ->
                                val isValid = app.securityManager.verifyPin(enteredPin)
                                if (isValid) {
                                    isPinDialogOpen = false
                                    isStaffDashboardOpen = true
                                }
                                isValid
                            },
                            onDismiss = { isPinDialogOpen = false }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}
