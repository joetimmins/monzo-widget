package com.emmaguy.monzo.widget.api.model

import com.squareup.moshi.Json

data class Account(
        val id: String,
        val type: AccountType,
        @Json(name = "sort_code") val sortCode: String? = null,
        @Json(name = "account_number") val accountNumber: String? = null
)