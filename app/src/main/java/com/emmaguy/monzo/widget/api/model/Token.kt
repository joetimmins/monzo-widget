package com.emmaguy.monzo.widget.api.model

import com.squareup.moshi.Json

data class Token(
        @Json(name = "access_token") val accessToken: String,
        @Json(name = "refresh_token") val refreshToken: String,
        @Json(name = "token_type") val tokenType: String
)