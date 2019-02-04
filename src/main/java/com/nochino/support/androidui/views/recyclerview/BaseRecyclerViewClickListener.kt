package com.nochino.support.androidui.views.recyclerview

interface BaseRecyclerViewClickListener<T> {
    fun onItemClicked(item: T)
}