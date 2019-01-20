package com.nochino.support.androidui.testing

import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.test.espresso.idling.CountingIdlingResource
import com.nochino.support.androidui.BuildConfig
import timber.log.Timber

/**
 * A class that combines the functionality of both a [ViewModel] (for easy retrieval and
 * monitoring) and a [CountingIdlingResource] (for instrumentation testing).
 *
 * Allows implementations the ability to more easily synchronize instrumentation tests between
 * and across different components of an application.
 * ie...Fragment -> Activity, Activity -> View, Fragment -> Activity View...etc)
 */
class CountingIdlingResourceViewModel(clazz: Class<Any>) : ViewModel() {

    /**
     * Publicly visible reference to the backing resource [_mBackingIdlingRes].
     * See [_mBackingIdlingRes].
     */
    val idlingResource: CountingIdlingResource? by lazy {
        return@lazy if (BuildConfig.DEBUG) {
            _mBackingIdlingRes
        } else {
            null
        }
    }

    /**
     * Private backing field of [CountingIdlingResource] used in instrumentation tests
     * to wait for the testing class to report that it's idle and safe for espresso to proceed
     *
     * This object will *always* be null in non-debuggable builds.
     *
     * Lazily initialized so it is only constructed when needed (Debug builds) by the implementation tests
     */
    // TODO :: Make instance usage better only for testing (maybe remove from production code entirely?)
    @Suppress("ConstantConditionIf")
    @VisibleForTesting
    private val _mBackingIdlingRes: CountingIdlingResource? by lazy {
        return@lazy if (BuildConfig.DEBUG) {
            // Debuggable build...create the object for testing
            CountingIdlingResource(clazz.simpleName + "::" + clazz.hashCode().toString())
        } else {
            // Non-debuggable build...don't create the object
            null
        }.also {
            // Log if the object was created or not (sanity check)
            Timber.i("idlingResource created = %s", it != null)
        }
    }

    /**
     * Decrements the idling resource counter, never letting it decrement
     * while it's idle
     */
    fun decrementIdleResourceCounter() {
        _mBackingIdlingRes?.let {
            if (!it.isIdleNow) {
                it.decrement()
            } else {
                Timber.d("Trying to decrementIdleResourceCounter an idle counter! Execution halted!")
            }
        }
    }

    /**
     * Increments the idling resource counter
     */
    fun incrementTestIdleResourceCounter() {
        _mBackingIdlingRes?.increment()
    }
}

/**
 * Subclassed ViewModelProvider Factory allowing the use of a non-default construct for the
 * provided ViewModel class.
 *
 * See [https://youtu.be/5qlIPTDE274?t=139]
 */
class CountingIdlingResourceViewModelFactory(private val clazz: Class<Any>) : ViewModelProvider.Factory {

    /**
     * Convenience companion object to create a [CountingIdlingResourceViewModel]
     * from an Activity or Fragment
     */
    companion object {
        fun getFragmentViewModel(fragment: Fragment): CountingIdlingResourceViewModel {
            return ViewModelProviders
                .of(fragment, CountingIdlingResourceViewModelFactory(fragment.javaClass))
                .get(CountingIdlingResourceViewModel::class.java)
        }

        fun getActivityViewModel(fragmentActivity: FragmentActivity): CountingIdlingResourceViewModel {
            return ViewModelProviders
                .of(fragmentActivity, CountingIdlingResourceViewModelFactory(fragmentActivity.javaClass))
                .get(CountingIdlingResourceViewModel::class.java)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CountingIdlingResourceViewModel(clazz) as T
    }
}