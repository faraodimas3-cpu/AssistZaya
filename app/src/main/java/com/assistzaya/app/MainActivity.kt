package com.assistzaya.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.assistzaya.app.ui.AppRoot
import com.assistzaya.app.ui.theme.AssistZayaTheme

// AppCompatActivity é necessário para trocar o idioma do app em tempo real (AppCompatDelegate).
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AssistZayaTheme { AppRoot() }
        }
    }
}
