package com.example.safemotion

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import com.example.safemotion.navigation.AppNavigation
import com.example.safemotion.ui.theme.SafeMotionTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    private val permissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    private var permissionRequested = false
    private val authListener = FirebaseAuth.AuthStateListener { auth ->
        if (auth.currentUser != null && !permissionRequested &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            permissionRequested = true
            permissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Firebase.auth.addAuthStateListener(authListener)
        setContent {
            SafeMotionTheme {
                AppNavigation()
            }
        }
    }

    override fun onDestroy() {
        Firebase.auth.removeAuthStateListener(authListener)
        super.onDestroy()
    }
}
