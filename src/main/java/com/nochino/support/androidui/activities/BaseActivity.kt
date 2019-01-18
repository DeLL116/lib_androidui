package com.nochino.support.androidui.activities

import androidx.appcompat.app.AppCompatActivity
import com.nochino.support.androidui.BuildConfig
import com.nochino.support.androidui.testing.CountingIdlingResourceViewModelManager

abstract class BaseActivity : AppCompatActivity() {

    // TODO :: Move to "Staging Debug" Flavor class variant (don't keep in production code)!
    companion object {
        val activityViewModelIdlingResource: CountingIdlingResourceViewModelManager? by lazy {
            when (BuildConfig.DEBUG) {
                true -> CountingIdlingResourceViewModelManager(this.javaClass)
                else -> null
            }
        }
    }

    /**
     * @return The layout id for the implementing Activity
     */
    protected abstract fun getLayoutId(): Int

    /**
     * Use this method to initialize view components. This method is called after [ ][BaseActivity.getLayoutId]
     */
    open fun initView() {}
}