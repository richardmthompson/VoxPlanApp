package com.voxplanapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.graphics.toArgb
import com.voxplanapp.navigation.VoxPlanApp
import com.voxplanapp.ui.constants.PrimaryColor
import com.voxplanapp.ui.constants.ToolbarIconColor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set system bar colors
        window.statusBarColor = ToolbarIconColor.toArgb()
        window.navigationBarColor = ToolbarIconColor.toArgb()

        setContent {
            VoxPlanApp()
        }
    }
}
