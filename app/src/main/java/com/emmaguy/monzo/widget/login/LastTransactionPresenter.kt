package com.emmaguy.monzo.widget.login

import com.emmaguy.monzo.widget.api.MonzoApi
import io.reactivex.Observable
import io.reactivex.Scheduler


class LastTransactionPresenter(monzoApi: MonzoApi, uiScheduler: Scheduler, ioScheduler: Scheduler) {

    interface LastTransactionView {
        fun showLastTransaction()
    }

    interface LastTransactionEventListener {
        fun lastTransactionClicks(): Observable<Unit>
        fun shareClicks(): Observable<Unit>
    }
}
