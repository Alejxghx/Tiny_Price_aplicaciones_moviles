package com.alejx.tiny_price.data.session

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class SearchSessionTest {
    private class MemoryStore(var session: SavedSession? = null) : SessionStore {
        override fun read() = session
        override fun write(session: SavedSession?) { this.session = session }
    }
    private class Api : SessionApi {
        var signups = 0
        var refreshes = 0
        var refreshStatus = 200
        override suspend fun anonymous(body: Map<String, Any>): Response<AuthTokens> {
            signups++
            delay(1)
            return Response.success(AuthTokens("guest-access", "guest-refresh", 3600))
        }
        override suspend fun refresh(grantType: String, body: RefreshRequest): Response<AuthTokens> {
            refreshes++
            assertEquals("refresh_token", grantType)
            return if (refreshStatus == 200) Response.success(AuthTokens("renewed-access", "renewed-refresh", 3600))
            else Response.error(refreshStatus, "{}".toResponseBody("application/json".toMediaType()))
        }
    }

    @Test fun cachedSessionAvoidsCreatingAnotherUser() = runTest {
        val api = Api()
        val store = MemoryStore(SavedSession("cached", "refresh", 100_000))
        val session = SearchSession(api, store, {}, now = { 10_000 })
        assertEquals("cached", session.accessToken())
        assertEquals(0, api.signups)
        assertEquals(0, api.refreshes)
    }
    @Test fun expiredTokenIsRenewedAndSaved() = runTest {
        val api = Api()
        val store = MemoryStore(SavedSession("expired", "refresh", 0))
        val session = SearchSession(api, store, {}, now = { 10_000 })
        assertEquals("renewed-access", session.accessToken())
        assertEquals("renewed-refresh", store.session?.refreshToken)
        assertEquals(1, api.refreshes)
        assertEquals(0, api.signups)
    }
    @Test fun invalidRefreshStartsOneNewGuestSession() = runTest {
        val api = Api().apply { refreshStatus = 400 }
        val store = MemoryStore(SavedSession("expired", "refresh", 0))
        val session = SearchSession(api, store, {}, now = { 10_000 })
        assertEquals("guest-access", session.accessToken())
        assertEquals(1, api.signups)
    }
    @Test fun temporaryAuthFailureDoesNotDiscardRefreshOrCreateUsers() = runTest {
        val api = Api().apply { refreshStatus = 503 }
        val old = SavedSession("expired", "refresh", 0)
        val store = MemoryStore(old)
        val session = SearchSession(api, store, {}, now = { 10_000 })
        try { session.accessToken(); fail("Expected failure") } catch (_: java.io.IOException) { }
        assertEquals(old, store.session)
        assertEquals(0, api.signups)
    }
    @Test fun simultaneousSearchesReuseOneGuestSession() = runTest {
        val api = Api()
        val session = SearchSession(api, MemoryStore(), {}, now = { 10_000 })
        val tokens = coroutineScope { List(3) { async { session.accessToken() } }.awaitAll() }
        assertEquals(listOf("guest-access", "guest-access", "guest-access"), tokens)
        assertEquals(1, api.signups)
    }
    @Test fun deletionClearsLocalSessionOnlyAfterServerSuccess() = runTest {
        val old = SavedSession("cached", "refresh", 100_000)
        val store = MemoryStore(old)
        var failDeletion = true
        val session = SearchSession(Api(), store, { token ->
            assertEquals("cached", token)
            if (failDeletion) throw java.io.IOException("Unavailable")
        }, now = { 10_000 })
        try { session.deleteAccount(); fail("Expected failure") } catch (_: java.io.IOException) { }
        assertEquals(old, store.session)
        failDeletion = false
        session.deleteAccount()
        assertEquals(null, store.session)
    }
}
