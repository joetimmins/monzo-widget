package com.emmaguy.monzo.widget.login

import com.emmaguy.monzo.widget.api.MonzoApi
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable


class LastTransactionPresenter(
        private val monzoApi: MonzoApi,
        private val lastTransactionEventListener: LastTransactionEventListener,
        private val lastTransactionView: LastTransactionView,
        private val uiScheduler: Scheduler,
        private val ioScheduler: Scheduler
) {
    private val disposables = CompositeDisposable()

    init {
        disposables.add(lastTransactionEventListener.lastTransactionClicks()
                .subscribe {
                    // get the account id out of user storage
                    monzoApi.transactions()
                }
        )
    }

    interface LastTransactionView {
        fun showLastTransaction()
    }

    interface LastTransactionEventListener {
        fun lastTransactionClicks(): Observable<Unit>
        fun shareClicks(): Observable<Unit>
    }
}
