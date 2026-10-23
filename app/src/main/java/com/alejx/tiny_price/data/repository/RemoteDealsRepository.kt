package com.alejx.tiny_price.data.repository

import com.alejx.tiny_price.BuildConfig
import com.alejx.tiny_price.data.model.ProductOffer
import com.alejx.tiny_price.data.remote.ApiErrorResponse
import com.alejx.tiny_price.data.remote.SearchProductsRequest
import com.alejx.tiny_price.data.remote.TinyPriceApi
import com.alejx.tiny_price.data.remote.TinyPriceApiFactory
import com.alejx.tiny_price.data.remote.toProductOffer
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class RemoteDealsRepository(
    private val api: TinyPriceApi = TinyPriceApiFactory.create(),
    private val gson: Gson = Gson(),
) : DealsRepository {
    override val isPreviewData = false

    override suspend fun search(query: String): List<ProductOffer> = withContext(Dispatchers.IO) {
        try {
            val response = api.searchProducts(SearchProductsRequest(query))
            if (!response.isSuccessful) throw RemoteSearchException(response.errorMessage())
            val body = response.body() ?: throw RemoteSearchException("El servidor devolvió una respuesta vacía.")
            body.offers.take(10).map { it.toProductOffer() }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: RemoteSearchException) {
            throw error
        } catch (_: Exception) {
            throw RemoteSearchException(
                "No se pudo conectar con la búsqueda en vivo. Revisa tu conexión e inténtalo de nuevo.",
            )
        }
    }

    private fun Response<*>.errorMessage(): String = runCatching {
        errorBody()?.charStream()?.use { reader ->
            gson.fromJson(reader, ApiErrorResponse::class.java)?.error
        }
    }.getOrNull().orEmpty().ifBlank {
        "No fue posible hacer la búsqueda en vivo. Inténtalo de nuevo."
    }
}

class RemoteSearchException(message: String) : Exception(message)

object DealsRepositoryProvider {
    fun create(): DealsRepository = if (BuildConfig.USE_REMOTE_DEALS) {
        RemoteDealsRepository()
    } else {
        PreviewDealsRepository
    }
}
