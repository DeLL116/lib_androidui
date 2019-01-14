package com.nochino.support.androidui.activities

import androidx.appcompat.widget.Toolbar
import com.nochino.support.androidui.R

abstract class BaseMobileToolbarActivity : BaseMobileActivity() {

    private var mToolbar: Toolbar? = null

    override fun initView() {
        super.initView()
        setupToolbar()
    }

    /**
     * Its common use a toolbar within activity, if it exists in the
     * layout this will be configured
     */
    fun setupToolbar() {
        mToolbar = findViewById(R.id.base_activity_toolbar)
        if (mToolbar != null) {
            setSupportActionBar(mToolbar)
        }
    }

    fun getToolbar(): Toolbar? {
        return mToolbar
    }
}