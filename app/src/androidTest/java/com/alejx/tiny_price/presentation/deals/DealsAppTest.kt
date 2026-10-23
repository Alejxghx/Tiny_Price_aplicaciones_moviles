package com.alejx.tiny_price.presentation.deals

import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.test.junit4.createComposeRule
import com.alejx.tiny_price.data.repository.PreviewDealsRepository
import com.alejx.tiny_price.ui.theme.TinyPriceTheme
import org.junit.Rule
import org.junit.Test

class DealsAppTest {
    @get:Rule val composeRule = createComposeRule()

    private fun render(state: DealsUiState = DealsUiState()) {
        composeRule.setContent { TinyPriceTheme { DealsApp(state = state, onEvent = {}) } }
    }

    private fun capture(name: String) {
        composeRule.waitForIdle()
        if (android.os.Build.VERSION.SDK_INT < 29) return
        val context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        val values = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "$name.png")
            put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TinyPrice")
        }
        val uri = context.contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
        context.contentResolver.openOutputStream(uri)!!.use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }

    }

    @Test fun startScreenShowsSearchAndSuggestion() {
        render()
        composeRule.onNodeWithText("Buscar").assertIsDisplayed()
        composeRule.onNodeWithText("Nintendo Switch OLED").assertIsDisplayed()
        capture("inicio")
    }

    @Test fun suggestionOpensResultsAndDetailThenReturns() {
        val viewModel = DealsViewModel(PreviewDealsRepository)
        composeRule.setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            TinyPriceTheme {
                DealsApp(state, viewModel::onEvent)
            }
        }
        composeRule.onNodeWithText("Nintendo Switch OLED").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("3 resultados de demostración").assertIsDisplayed()
        composeRule.onNodeWithText("Nintendo Switch OLED · Blanca").performClick()
        composeRule.onNodeWithText("Detalle de oferta").assertIsDisplayed()
        composeRule.onNodeWithText("‹  Volver").performClick()
        composeRule.onNodeWithText("3 resultados de demostración").assertIsDisplayed()
    }

    @Test fun loadingHasAnAccessibleProgressIndicator() {
        render(DealsUiState(hasSearched = true, isLoading = true))
        composeRule.onNodeWithTag("loading").assertIsDisplayed()
        composeRule.onNodeWithText("Buscando ofertas…").assertIsDisplayed()
        capture("carga")
    }

    @Test fun emptySearchHasRetry() {
        render(DealsUiState(hasSearched = true, searchedQuery = "unknown"))
        composeRule.onNodeWithText("No encontramos ofertas para “unknown”").assertIsDisplayed()
        composeRule.onNodeWithText("Reintentar").assertIsDisplayed()
        capture("vacio")
    }

    @Test fun errorMessageIsDisplayed() {
        render(DealsUiState(hasSearched = true, errorMessage = "Sin servicio"))
        composeRule.onNodeWithText("Sin servicio").assertIsDisplayed()
        capture("error")
    }

    @Test fun keyboardSearchValidatesEmptyInput() {
        val viewModel = DealsViewModel(PreviewDealsRepository)
        composeRule.setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            TinyPriceTheme {
                DealsApp(state, viewModel::onEvent)
            }
        }
        composeRule.onNodeWithTag("search-input").performImeAction()
        composeRule.onNodeWithText("Escribe un producto para iniciar la búsqueda.").assertIsDisplayed()
        capture("validacion")
    }
}
