package com.emmaguy.monzo.widget.api

import com.emmaguy.monzo.widget.api.model.AccountsResponse
import com.emmaguy.monzo.widget.api.model.Balance
import com.emmaguy.monzo.widget.api.model.Token
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.*

interface MonzoApi {
    @FormUrlEncoded
    @POST("oauth2/token")
    fun requestAccessToken(
            @Field("client_id") clientId: String,
            @Field("client_secret") clientSecret: String,
            @Field("redirect_uri") redirectUri: String,
            @Field("code") code: String,
            @Field("grant_type") grantType: String = "authorization_code"
    ): Single<Token>

    @FormUrlEncoded
    @POST("oauth2/token")
    fun refreshToken(
            @Field("client_id") clientId: String,
            @Field("client_secret") clientSecret: String,
            @Field("refresh_token") refreshToken: String,
            @Field("grant_type") grantType: String = "refresh_token"
    ): Call<Token>

    @GET("accounts")
    fun accounts(@Query("account_type") type: String): Single<AccountsResponse>

    @GET("balance")
    fun balance(@Query("account_id") accountId: String): Single<Balance>
}
