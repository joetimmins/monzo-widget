package com.emmaguy.monzo.widget.settings

import com.emmaguy.monzo.widget.StorageModule

class SettingsModule(private val storageModule: StorageModule) {

    fun provideSettingsPresenter(widgetId: Int): SettingsPresenter {
        return SettingsPresenter(widgetId, storageModule.userStorage)
    }
}