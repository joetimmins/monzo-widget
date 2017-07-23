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
import java.math.BigDecimal
import java.util.*


class BalanceWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateWidgets(context, appWidgetIds, appWidgetManager)
    }

    companion object {
        private const val ROBOTO_LIGHT = "sans-serif-light"
        private const val ROBOTO_REGULAR = "sans-serif"

        fun updateAllWidgets(context: Context) {
            val thisWidget = ComponentName(context, BalanceWidgetProvider::class.java)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

            updateWidgets(context, allWidgetIds, appWidgetManager)
        }

        private fun updateWidgets(context: Context, appWidgetIds: IntArray, appWidgetManager: AppWidgetManager) {
            val userStorage = MonzoWidgetApp.get(context).storageModule.userStorage
            val prepaidBalance = userStorage.prepaidBalance
            val currentAccountBalance = userStorage.currentAccountBalance

            if (prepaidBalance == null && currentAccountBalance == null) {
                return
            }

            for (i in 0..appWidgetIds.size - 1) {
                val (balanceObj, isCurrentAccount) = getBalance(currentAccountBalance, prepaidBalance, i)
                val backgroundResource = if (isCurrentAccount) R.drawable.background_ca else R.drawable.background_prepaid
                val textColour = ContextCompat.getColor(context, if (isCurrentAccount) R.color.monzo_dark else R.color.monzo_light)

                val balance = BigDecimal(balanceObj.balance).scaleByPowerOfTen(-2).toBigInteger()
                val currency = Currency.getInstance(balanceObj.currency).symbol
                val spannableString = createSpannableForBalance(context, currency, balance.toString(), textColour)

                val remoteViews = RemoteViews(context.packageName, R.layout.widget_balance)
                remoteViews.setTextViewText(R.id.widgetAmountTextView, spannableString)
                remoteViews.setInt(R.id.widgetBackgroundView, "setBackgroundResource", backgroundResource)
                appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews)
            }
        }

        private fun getBalance(currentAccountBalance: Balance?, prepaidBalance: Balance?, i: Int): Pair<Balance, Boolean> {
            val balanceObj: Balance
            val isCurrentAccount: Boolean
            if (currentAccountBalance == null) {
                // We already checked that both are not null, so if CA is, prepaid must not be
                balanceObj = prepaidBalance!!
                isCurrentAccount = false
            } else if (prepaidBalance == null) {
                balanceObj = currentAccountBalance
                isCurrentAccount = true
            } else {
                if (i % 2 == 0) {
                    balanceObj = prepaidBalance
                    isCurrentAccount = false
                } else {
                    balanceObj = currentAccountBalance
                    isCurrentAccount = true
                }
            }
            return Pair(balanceObj, isCurrentAccount)
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