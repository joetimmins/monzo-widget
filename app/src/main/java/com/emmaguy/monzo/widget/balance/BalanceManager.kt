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
                .andThen(requestTransactionsForAccount(AccountType.PREPAID))
                .andThen(requestTransactionsForAccount(AccountType.CURRENT_ACCOUNT))
    }

    private fun requestBalanceForAccount(accountType: AccountType, balanceWriter: (Balance?) -> Unit): Completable {
        val apiCall: (String) -> Completable = { accountId ->
            monzoApi.balance(accountId)
                    .doOnSuccess { balance: Balance? ->
                        balanceWriter.invoke(balance)
                    }
                    .toCompletable()
        }
        return apiCallAsCompletable(accountType, apiCall)
    }

    private fun requestTransactionsForAccount(accountType: AccountType): Completable {
        val apiCall: (String) -> Completable = { accountId ->
            monzoApi.transactions(accountId).toCompletable()
        }
        return apiCallAsCompletable(accountType, apiCall)
    }

    private fun apiCallAsCompletable(accountType: AccountType, apiCall: (String) -> Completable) =
            accountIdFor(accountType)?.let(apiCall) ?: Completable.complete()

    private fun accountIdFor(type: AccountType): String? =
            if (type == AccountType.PREPAID) userStorage.prepaidAccountId else userStorage.currentAccountId
}
