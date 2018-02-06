package com.emmaguy.monzo.widget.login

import android.content.Context
import com.emmaguy.monzo.widget.AppModule
import com.emmaguy.monzo.widget.R
import com.emmaguy.monzo.widget.StorageModule
import com.emmaguy.monzo.widget.api.ApiModule
import com.emmaguy.monzo.widget.login.LastTransactionPresenter.LastTransactionEventListener
import com.emmaguy.monzo.widget.login.LastTransactionPresenter.LastTransactionView

class LoginModule(
        private val context: Context,
        private val storageModule: StorageModule,
        private val apiModule: ApiModule
) {

    fun provideLoginPresenter(): LoginPresenter {
        return LoginPresenter(
                apiModule.monzoApi,
                AppModule.uiScheduler(),
                AppModule.ioScheduler(),
                apiModule.clientId,
                apiModule.clientSecret,
                provideRedirectUri(),
                storageModule.userStorage
        )
    }

    fun provideLastTransactionPresenter(eventListener: LastTransactionEventListener, view: LastTransactionView): LastTransactionPresenter {
        return LastTransactionPresenter(
                apiModule.monzoApi,
                AppModule.uiScheduler(),
                AppModule.ioScheduler()
        )
    }

    private fun provideRedirectUri(): String {
        return context.getString(R.string.callback_url_scheme) + "://" + context.getString(R.string.callback_url_host)
    }
}
