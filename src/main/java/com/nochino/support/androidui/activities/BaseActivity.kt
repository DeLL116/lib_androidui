package com.nochino.support.androidui.activities

import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    /**
     * @return The layout id for the implementing Activity
     */
    protected abstract fun getLayoutId(): Int

    /**
     * Use this method to initialize view components. This method is called after [ ][BaseActivity.getLayoutId]
     */
    open fun initView() {}
}