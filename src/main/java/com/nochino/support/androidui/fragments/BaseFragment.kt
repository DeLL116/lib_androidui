package com.nochino.support.androidui.fragments

import android.view.animation.Animation
import android.view.animation.AnimationUtils
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

    /**
     * Overridden to create IdlingResourceCounters when a Fragment has an "Enter"
     * animation. This is done so Espresso tests will wait on the fragment's
     * enter animation to complete before executing View assertions. Essentially,
     * this keeps Espresso tests within Fragment synchronized to its enter
     * animation.
     */
    // TODO :: Move to "Staging Debug" Flavor class variant (don't keep in production code)!
    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        var fragmentAnimation = super.onCreateAnimation(transit, enter, nextAnim)

        if (fragmentAnimation == null && enter && nextAnim!= 0) {
            fragmentAnimation = AnimationUtils.loadAnimation(activity, nextAnim)
            fragmentViewModelIdlingResource?.incrementTestIdleResourceCounter()
        }

        fragmentAnimation?.setAnimationListener(object: Animation.AnimationListener {

            override fun onAnimationEnd(animation: Animation?) {
                fragmentViewModelIdlingResource?.decrementIdleResourceCounter()
            }

            override fun onAnimationRepeat(animation: Animation?) { /* Currently no op*/ }
            override fun onAnimationStart(animation: Animation?) {/* Currently no op */ }
        })

        return fragmentAnimation
    }
}