package com.emmaguy.monzo.widget.settings

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.emmaguy.monzo.widget.MonzoWidgetApp
import com.emmaguy.monzo.widget.R
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.enabled
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : AppCompatActivity(), SettingsPresenter.View {

    private val presenter by lazy { MonzoWidgetApp.get(this).settingsModule.provideSettingsPresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        setResult(RESULT_CANCELED)

        presenter.attachView(this)
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }

    override fun finishSuccess() {
        val extras = intent.extras
        if (extras != null) {
            val widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                setResult(Activity.RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
                finish()
            }
        }
    }

    override fun finishActivity() {
        finish()
    }

    override fun getSettingsIntent(): Intent {
        return intent
    }

    override fun onCurrentAccountClicked(): Observable<Unit> {
        return cButton.clicks()
    }

    override fun onPrepaidAccountClicked(): Observable<Unit> {
        return ppButton.clicks()
    }

    override fun showCurrentAccountButton(enabled: Boolean) {
        cButton.isEnabled = enabled
    }

    override fun showPrepaidAccountButton(enabled: Boolean) {
        ppButton.isEnabled = enabled
    }

}

