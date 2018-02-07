package com.emmaguy.monzo.widget.api

import android.content.Context
import com.emmaguy.monzo.widget.BuildConfig
import com.emmaguy.monzo.widget.StorageModule
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.readystatesoftware.chuck.ChuckInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.Rfc3339DateJsonAdapter
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.io.IOException
import java.util.*

class ApiModule(
        private val context: Context,
        private val storageModule: StorageModule
) {
    val monzoApi = createMonzoApi()
    val clientId = BuildConfig.CLIENT_ID
    val clientSecret = BuildConfig.CLIENT_SECRET

    private fun createMonzoApi(): MonzoApi {
        val baseHttpClient = OkHttpClient.Builder()
                .addInterceptor(ChuckInterceptor(context))
                .build()

        val userStorage = storageModule.userStorage
        val client = baseHttpClient
                .newBuilder()
                .addInterceptor { chain ->
                    val original = chain.request()

                    val requestBuilder = original.newBuilder()
                            .header("Accept", "application/json")
                            .header("Content-type", "application/json")

                    if (userStorage.hasToken()) {
                        requestBuilder.header("Authorization", userStorage.getTokenType() + " " + userStorage.getAccessToken())
                    }

                    chain.proceed(requestBuilder.build())
                }
                .authenticator({ _, response ->
                    synchronized(this) {
                        // Make a new instance to avoid making another call with our expired access token
                        val monzoApi = createMonzoApi(baseHttpClient)
                        val call = monzoApi.refreshToken(clientId, clientSecret, userStorage.getRefreshToken()!!)
                        try {
                            val tokenResponse = call.execute()
                            if (tokenResponse.code() == 200) {
                                val newToken = tokenResponse.body()
                                userStorage.saveToken(newToken)

                                response.request().newBuilder()
                                        .header("Authorization", newToken.tokenType + " " + newToken.accessToken)
                                        .build()
                            }
                        } catch (e: IOException) {
                            Timber.e(e, "Exception whilst trying to refresh token")
                        }
                        null
                    }
                })
                .build()

        return createMonzoApi(client)
    }

    private fun createMonzoApi(okHttpClient: OkHttpClient): MonzoApi {
        val build = Moshi.Builder()
                .add(Date::class.java, Rfc3339DateJsonAdapter())
                .build()
        return Retrofit.Builder()
                .baseUrl("https://api.monzo.com")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create(build))
                .client(okHttpClient)
                .build()
                .create(MonzoApi::class.java)
    }
}
