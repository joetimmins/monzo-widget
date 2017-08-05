package com.emmaguy.monzo.widget.balance

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.widget.RemoteViews
import com.emmaguy.monzo.widget.MonzoWidgetApp
import com.emmaguy.monzo.widget.R
import com.emmaguy.monzo.widget.api.model.Balance
import com.emmaguy.monzo.widget.common.TypefaceSpan
import com.emmaguy.monzo.widget.common.toPx
import com.emmaguy.monzo.widget.settings.SettingsPresenter
import java.math.BigDecimal
import java.util.*
import android.app.PendingIntent
import android.content.Intent
import com.emmaguy.monzo.widget.settings.SettingsActivity


class BalanceWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateAllWidgets(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val userStorage = MonzoWidgetApp.get(context).storageModule.userStorage
        for (widgetId in appWidgetIds) {
            userStorage.removeAccountType(widgetId)
        }
    }

    companion object {
        private const val ROBOTO_LIGHT = "sans-serif-light"
        private const val ROBOTO_REGULAR = "sans-serif"

        fun updateAllWidgets(context: Context) {
            val thisWidget = ComponentName(context, BalanceWidgetProvider::class.java)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

            for (i in allWidgetIds) {
                updateWidget(context, i, appWidgetManager)
            }
        }

        fun updateWidget(context: Context, appWidgetId: Int, appWidgetManager: AppWidgetManager) {
            val userStorage = MonzoWidgetApp.get(context).storageModule.userStorage
            val accountType = userStorage.getAccountType(appWidgetId)
            val isCurrentAccount = accountType == SettingsPresenter.AccountType.CURRENT.ordinal

            val accountBalance = (if (isCurrentAccount)
                userStorage.currentAccountBalance
            else userStorage.prepaidBalance) ?: return

            val backgroundResource = if (isCurrentAccount) R.drawable.background_ca else R.drawable.background_prepaid
            val textColour = ContextCompat.getColor(context, if (isCurrentAccount) R.color.monzo_dark else R.color.monzo_light)

            val balance = BigDecimal(accountBalance.balance).scaleByPowerOfTen(-2).toBigInteger()
            val currency = Currency.getInstance(accountBalance.currency).symbol
            val spannableString = createSpannableForBalance(context, currency, balance.toString(), textColour)

            // Create intent to open settings when widget is clicked
            val intent = Intent(context, SettingsActivity::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            val pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            val remoteViews = RemoteViews(context.packageName, R.layout.widget_balance)
            remoteViews.setOnClickPendingIntent(R.id.root_layout, pendingIntent)
            remoteViews.setTextViewText(R.id.widgetAmountTextView, spannableString)
            remoteViews.setInt(R.id.widgetBackgroundView, "setBackgroundResource", backgroundResource)
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }



        private fun createSpannableForBalance(context: Context, currency: String, balance: String, textColour: Int): SpannableString {
            // Some supremely crude scaling as balance gets larger
            val currencySize = when {
                balance.length < 2 -> 23f
                balance.length == 2 -> 18f
                balance.length == 3 -> 14f
                balance.length == 4 -> 12f
                else -> 9f
            }
            val integerPartSize = when {
                balance.length < 2 -> 35f
                balance.length == 2 -> 27f
                balance.length == 3 -> 23f
                balance.length == 4 -> 18f
                else -> 14f
            }

            val fullString = currency + balance
            val span = SpannableString(fullString)
            applyToCurrency(span, currency, AbsoluteSizeSpan(currencySize.toPx(context)))
            applyToCurrency(span, currency, ForegroundColorSpan(textColour))
            applyToCurrency(span, currency, TypefaceSpan(Typeface.create(ROBOTO_LIGHT, Typeface.NORMAL)))

            applyToIntegerPart(span, currency, fullString, AbsoluteSizeSpan(integerPartSize.toPx(context)))
            applyToIntegerPart(span, currency, fullString, ForegroundColorSpan(textColour))
            applyToIntegerPart(span, currency, fullString, TypefaceSpan(Typeface.create(ROBOTO_REGULAR, Typeface.NORMAL)))

            return span
        }

        private fun applyToCurrency(spannable: SpannableString, currency: String, span: Any) {
            spannable.setSpan(span, 0, currency.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        private fun applyToIntegerPart(spannable: SpannableString, currency: String, wholeSting: String, span: Any) {
            spannable.setSpan(span, currency.length, wholeSting.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }
    }
}