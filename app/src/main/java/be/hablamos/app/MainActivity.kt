package be.hablamos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import be.hablamos.app.ui.theme.HablamosTheme
import be.hablamos.app.ui.v4.HablamosV4Shell

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HablamosTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HablamosV4Shell()
                }
            }
        }
    }
}
