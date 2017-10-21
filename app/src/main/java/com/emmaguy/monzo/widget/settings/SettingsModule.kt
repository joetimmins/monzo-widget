package com.emmaguy.monzo.widget.settings

import android.content.Context
import com.emmaguy.monzo.widget.StorageModule

class SettingsModule(
        private val context: Context,
        private val storageModule: StorageModule) {

    fun provideSettingsPresenter(): SettingsPresenter {
        return SettingsPresenter(context, storageModule.userStorage)
    }

}