package com.nochino.support.androidui.fragments

import androidx.fragment.app.Fragment
import com.nochino.support.androidui.BuildConfig
import com.nochino.support.androidui.testing.CountingIdlingResourceViewModelManager

// WIP...stub for future use
abstract class BaseFragment: Fragment() {
    // TODO :: Move to "Staging Debug" Flavor class variant (don't keep in production code)!
    companion object {
        val fragmentViewModelIdlingResource: CountingIdlingResourceViewModelManager? by lazy {
            when (BuildConfig.DEBUG) {
                true -> CountingIdlingResourceViewModelManager(this.javaClass)
                else -> null
            }
        }
    }
}