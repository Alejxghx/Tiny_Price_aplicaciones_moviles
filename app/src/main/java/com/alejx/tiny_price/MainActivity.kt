package com.alejx.tiny_price

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alejx.tiny_price.presentation.deals.DealsApp
import com.alejx.tiny_price.presentation.deals.DealsViewModel
import com.alejx.tiny_price.ui.theme.TinyPriceTheme

class MainActivity : ComponentActivity() {
    private val viewModel: DealsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            TinyPriceTheme {
                DealsApp(state = state, onEvent = viewModel::onEvent)
            }
        }
    }
}
