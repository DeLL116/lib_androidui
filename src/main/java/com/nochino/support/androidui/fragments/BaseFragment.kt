package com.nochino.support.androidui.fragments

import androidx.fragment.app.Fragment
import com.nochino.support.androidui.BuildConfig
import com.nochino.support.androidui.testing.CountingIdlingResourceViewModel
import com.nochino.support.androidui.testing.CountingIdlingResourceViewModelFactory

// WIP...stub for future use
abstract class BaseFragment: Fragment() {
    // TODO :: Move to "Staging Debug" Flavor class variant (don't keep in production code)!
    val fragmentViewModelIdlingResource: CountingIdlingResourceViewModel? by lazy {
        when (BuildConfig.DEBUG) {
            true -> CountingIdlingResourceViewModelFactory.getFragmentViewModel(this)
            else -> null
        }
    }
}