package com.nochino.support.androidui.recyclerview

import android.view.View
import androidx.recyclerview.widget.RecyclerView


abstract class BaseViewHolder<T, L : BaseRecyclerViewClickListener<T>>
    (itemView: View) : RecyclerView.ViewHolder(itemView) {

    /**
     * Bind data to the item and set listener if needed.
     *
     * @param item     object, associated with the item.
     * @param listener listener a listener [BaseRecyclerViewClickListener] which has to b set
     * at the item (if not `null`).
     */
    abstract fun onBind(item: T, listener: L?)
}