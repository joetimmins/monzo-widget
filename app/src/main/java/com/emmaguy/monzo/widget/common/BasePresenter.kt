package com.emmaguy.monzo.widget.common

import io.reactivex.disposables.CompositeDisposable

abstract class BasePresenter<in V : BasePresenter.View> {
    protected var disposables: CompositeDisposable = CompositeDisposable()
    private var view: View? = null

    open fun attachView(view: V) {
        if (this.view !== null) {
            throw IllegalStateException("View " + this.view + " has already been attached")
        }

        this.view = view
    }

    open fun detachView() {
        if (this.view == null) {
            throw IllegalStateException("View has already been detached")
        }

        this.view = null
        this.disposables.clear()
    }

    interface View
}