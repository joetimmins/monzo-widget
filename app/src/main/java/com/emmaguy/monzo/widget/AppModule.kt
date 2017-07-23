package com.emmaguy.monzo.widget

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

object AppModule {
    fun uiScheduler(): Scheduler {
        return AndroidSchedulers.mainThread()
    }

    fun ioScheduler(): Scheduler {
        return Schedulers.io()
    }
}