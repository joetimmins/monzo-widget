package com.emmaguy.monzo.widget.api.model

import com.squareup.moshi.Json

enum class AccountType(val key: String) {
    @Json(name = "uk_prepaid") PREPAID("prepaid"),
    @Json(name = "uk_retail") CURRENT_ACCOUNT("retail");

    companion object {
        fun find(key: String?): AccountType? {
            return AccountType.values().firstOrNull { it.key == key }
        }
    }
}