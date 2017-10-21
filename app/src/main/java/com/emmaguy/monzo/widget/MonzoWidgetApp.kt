package com.emmaguy.monzo.widget

import android.app.Application
import android.content.Context
import com.emmaguy.monzo.widget.api.ApiModule
import com.emmaguy.monzo.widget.login.LoginModule
import com.emmaguy.monzo.widget.settings.SettingsModule
import timber.log.Timber


class MonzoWidgetApp : Application() {
    lateinit var storageModule: StorageModule
    lateinit var apiModule: ApiModule
    lateinit var loginModule: LoginModule
    lateinit var settingsModule: SettingsModule

    override fun onCreate() {
        super.onCreate()

        storageModule = StorageModule(this)
        apiModule = ApiModule(this, storageModule)
        loginModule = LoginModule(this, storageModule, apiModule)
        settingsModule = SettingsModule(this, storageModule)

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
