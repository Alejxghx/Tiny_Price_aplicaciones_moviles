package com.alejx.tiny_price.data.session

import android.content.Context
import com.alejx.tiny_price.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SessionRegistry {
    private lateinit var session: SessionProvider
    fun initialize(context: Context) {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor { chain -> chain.proceed(chain.request().newBuilder().header("apikey", BuildConfig.SUPABASE_PUBLISHABLE_KEY).build()) }
            .build()
        val api = Retrofit.Builder().baseUrl(BuildConfig.SUPABASE_URL + "/auth/v1/")
            .client(client).addConverterFactory(GsonConverterFactory.create()).build().create(SessionApi::class.java)
        session = SearchSession(api, EncryptedSessionStore(context.applicationContext), deleteRemote = { token ->
            withContext(Dispatchers.IO) {
                val request = Request.Builder().url(BuildConfig.EDGE_RUNTIME_BASE_URL.trimEnd('/') + "/delete-account")
                    .header("Authorization", "Bearer $token").delete().build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw java.io.IOException("No se pudieron eliminar los datos. Inténtalo más tarde.")
                }
            }
        })
    }
    fun get(): SessionProvider = session
}
