package com.emmaguy.monzo.widget.settings

import com.emmaguy.monzo.widget.UserStorage
import com.emmaguy.monzo.widget.common.BasePresenter
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import android.appwidget.AppWidgetManager
import android.content.Context
import com.emmaguy.monzo.widget.balance.BalanceWidgetProvider
import android.content.Intent


class SettingsPresenter(
        private val context: Context,
        private val userStorage: UserStorage
) : BasePresenter<SettingsPresenter.View>() {

    private lateinit var settingsView: SettingsPresenter.View

    override fun attachView(view: SettingsPresenter.View) {
        super.attachView(view)
        settingsView = view

        disposables.add(prepaidAccountClick(view.onPrepaidAccountClicked()))
        disposables.add(currentAccountClick(view.onCurrentAccountClicked()))

        enableButtons()
    }

    private fun enableButtons() {
        // Disable account type button if it has no balance
        settingsView.showCurrentAccountButton(userStorage.currentAccountBalance != null)
        settingsView.showPrepaidAccountButton(userStorage.prepaidBalance != null)
    }

    private fun prepaidAccountClick(onPrepaidAccountClicked: Observable<Unit>): Disposable {
        return onPrepaidAccountClicked.subscribe({ accountTypeButtonClick(AccountType.PREPAID) })
    }

    private fun currentAccountClick(onCurrentAccountClicked: Observable<Unit>): Disposable {
        return onCurrentAccountClicked.subscribe({ accountTypeButtonClick(AccountType.CURRENT) })
    }

    private fun accountTypeButtonClick(accountType: AccountType) {
        val appWidgetManager = AppWidgetManager.getInstance(context)

        val extras = settingsView.getSettingsIntent().extras
        val widgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            settingsView.finishActivity()
        }

        userStorage.saveAccountType(widgetId, accountType.ordinal)
        BalanceWidgetProvider.updateWidget(context, widgetId, appWidgetManager)

        settingsView.finishSuccess()
    }

    interface View : BasePresenter.View {
        fun finishSuccess()
        fun finishActivity()
        fun onCurrentAccountClicked(): Observable<Unit>
        fun onPrepaidAccountClicked(): Observable<Unit>
        fun showCurrentAccountButton(enabled: Boolean)
        fun showPrepaidAccountButton(enabled: Boolean)
        fun getSettingsIntent(): Intent
    }

    enum class AccountType {
        PREPAID,
        CURRENT
    }
}