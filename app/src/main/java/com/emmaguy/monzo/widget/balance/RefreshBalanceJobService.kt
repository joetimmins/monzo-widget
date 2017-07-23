package com.emmaguy.monzo.widget.balance


import android.app.job.JobParameters
import android.app.job.JobService
import com.emmaguy.monzo.widget.AppModule
import com.emmaguy.monzo.widget.MonzoWidgetApp
import com.emmaguy.monzo.widget.login.LoginModule
import io.reactivex.disposables.Disposable
import timber.log.Timber

class RefreshBalanceJobService : JobService() {
    private lateinit var loginModule: LoginModule
    private var disposable: Disposable? = null

    override fun onCreate() {
        super.onCreate()

        loginModule = MonzoWidgetApp.get(this).loginModule
    }

    override fun onStartJob(jobParams: JobParameters?): Boolean {
        val monzoApi = loginModule.provideMonzoApi()
        val userStorage = loginModule.userStorage
        val ioScheduler = AppModule.ioScheduler()

        Timber.d("Refreshing balance")
        disposable = monzoApi.balance(userStorage.prepaidAccountId!!)
                .doOnSuccess { balance -> userStorage.prepaidBalance = balance }
                .flatMap { monzoApi.balance(userStorage.currentAccountId!!) }
                .doOnSuccess { balance -> userStorage.currentAccountBalance = balance }
                .subscribeOn(ioScheduler)
                .subscribe({ balance ->
                    Timber.d("Successfully refreshed balances: " + balance)
                    jobFinished(jobParams, false)
                    BalanceWidgetProvider.updateAllWidgets(this)
                }, { error -> Timber.e(error, "Failed to refresh balance") })
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