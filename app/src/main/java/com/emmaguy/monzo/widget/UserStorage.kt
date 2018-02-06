package com.emmaguy.monzo.widget

import android.content.Context
import android.content.SharedPreferences
import com.emmaguy.monzo.widget.api.model.AccountType
import com.emmaguy.monzo.widget.api.model.Balance
import com.emmaguy.monzo.widget.api.model.Token

class UserStorage(context: Context) {
    private companion object {
        private const val KEY_REFRESH_TOKEN = "KEY_REFRESH_TOKEN"
        private const val KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN"
        private const val KEY_TOKEN_TYPE = "KEY_TOKEN_TYPE"

        private const val KEY_STATE = "KEY_STATE"

        private const val KEY_PREPAID_ACCOUNT_ID = "KEY_PREPAID_ACCOUNT_ID"
        private const val KEY_CURRENT_ACCOUNT_ID = "KEY_CURRENT_ACCOUNT_ID"

        private const val KEY_PREPAID_CURRENCY = "KEY_PREPAID_CURRENCY"
        private const val KEY_PREPAID_BALANCE = "KEY_PREPAID_BALANCE"

        private const val KEY_CA_CURRENCY = "KEY_CA_CURRENCY"
        private const val KEY_CA_BALANCE = "KEY_CA_BALANCE"

        private const val KEY_ACCOUNT_TYPE = "KEY_ACCOUNT_TYPE"

        private const val KEY_MONZO_ME_LINK: String = "KEY_MONZO_ME_LINK"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_storage", Context.MODE_PRIVATE)

    var prepaidBalance: Balance?
        get() {
            val currency = sharedPreferences.getString(KEY_PREPAID_CURRENCY, null)
            val balance = sharedPreferences.getLong(KEY_PREPAID_BALANCE, 0)
            if (currency == null) return null

            return Balance(balance, currency)
        }
        set(balance) {
            sharedPreferences
                    .edit()
                    .putString(KEY_PREPAID_CURRENCY, balance?.currency)
                    .putLong(KEY_PREPAID_BALANCE, balance?.balance ?: 0)
                    .apply()
        }

    var currentAccountBalance: Balance?
        get() {
            val currency = sharedPreferences.getString(KEY_CA_CURRENCY, null)
            val balance = sharedPreferences.getLong(KEY_CA_BALANCE, 0)
            if (currency == null) return null

            return Balance(balance, currency)
        }
        set(balance) {
            sharedPreferences
                    .edit()
                    .putString(KEY_CA_CURRENCY, balance?.currency)
                    .putLong(KEY_CA_BALANCE, balance?.balance ?: 0)
                    .apply()
        }

    var state: String?
        get() = sharedPreferences.getString(KEY_STATE, null)
        set(state) {
            sharedPreferences.edit().putString(KEY_STATE, state).apply()
        }

    var prepaidAccountId: String?
        get() = sharedPreferences.getString(KEY_PREPAID_ACCOUNT_ID, null)
        set(id) {
            sharedPreferences.edit().putString(KEY_PREPAID_ACCOUNT_ID, id).apply()
        }

    var currentAccountId: String?
        get() = sharedPreferences.getString(KEY_CURRENT_ACCOUNT_ID, null)
        set(id) {
            sharedPreferences.edit().putString(KEY_CURRENT_ACCOUNT_ID, id).apply()
        }

    fun saveAccountType(widgetId: Int, accountType: AccountType) {
        sharedPreferences
                .edit()
                .putString(KEY_ACCOUNT_TYPE + widgetId, accountType.key)
                .apply()
    }

    /**
     * The account type for the given widget. Defaults to current account
     */
    fun accountType(widgetId: Int): AccountType {
        val savedAccountType = sharedPreferences.getString(KEY_ACCOUNT_TYPE + widgetId, null)
        return AccountType.find(savedAccountType) ?: AccountType.CURRENT_ACCOUNT
    }

    fun removeAccountType(widgetId: Int) {
        return sharedPreferences
                .edit()
                .remove(KEY_ACCOUNT_TYPE + widgetId)
                .apply()
    }

    fun saveToken(token: Token) {
        sharedPreferences
                .edit()
                .putString(KEY_REFRESH_TOKEN, token.refreshToken)
                .putString(KEY_ACCESS_TOKEN, token.accessToken)
                .putString(KEY_TOKEN_TYPE, token.tokenType)
                .apply()
    }

    fun hasToken(): Boolean {
        return getAccessToken() != null && getRefreshToken() != null
    }

    fun getTokenType(): String? {
        return sharedPreferences.getString(KEY_TOKEN_TYPE, null)
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    fun saveMonzoMeLink(monzoMeLink: String) {
        sharedPreferences
                .edit()
                .putString(KEY_MONZO_ME_LINK, monzoMeLink)
                .apply()
    }
}
