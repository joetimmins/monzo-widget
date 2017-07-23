package com.emmaguy.monzo.widget.login

import android.content.Context
import com.emmaguy.monzo.widget.AppModule
import com.emmaguy.monzo.widget.BuildConfig
import com.emmaguy.monzo.widget.R
import com.emmaguy.monzo.widget.UserStorage
import com.emmaguy.monzo.widget.api.MonzoApi
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.readystatesoftware.chuck.ChuckInterceptor
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.io.IOException

class LoginModule(private val context: Context) {
    val userStorage = UserStorage(context)

    fun provideLoginPresenter(): LoginPresenter {
        return LoginPresenter(provideMonzoApi(),
                AppModule.uiScheduler(),
                AppModule.ioScheduler(),
                provideClientId(),
                provideClientSecret(),
                provideRedirectUri(),
                userStorage)
    }

    fun provideMonzoApi(): MonzoApi {
        val baseHttpClient = OkHttpClient.Builder()
                .addInterceptor(ChuckInterceptor(context))
                .build()

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
                        val call = monzoApi.refreshToken(provideClientId(), provideClientSecret(), userStorage.getRefreshToken()!!)
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
        return Retrofit.Builder()
                .baseUrl("https://api.monzo.com")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
                .client(okHttpClient)
                .build()
                .create(MonzoApi::class.java)
    }

    private fun provideClientId(): String {
        return BuildConfig.CLIENT_ID
    }

    private fun provideClientSecret(): String {
        return BuildConfig.CLIENT_SECRET
    }

    private fun provideRedirectUri(): String {
        return context.getString(R.string.callback_url_scheme) + "://" + context.getString(R.string.callback_url_host)
    }
}