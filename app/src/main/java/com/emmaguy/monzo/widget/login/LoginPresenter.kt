package com.emmaguy.monzo.widget.login

import com.emmaguy.monzo.widget.UserStorage
import com.emmaguy.monzo.widget.api.MonzoApi
import com.emmaguy.monzo.widget.api.model.AccountType
import com.emmaguy.monzo.widget.common.BasePresenter
import com.emmaguy.monzo.widget.common.plus
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Scheduler
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
) : BasePresenter<LoginPresenter.View>() {

    override fun attachView(view: View) {
        super.attachView(view)

        if (userStorage.hasToken()) {
            view.showLoggedIn()
            view.startBackgroundRefresh()
        }

        disposables += view.loginClicks()
                .subscribe {
                    userStorage.state = UUID.randomUUID().toString()

                    view.showRedirecting()
                    view.hideLoginButton()
                    view.startLogin("https://auth.monzo.com/?client_id=$clientId" +
                            "&redirect_uri=$redirectUri" +
                            "&response_type=code" +
                            "&state=" + userStorage.state)
                }

        disposables += view.authCodeChanges()
                .doOnNext { view.showLoading() }
                .doOnNext { view.showLoggingIn() }
                .flatMapMaybe { (code, state) ->
                    when (state) {
                        userStorage.state -> {
                            monzoApi.requestAccessToken(clientId, clientSecret, redirectUri, code)
                                    .doOnSuccess { token -> userStorage.saveToken(token) }
                                    .flatMap { monzoApi.accounts() }
                                    .map { it.accounts }
                                    .subscribeOn(ioScheduler)
                                    .observeOn(uiScheduler)
                                    .doOnError {
                                        view.showLogIn()
                                        userStorage.state = null
                                    }
                                    .toMaybe()
                                    .onErrorResumeNext(Maybe.empty())
                        }
                        else -> {
                            view.showLogIn()
                            userStorage.state = null
                            Maybe.empty()
                        }
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
                .doOnNext { view.hideLoading() }
                .subscribe({
                    view.showLoggedIn()
                    view.startBackgroundRefresh()
                }, Timber::e)
    }

    interface View : BasePresenter.View {
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
    }
}