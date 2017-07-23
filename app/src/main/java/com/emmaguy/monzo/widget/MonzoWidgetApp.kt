package com.emmaguy.monzo.widget

import android.app.*
import android.content.Context
import android.os.Build
import com.emmaguy.monzo.widget.login.LoginModule
import com.readystatesoftware.chuck.Chuck
import timber.log.Timber


class MonzoWidgetApp : Application() {
    lateinit var loginModule: LoginModule

    override fun onCreate() {
        super.onCreate()

        loginModule = LoginModule(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    companion object {
        fun get(context: Context): MonzoWidgetApp {
            return context.applicationContext as MonzoWidgetApp
        }
    }
}
