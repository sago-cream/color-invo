package dev.hsichen.colorinvo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.hsichen.colorinvo.ui.CarrierEditorScreen
import dev.hsichen.colorinvo.ui.ColorInvoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ColorInvoTheme { CarrierEditorScreen() } }
    }
}
