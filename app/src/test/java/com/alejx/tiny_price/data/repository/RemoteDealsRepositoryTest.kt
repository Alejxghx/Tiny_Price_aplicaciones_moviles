package com.alejx.tiny_price.data.repository

import com.alejx.tiny_price.data.remote.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class RemoteDealsRepositoryTest {
    @Test fun mapsOffersAndLimitsResponseToTen() = runTest {
        val api = api { request ->
            assertEquals("Switch", request.query)
            Response.success(SearchProductsResponse(List(12) {
                ProductOfferDto(id = "$it", productName = "Switch", sourceUrl = "https://example.com/$it")
            }))
        }
        val offers = RemoteDealsRepository(api).search("Switch")
        assertEquals(10, offers.size)
        assertEquals("Ver en fuente", offers.first().price)
        assertEquals("https://example.com/0", offers.first().sourceUrl)
    }

    @Test fun preservesControlledJsonErrors() = runTest {
        val repository = RemoteDealsRepository(api {
            Response.error(503, """{"error":"La búsqueda aún no está configurada."}""".toResponseBody("application/json".toMediaType()))
        })
        try {
            repository.search("Switch")
            fail("Expected RemoteSearchException")
        } catch (error: RemoteSearchException) {
            assertEquals("La búsqueda aún no está configurada.", error.message)
        }
    }

    @Test fun rawProviderBodiesAreNotShownToTheUser() = runTest {
        val repository = RemoteDealsRepository(api {
            Response.error(502, "private-internal-body".toResponseBody("text/plain".toMediaType()))
        })
        try {
            repository.search("Switch")
            fail("Expected RemoteSearchException")
        } catch (error: RemoteSearchException) {
            assertFalse(error.message.orEmpty().contains("private-internal-body"))
        }
    }

    @Test fun cancellationIsNeverConvertedIntoNetworkFailure() = runTest {
        val repository = RemoteDealsRepository(api { throw CancellationException("cancelled") })
        try {
            repository.search("Switch")
            fail("Expected CancellationException")
        } catch (error: CancellationException) {
            assertEquals("cancelled", error.message)
        }
    }

    @Test fun demoCanBeSearchedWithoutNetworkAndEmptyResultsAreValid() = runTest {
        assertTrue(PreviewDealsRepository.isPreviewData)
        assertEquals(3, PreviewDealsRepository.search("switch").size)
        assertTrue(PreviewDealsRepository.search("not-a-demo-product").isEmpty())
    }

    private fun api(block: suspend (SearchProductsRequest) -> Response<SearchProductsResponse>) =
        object : TinyPriceApi {
            override suspend fun searchProducts(request: SearchProductsRequest) = block(request)
        }
}
