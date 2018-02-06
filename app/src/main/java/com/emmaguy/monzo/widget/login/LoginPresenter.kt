package com.emmaguy.monzo.widget.login

import com.emmaguy.monzo.widget.UserStorage
import com.emmaguy.monzo.widget.api.MonzoApi
import com.emmaguy.monzo.widget.api.model.AccountType
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.*


class LoginPresenter(
        private val monzoApi: MonzoApi,
        private val uiScheduler: Scheduler,
        private val ioScheduler: Scheduler,
        private val clientId: String,
        private val clientSecret: String,
        private val redirectUri: String,
        private val userStorage: UserStorage
) {
    private val disposables = CompositeDisposable()
    private var view: LoginView? = null

    fun attachView(loginView: LoginView) {
        if (view !== null) {
            throw IllegalStateException("View $view has already been attached")
        }
        view = loginView

        if (userStorage.hasToken()) {
            loginView.showLoggedIn()
            loginView.startBackgroundRefresh()
        }

        disposables.add(loginView.loginClicks()
                .subscribe {
                    userStorage.state = UUID.randomUUID().toString()

                    loginView.showRedirecting()
                    loginView.hideLoginButton()
                    loginView.startLogin("https://auth.monzo.com/?client_id=$clientId" +
                            "&redirect_uri=$redirectUri" +
                            "&response_type=code" +
                            "&state=" + userStorage.state)
                })

        disposables.add(loginView.authCodeChanges()
                .doOnNext { loginView.showLoading() }
                .doOnNext { loginView.showLoggingIn() }
                .flatMapMaybe { (code, state) ->
                    if (state == userStorage.state) {
                        monzoApi.requestAccessToken(clientId, clientSecret, redirectUri, code)
                                .doOnSuccess { token -> userStorage.saveToken(token) }
                                .flatMap { monzoApi.accounts() }
                                .map { it.accounts }
                                .subscribeOn(ioScheduler)
                                .observeOn(uiScheduler)
                                .doOnError {
                                    loginView.showLogIn()
                                    userStorage.state = null
                                }
                                .toMaybe()
                                .onErrorResumeNext(Maybe.empty())
                    } else {
                        loginView.showLogIn()
                        userStorage.state = null
                        Maybe.empty()
                    }
                }
                .doOnNext {
                    for (account in it) {
                        when {
                            account.type == AccountType.PREPAID -> userStorage.prepaidAccountId = account.id
                            account.type == AccountType.CURRENT_ACCOUNT -> userStorage.currentAccountId = account.id
                        }
                    }
                }
                .observeOn(uiScheduler)
                .doOnNext { loginView.hideLoading() }
                .subscribe({
                    loginView.showLoggedIn()
                    loginView.startBackgroundRefresh()
                }, Timber::e))
    }

    fun detachView() {
        if (view == null) {
            throw IllegalStateException("View has already been detached")
        }
        view = null
        disposables.clear()
    }

    fun storeMonzoMeLink(monzoMeLink: String) {
        userStorage.saveMonzoMeLink(monzoMeLink)
    }

    interface LoginView {
        fun loginClicks(): Observable<Unit>
        fun authCodeChanges(): Observable<Pair<String, String>>

        fun showLoading()
        fun hideLoading()

        fun showLogIn()

        fun startLogin(uri: String)
        fun showRedirecting()
        fun hideLoginButton()

        fun showLoggingIn()

        fun showLoggedIn()
        fun startBackgroundRefresh()
        fun showMonzoMeInput()
    }
}
