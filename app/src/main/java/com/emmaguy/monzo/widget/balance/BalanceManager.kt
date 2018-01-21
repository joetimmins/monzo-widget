package com.emmaguy.monzo.widget.balance

import com.emmaguy.monzo.widget.UserStorage
import com.emmaguy.monzo.widget.api.MonzoApi
import com.emmaguy.monzo.widget.api.model.AccountType
import io.reactivex.Completable

class BalanceManager(
        private val monzoApi: MonzoApi,
        private val userStorage: UserStorage
) {
    fun refreshBalances(): Completable {
        return Completable.defer { requestBalanceForAccount(AccountType.PREPAID) }
                .andThen(requestBalanceForAccount(AccountType.CURRENT_ACCOUNT))
    }

    private fun requestBalanceForAccount(type: AccountType): Completable {
        val accountId = if (type == AccountType.PREPAID) userStorage.prepaidAccountId else userStorage.currentAccountId
        return if (accountId != null) {
            monzoApi.balance(accountId)
                    .doOnSuccess { balance ->
                        if (type == AccountType.PREPAID) {
                            userStorage.prepaidBalance = balance
                        } else {
                            userStorage.currentAccountBalance = balance
                        }
                    }.toCompletable()
        } else {
            Completable.complete()
        }
    }
}