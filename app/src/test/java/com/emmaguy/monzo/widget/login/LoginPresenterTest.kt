package com.emmaguy.monzo.widget.login

import com.emmaguy.monzo.widget.UserStorage
import com.emmaguy.monzo.widget.api.MonzoApi
import com.emmaguy.monzo.widget.api.model.Account
import com.emmaguy.monzo.widget.api.model.AccountType
import com.emmaguy.monzo.widget.api.model.AccountsResponse
import com.emmaguy.monzo.widget.api.model.Token
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations.initMocks
import org.mockito.Mockito.`when` as whenMock

class LoginPresenterTest {
    private val CLIENT_ID = "CLIENT_ID"
    private val CLIENT_SECRET = "CLIENT_SECRET"
    private val REDIRECT_URI = "REDIRECT_URI"

    private val DEFAULT_CODE = "DEFAULT_CODE"
    private val DEFAULT_STATE = "DEFAULT_STATE"

    private val defaultToken = Token("access_token", "refresh_token", "type")

    private val loginRelay = PublishRelay.create<Unit>()
    private val authCodeRelay = PublishRelay.create<Pair<String, String>>()

    @Mock private lateinit var monzoApi: MonzoApi
    @Mock private lateinit var userStorage: UserStorage

    private lateinit var presenter: LoginPresenter
    @Mock private lateinit var view: LoginPresenter.View

    @Before
    fun setUp() {
        initMocks(this)

        whenMock(view.onLoginClicked()).thenReturn(loginRelay)
        whenMock(view.onAuthCodeReceived()).thenReturn(authCodeRelay)

        whenMock(userStorage.state).thenReturn(DEFAULT_STATE)

        whenMock(monzoApi.requestAccessToken(CLIENT_ID, CLIENT_SECRET, REDIRECT_URI, DEFAULT_CODE)).thenReturn(Single.just(defaultToken))
        whenMock(monzoApi.accounts(AccountType.PREPAID.value)).thenReturn(Single.just(AccountsResponse(listOf(Account("prepaid_id")))))
        whenMock(monzoApi.accounts(AccountType.CURRENT_ACCOUNT.value)).thenReturn(Single.just(AccountsResponse(listOf(Account("ca_id")))))

        presenter = LoginPresenter(monzoApi, Schedulers.trampoline(), Schedulers.trampoline(), CLIENT_ID, CLIENT_SECRET,
                REDIRECT_URI, userStorage)
    }

    @Test
    @Throws(Exception::class)
    fun attachView_hasToken_showLoggedIn() {
        whenMock(userStorage.hasToken()).thenReturn(true)

        presenter.attachView(view)

        verify(view).showLoggedIn()
    }

    @Test
    @Throws(Exception::class)
    fun attachView_hasToken_startBackgroundRefresh() {
        whenMock(userStorage.hasToken()).thenReturn(true)

        presenter.attachView(view)

        verify(view).startBackgroundRefresh()
    }

    @Test
    @Throws(Exception::class)
    fun onLoginClicked_showRedirecting() {
        presenter.attachView(view)

        loginRelay.accept(Unit)

        verify(view).showRedirecting()
    }

    @Test
    @Throws(Exception::class)
    fun onLoginClicked_hideLoginButton() {
        presenter.attachView(view)

        loginRelay.accept(Unit)

        verify(view).hideLoginButton()
    }

    @Test
    @Throws(Exception::class)
    fun onLoginClicked_startLogin() {
        presenter.attachView(view)

        loginRelay.accept(Unit)

        verify(view).startLogin(anyString())
    }

    @Test
    @Throws(Exception::class)
    fun onAuthCodeReceived_showLoading() {
        presenter.attachView(view)

        authCodeRelay.accept(Pair(DEFAULT_CODE, DEFAULT_STATE))

        verify(view).showLoading()
    }

    @Test
    @Throws(Exception::class)
    fun onAuthCodeReceived_showLoggingIn() {
        presenter.attachView(view)

        authCodeRelay.accept(Pair(DEFAULT_CODE, DEFAULT_STATE))

        verify(view).showLoggingIn()
    }

    @Test
    @Throws(Exception::class)
    fun onAuthCodeReceived_requestAccessToken() {
        presenter.attachView(view)

        authCodeRelay.accept(Pair(DEFAULT_CODE, DEFAULT_STATE))

        verify(monzoApi).requestAccessToken(anyString(), anyString(), anyString(), anyString(), anyString())
    }

    @Test
    @Throws(Exception::class)
    fun onAuthCodeReceived_saveToken() {
        presenter.attachView(view)

        authCodeRelay.accept(Pair(DEFAULT_CODE, DEFAULT_STATE))

        verify(userStorage).saveToken(defaultToken) }


    @Test
    @Throws(Exception::class)
    fun onAuthCodeReceived_showLoggedIn() {
        presenter.attachView(view)

        authCodeRelay.accept(Pair(DEFAULT_CODE, DEFAULT_STATE))

        verify(view).showLoggedIn()
    }

    @Test
    @Throws(Exception::class)
    fun onAuthCodeReceived_differentState_dontRequestAccessToken() {
        presenter.attachView(view)

        authCodeRelay.accept(Pair(DEFAULT_CODE, "differentState"))

        verify(monzoApi, never()).requestAccessToken(anyString(), anyString(), anyString(), anyString(), anyString())
    }

    @Test
    @Throws(Exception::class)
    fun onAuthCodeReceived_differentState_showLogIn() {
        presenter.attachView(view)

        authCodeRelay.accept(Pair(DEFAULT_CODE, "differentState"))

        verify(view).showLogIn()
    }

    @Test
    @Throws(Exception::class)
    fun onAuthCodeReceived_differentStateThenCorrectState_showLoggedIn() {
        presenter.attachView(view)

        authCodeRelay.accept(Pair(DEFAULT_CODE, "differentState"))
        authCodeRelay.accept(Pair(DEFAULT_CODE, DEFAULT_STATE))

        verify(view).showLoggedIn()
    }

    @Test
    @Throws(Exception::class)
    fun onAuthCodeReceived_noCurrentAccount_showLoggedIn() {
        whenMock(monzoApi.accounts(AccountType.CURRENT_ACCOUNT.value)).thenReturn(Single.just(AccountsResponse(listOf())))
        presenter.attachView(view)

        authCodeRelay.accept(Pair(DEFAULT_CODE, DEFAULT_STATE))

        verify(view).showLoggedIn()
    }

    @Test
    @Throws(Exception::class)
    fun onAuthCodeReceived_noPrepaidAccount_showLoggedIn() {
        whenMock(monzoApi.accounts(AccountType.PREPAID.value)).thenReturn(Single.just(AccountsResponse(listOf())))
        presenter.attachView(view)

        authCodeRelay.accept(Pair(DEFAULT_CODE, DEFAULT_STATE))

        verify(view).showLoggedIn()
    }
}