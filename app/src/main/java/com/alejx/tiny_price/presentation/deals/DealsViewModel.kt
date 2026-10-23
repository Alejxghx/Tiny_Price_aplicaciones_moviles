package com.alejx.tiny_price.presentation.deals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejx.tiny_price.data.model.ProductOffer
import com.alejx.tiny_price.data.repository.DealsRepository
import com.alejx.tiny_price.data.repository.DealsRepositoryProvider
import com.alejx.tiny_price.data.repository.RemoteSearchException
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DealsUiState(
    val query: String = "",
    val searchedQuery: String = "",
    val offers: List<ProductOffer> = emptyList(),
    val hasSearched: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPreviewData: Boolean = true,
    val selectedOffer: ProductOffer? = null,
)

sealed interface DealsEvent {
    data class QueryChanged(val value: String) : DealsEvent
    data object SubmitSearch : DealsEvent
    data class UseSuggestion(val value: String) : DealsEvent
    data class OpenOffer(val offer: ProductOffer) : DealsEvent
    data object Back : DealsEvent
}

class DealsViewModel(
    private val repository: DealsRepository = DealsRepositoryProvider.create(),
) : ViewModel() {
    private var searchJob: Job? = null
    private val _state = MutableStateFlow(DealsUiState(isPreviewData = repository.isPreviewData))
    val state: StateFlow<DealsUiState> = _state.asStateFlow()

    fun onEvent(event: DealsEvent) {
        when (event) {
            is DealsEvent.QueryChanged -> _state.update { it.copy(query = event.value, selectedOffer = null, errorMessage = null) }
            DealsEvent.SubmitSearch -> search(_state.value.query)
            is DealsEvent.UseSuggestion -> {
                _state.update { it.copy(query = event.value, selectedOffer = null, errorMessage = null) }
                search(event.value)
            }
            is DealsEvent.OpenOffer -> _state.update { it.copy(selectedOffer = event.offer) }
            DealsEvent.Back -> _state.update { it.copy(selectedOffer = null) }
        }
    }

    private fun search(query: String) {
        searchJob?.cancel()
        val normalized = query.trim()
        if (normalized.length !in 2..200) {
            _state.update {
                it.copy(
                    hasSearched = true,
                    isLoading = false,
                    selectedOffer = null,
                    offers = emptyList(),
                    errorMessage = if (normalized.isEmpty()) "Escribe un producto para iniciar la búsqueda."
                    else "La búsqueda debe tener entre 2 y 200 caracteres.",
                )
            }
            return
        }

        searchJob = viewModelScope.launch {
            _state.update { it.copy(query = normalized, searchedQuery = normalized, offers = emptyList(), isLoading = true, hasSearched = true, selectedOffer = null, errorMessage = null) }
            try {
                val offers = repository.search(normalized)
                _state.update { it.copy(offers = offers, isLoading = false) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: RemoteSearchException) {
                _state.update { it.copy(offers = emptyList(), isLoading = false, errorMessage = exception.message) }
            }
        }
    }
}
