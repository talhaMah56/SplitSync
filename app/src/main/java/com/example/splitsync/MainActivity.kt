package com.example.splitsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.splitsync.ui.theme.SplitSyncTheme
import com.example.splitsync.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplitSyncTheme {
                AppNavigation()
            }
        }
    }
}