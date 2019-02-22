package com.nochino.support.androidui.views.recyclerview.adapters

import androidx.recyclerview.widget.DiffUtil

abstract class BaseRecyclerViewDiffUtil<T>(val oldList: List<T>, val newList: List<T>) : DiffUtil.Callback()