package com.alejx.tiny_price.data.session

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface SessionProvider {
    suspend fun accessToken(): String
    suspend fun invalidate()
    suspend fun deleteAccount()
}

data class SavedSession(val accessToken: String, val refreshToken: String, val expiresAt: Long)
interface SessionStore {
    fun read(): SavedSession?
    fun write(session: SavedSession?)
}

data class AuthTokens(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("expires_in") val expiresIn: Long = 3600,
)
data class RefreshRequest(@SerializedName("refresh_token") val refreshToken: String)

interface SessionApi {
    @POST("signup")
    suspend fun anonymous(@Body body: Map<String, Any> = emptyMap()): Response<AuthTokens>
    @POST("token")
    suspend fun refresh(@Query("grant_type") grantType: String = "refresh_token", @Body body: RefreshRequest): Response<AuthTokens>
}

class SearchSession(
    private val api: SessionApi,
    private val store: SessionStore,
    private val deleteRemote: suspend (String) -> Unit,
    private val now: () -> Long = System::currentTimeMillis,
) : SessionProvider {
    private val mutex = Mutex()
    override suspend fun accessToken(): String = mutex.withLock {
        val saved = store.read()
        if (saved != null && saved.expiresAt > now() + 60_000) return@withLock saved.accessToken
        val response = if (saved != null) {
            val refreshed = api.refresh(body = RefreshRequest(saved.refreshToken))
            if (refreshed.code() in listOf(400, 401, 403)) {
                refreshed.errorBody()?.close()
                store.write(null)
                api.anonymous()
            } else refreshed
        } else api.anonymous()
        if (!response.isSuccessful) {
            response.errorBody()?.close()
            throw java.io.IOException("No fue posible iniciar una sesión de búsqueda. Inténtalo más tarde.")
        }
        val tokens = response.body()
        val access = tokens?.accessToken?.takeIf(String::isNotBlank)
        val refresh = tokens?.refreshToken?.takeIf(String::isNotBlank)
        if (access == null || refresh == null) throw java.io.IOException("La sesión recibida no es válida.")
        val session = SavedSession(access, refresh, now() + tokens.expiresIn.coerceAtLeast(0) * 1000)
        store.write(session)
        session.accessToken
    }

    override suspend fun invalidate() = mutex.withLock { store.write(null) }

    override suspend fun deleteAccount() {
        if (store.read() == null) return
        val token = accessToken()
        mutex.withLock {
            deleteRemote(token)
            store.write(null)
        }
    }
}
