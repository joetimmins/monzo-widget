package com.emmaguy.monzo.widget.balance

import com.emmaguy.monzo.widget.UserStorage
import com.emmaguy.monzo.widget.api.MonzoApi
import com.emmaguy.monzo.widget.api.model.AccountType
import com.emmaguy.monzo.widget.api.model.Balance
import io.reactivex.Completable

class BalanceManager(
        private val monzoApi: MonzoApi,
        private val userStorage: UserStorage
) {
    fun refreshBalances(): Completable {
        return Completable.defer { requestBalanceForAccount(AccountType.PREPAID, { userStorage.prepaidBalance = it }) }
                .andThen(requestBalanceForAccount(AccountType.CURRENT_ACCOUNT, { userStorage.currentAccountBalance = it }))
    }

    private fun requestBalanceForAccount(accountType: AccountType, balanceWriter: (Balance?) -> Unit): Completable {
        return accountIdFor(accountType)?.let { accountId ->
            monzoApi.balance(accountId)
                    .doOnSuccess { balance: Balance? ->
                        balanceWriter.invoke(balance)
                    }
                    .toCompletable()
        } ?: Completable.complete()
    }

    private fun accountIdFor(type: AccountType): String? =
            if (type == AccountType.PREPAID) userStorage.prepaidAccountId else userStorage.currentAccountId
}
