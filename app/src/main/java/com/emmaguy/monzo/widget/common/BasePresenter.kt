package com.emmaguy.monzo.widget.common

import io.reactivex.disposables.CompositeDisposable

abstract class BasePresenter<in V : BasePresenter.View> {
    protected var disposables: CompositeDisposable = CompositeDisposable()
    private var view: View? = null

    interface View
}
