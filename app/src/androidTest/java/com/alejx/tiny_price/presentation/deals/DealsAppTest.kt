package com.alejx.tiny_price.presentation.deals

import androidx.compose.runtime.getValue
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

    @Test fun startScreenShowsSearchAndSuggestion() {
        render()
        composeRule.onNodeWithText("Buscar").assertIsDisplayed()
        composeRule.onNodeWithText("Nintendo Switch OLED").assertIsDisplayed()
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
    }

    @Test fun emptySearchHasRetry() {
        render(DealsUiState(hasSearched = true, searchedQuery = "unknown"))
        composeRule.onNodeWithText("No encontramos ofertas para “unknown”").assertIsDisplayed()
        composeRule.onNodeWithText("Reintentar").assertIsDisplayed()
    }

    @Test fun errorMessageIsDisplayed() {
        render(DealsUiState(hasSearched = true, errorMessage = "Sin servicio"))
        composeRule.onNodeWithText("Sin servicio").assertIsDisplayed()
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
    }
}
