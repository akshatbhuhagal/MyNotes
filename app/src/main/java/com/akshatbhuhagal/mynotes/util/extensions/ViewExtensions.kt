package com.akshatbhuhagal.mynotes.util.extensions

import android.view.View


fun View.makeVisible() {
    this.visibility = View.VISIBLE
}

fun View.makeInvisible() {
    this.visibility = View.INVISIBLE
}

fun View.makeGone() {
    this.visibility = View.GONE
}

fun makeVisible(vararg views: View) {
    for (view in views) {
        view.visibility = View.VISIBLE
    }
}

fun makeInvisible(vararg views: View) {
    for (view in views) {
        view.visibility = View.INVISIBLE
    }
}

fun makeGone(vararg views: View) {
    for (view in views) {
        view.visibility = View.INVISIBLE
    }
}
