package com.otus.securehomework.di

import androidx.viewbinding.BuildConfig
import com.otus.securehomework.data.repository.AuthRepository
import com.otus.securehomework.data.repository.TokenAuthenticator
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.data.source.network.TokenRefreshApi
import com.otus.securehomework.data.source.secure.PreferenceManager
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

private const val BASE_URL = "https://auth.tragltech.com/otus/api/"

class RemoteDataSource @Inject constructor(
    private val _preferenceManager: PreferenceManager
) {

    fun <Api> buildApi(
        api: Class<Api>,
    ): Api {
        val authenticator = TokenAuthenticator(buildTokenApi(), _preferenceManager)
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getRetrofitClient(authenticator))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(api)
    }

    private fun buildTokenApi(): TokenRefreshApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getRetrofitClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TokenRefreshApi::class.java)
    }

    private fun getRetrofitClient(authenticator: Authenticator? = null): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder().also {
                    it.addHeader("Accept", "application/json")
                }.build())
            }.also { client ->
                authenticator?.let { client.authenticator(it) }
                if (BuildConfig.DEBUG) {
                    val logging = HttpLoggingInterceptor()
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                    client.addInterceptor(logging)
                }
            }.build()
    }
}