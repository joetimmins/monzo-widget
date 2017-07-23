package com.emmaguy.monzo.widget.balance


import android.app.job.JobParameters
import android.app.job.JobService
import com.emmaguy.monzo.widget.AppModule
import com.emmaguy.monzo.widget.MonzoWidgetApp
import io.reactivex.disposables.Disposable
import timber.log.Timber

class RefreshBalanceJobService : JobService() {
    private lateinit var balanceManager: BalanceManager
    private var disposable: Disposable? = null

    override fun onCreate() {
        super.onCreate()

        val app = MonzoWidgetApp.get(this)
        balanceManager = BalanceManager(app.apiModule.monzoApi, app.storageModule.userStorage)
    }

    override fun onStartJob(jobParams: JobParameters?): Boolean {
        disposable = balanceManager.refreshBalances()
                .subscribeOn(AppModule.ioScheduler())
                .subscribe({
                    Timber.d("Successfully refreshed balance(s)")
                    jobFinished(jobParams, false)
                    BalanceWidgetProvider.updateAllWidgets(this)
                }, { error ->
                    jobFinished(jobParams, false)
                    Timber.e(error, "Failed to refresh balance(s)")
                })
        return true
    }

    override fun onStopJob(jobParams: JobParameters?): Boolean {
        if (disposable?.isDisposed ?: false) {
            disposable?.dispose()
        }
        return true
    }

    override fun onDestroy() {
        if (disposable?.isDisposed ?: false) {
            disposable?.dispose()
        }
        super.onDestroy()
    }
}