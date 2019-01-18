package com.nochino.support.androidui.testing

import androidx.test.espresso.IdlingResource
import timber.log.Timber

class CountingIdlingResourceViewModelManager(clazz: Class<Any>) {

    private val countingIdlingResourceViewModel: CountingIdlingResourceViewModel? by lazy {
        return@lazy CustomViewModelFactory(clazz.javaClass)
            .create(CountingIdlingResourceViewModel::class.java)
    }

    fun decrementIdleResourceCounter() {
        countingIdlingResourceViewModel?.idlingResource?.let {
            when (it.isIdleNow) {
                false -> {
                    it.decrement()
                }
                true -> {
                    Timber.d("Trying to decrementIdleResourceCounter an idle counter! Execution halted!")
                }
            }
        }
    }

    fun incrementTestIdleResourceCounter() {
        countingIdlingResourceViewModel?.idlingResource?.increment()
    }

    fun getIdlingResource(): IdlingResource? {
        return countingIdlingResourceViewModel?.idlingResource
    }
}