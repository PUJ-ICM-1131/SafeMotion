package com.example.safemotion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.safemotion.navigation.AppNavigation
import com.example.safemotion.ui.theme.SafeMotionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SafeMotionTheme {
                AppNavigation()
            }
        }
    }
}
