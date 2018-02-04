package com.emmaguy.monzo.widget.settings

import com.emmaguy.monzo.widget.UserStorage
import com.emmaguy.monzo.widget.api.model.AccountType
import com.emmaguy.monzo.widget.common.BasePresenter
import com.emmaguy.monzo.widget.common.plus
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber


class SettingsPresenter(
        private val appWidgetId: Int,
        private val userStorage: UserStorage
) : BasePresenter<SettingsPresenter.SettingsView>() {
    private var disposables: CompositeDisposable = CompositeDisposable()
    private var view: BasePresenter.View? = null

    fun attachView(settingsView: SettingsView) {
        if (view !== null) {
            throw IllegalStateException("View $view has already been attached")
        }
        view = settingsView

        disposables += settingsView.currentAccountClicks()
                .doOnNext { userStorage.saveAccountType(appWidgetId, AccountType.CURRENT_ACCOUNT) }
                .subscribe({ settingsView.finish(appWidgetId) }, Timber::e)

        disposables += settingsView.prepaidClicks()
                .doOnNext { userStorage.saveAccountType(appWidgetId, AccountType.PREPAID) }
                .subscribe({ settingsView.finish(appWidgetId) }, Timber::e)
    }

    fun detachView() {
        if (view == null) {
            throw IllegalStateException("View has already been detached")
        }
        view = null
        disposables.clear()
    }

    interface SettingsView : BasePresenter.View {
        fun currentAccountClicks(): Observable<Unit>
        fun prepaidClicks(): Observable<Unit>

        fun finish(appWidgetId: Int)
    }
}
