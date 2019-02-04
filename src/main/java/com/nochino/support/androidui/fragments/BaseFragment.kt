package com.nochino.support.androidui.fragments

import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import com.nochino.support.androidui.BuildConfig
import com.nochino.support.androidui.R
import com.nochino.support.androidui.testing.CountingIdlingResourceViewModel
import com.nochino.support.androidui.testing.CountingIdlingResourceViewModelFactory
import java.lang.reflect.ParameterizedType

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

    fun setAppBarScrollFlags(scrollFlags: Int =
                           AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                                   or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
                                   or AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP) {

        val mainView = activity?.findViewById(android.R.id.content) as ViewGroup?
        val appBarLayout = mainView?.findViewById<AppBarLayout>(R.id.base_app_bar)

        appBarLayout?.let {
            if (scrollFlags == 0) {
                // Ensure the AppBarLayout is expanded.
                // This removes top margin that is placed on the containing view
                // when the AppBarLayout is able to collapse
                it.setExpanded(true, true)
            }

            // Set the new scrolling flags on the ToolBar's layout params
            val toolbar = it.findViewById<Toolbar>(R.id.base_activity_toolbar)
            val toolbarParams = toolbar?.layoutParams as AppBarLayout.LayoutParams?
            toolbarParams?.scrollFlags = scrollFlags
        }
    }
}

// BaseFragment Extension Functions
/**
 * @param typeParamPosition The position of the type parameter in the class signature as it
 * is.
 *
 * @return The simple name of the class object provided at the [typeParamPosition]. If the
 * type cannot be found the implementing class' simple name will be returned.
 */
fun BaseFragment.getGenericTypeClassName(typeParamPosition: Int) : String {
    return try {
        getGenericTypeClass(typeParamPosition).simpleName
    } catch (cce: ClassCastException) {
        this::class.simpleName!!
    }
}

/**
 * @param typeParamPosition The position of the type parameter in the class signature as it
 * is listed
 *
 * @return A Class object of the parameter type provided at the [typeParamPosition]
 */
fun BaseFragment.getGenericTypeClass(typeParamPosition: Int): Class<*> {
    val parameterizedType = this.javaClass.genericSuperclass as ParameterizedType
    return parameterizedType.actualTypeArguments[typeParamPosition] as Class<*>
}