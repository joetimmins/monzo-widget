package com.emmaguy.monzo.widget.settings

import com.emmaguy.monzo.widget.UserStorage
import com.emmaguy.monzo.widget.api.model.AccountType
import com.jakewharton.rxrelay2.PublishRelay
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations.initMocks
import org.mockito.Mockito.`when` as whenever

class SettingsPresenterTest {
    private val appWidgetId = 1
    private val currentAccountRelay = PublishRelay.create<Unit>()
    private val prepaidRelay = PublishRelay.create<Unit>()

    @Mock private lateinit var userStorage: UserStorage

    private lateinit var presenter: SettingsPresenter
    @Mock private lateinit var settingsView: SettingsPresenter.SettingsView

    @Before fun setUp() {
        initMocks(this)

        whenever(settingsView.currentAccountClicks()).thenReturn(currentAccountRelay)
        whenever(settingsView.prepaidClicks()).thenReturn(prepaidRelay)

        presenter = SettingsPresenter(appWidgetId, userStorage)
    }

    @Test fun currentAccountClicks_saveCurrentAccount() {
        presenter.attachView(settingsView)

        currentAccountRelay.accept(Unit)

        verify(userStorage).saveAccountType(appWidgetId, AccountType.CURRENT_ACCOUNT)
    }

    @Test fun prepaidClicks_savePrepaid() {
        presenter.attachView(settingsView)

        prepaidRelay.accept(Unit)

        verify(userStorage).saveAccountType(appWidgetId, AccountType.PREPAID)
    }

    @Test fun currentAccountClicks_finishSuccess() {
        presenter.attachView(settingsView)

        currentAccountRelay.accept(Unit)

        verify(settingsView).finish(appWidgetId)
    }

    @Test fun prepaidClicks_finishSuccess() {
        presenter.attachView(settingsView)

        prepaidRelay.accept(Unit)

        verify(settingsView).finish(appWidgetId)
    }
}
