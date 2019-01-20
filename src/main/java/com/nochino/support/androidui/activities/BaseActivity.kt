package com.nochino.support.androidui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nochino.support.androidui.BuildConfig
import com.nochino.support.androidui.testing.CountingIdlingResourceViewModel
import com.nochino.support.androidui.testing.CountingIdlingResourceViewModelFactory

abstract class BaseActivity : AppCompatActivity() {

    // TODO :: Move to "Staging Debug" Flavor class variant (don't keep in production code)!
    val activityViewModelIdlingResource: CountingIdlingResourceViewModel? by lazy {
        when (BuildConfig.DEBUG) {
            true -> CountingIdlingResourceViewModelFactory.getActivityViewModel(this)
            else -> null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        initView()
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