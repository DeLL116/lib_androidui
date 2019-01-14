package com.nochino.support.androidui.activities

import android.os.Bundle

abstract class BaseLeanbackActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        initView()
    }
}