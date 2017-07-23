package com.emmaguy.monzo.widget.balance

import com.emmaguy.monzo.widget.UserStorage
import com.emmaguy.monzo.widget.api.MonzoApi
import io.reactivex.Completable
import io.reactivex.Single

class BalanceManager(
        private val monzoApi: MonzoApi,
        private val userStorage: UserStorage
) {
    fun refreshBalances(): Completable {
        return Single.just(Unit)
                .flatMap {
                    val accountId = userStorage.prepaidAccountId
                    if (accountId != null) {
                        monzoApi.balance(accountId).doOnSuccess { balance -> userStorage.prepaidBalance = balance }
                    } else {
                        Single.just(Unit)
                    }
                }
                .flatMap {
                    val accountId = userStorage.currentAccountId
                    if (accountId != null) {
                        monzoApi.balance(accountId).doOnSuccess { balance -> userStorage.currentAccountBalance = balance }
                    } else {
                        Single.just(Unit)
                    }
                }
                .toCompletable()
    }
}