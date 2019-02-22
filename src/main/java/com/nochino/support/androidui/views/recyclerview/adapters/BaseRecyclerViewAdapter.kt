package com.nochino.support.androidui.views.recyclerview.adapters

import android.content.Context
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nochino.support.androidui.views.recyclerview.BaseRecyclerViewClickListener
import com.nochino.support.androidui.views.recyclerview.BaseViewHolder

/**
 * Base generic RecyclerView adapter.
 * Handles basic logic such as adding/removing items, setting listener, binding ViewHolders.
 * Extend the adapter for appropriate use case.
 *
 * @param <T>  type of objects, which will be used in the adapter's data set
 * @param <L>  click listener [BaseRecyclerViewClickListener]
 * @param <VH> ViewHolder [BaseViewHolder]
 */
@Suppress("unused")
abstract class BaseRecyclerViewAdapter<T, L : BaseRecyclerViewClickListener<T>, VH : BaseViewHolder<T, L>>
    /**
     * Base constructor.
     * Allocate adapter-related objects here if needed.
     *
     * @param context Context needed to retrieve LayoutInflater
     */
    (context: Context) : RecyclerView.Adapter<VH>() {

    /**
     * Secondary constructor which allows passing data immediately
     */
    constructor(context: Context, data: MutableList<T>) : this(context) {
        setItems(data)
    }

    val items: MutableList<T>
    private var listener: L? = null
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    /**
     * Returns whether adapter is empty or not.
     *
     * @return `true` if adapter is empty or `false` otherwise
     */
    val isEmpty: Boolean
        get() = itemCount == 0

    init {
        items = ArrayList()
    }

    /**
     * Inflates a view.
     *
     * @param layout       layout to me inflater
     * @param parent       container where to inflate
     * @param attachToRoot whether to attach to root or not
     * @return inflated View
     */
    @JvmOverloads
    protected fun inflate(@LayoutRes layout: Int, parent: ViewGroup?, attachToRoot: Boolean = false): View {
        return layoutInflater.inflate(layout, parent, attachToRoot)
    }

    /**
     * Base method for creating the adapter's ViewHolder.
     * Implementations should override [getViewHolder] to return an instance
     * of [BaseViewHolder]
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val holder = getViewHolder(parent, viewType)
        onViewHolderCreated(parent, holder)
        return holder
    }

    /**
     * Must be implemented by subclasses to create an instance of
     * [BaseViewHolder]
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    abstract fun getViewHolder(parent: ViewGroup, viewType: Int): VH

    /**
     * Called after a [BaseViewHolder] is created. Implementing classes
     * may override if something must be done immediately after a
     * [BaseViewHolder] is created (like modifying layout params...etc)
     */
    open fun onViewHolderCreated(parent: ViewGroup, holder : VH){}

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the itemView to reflect the item at the given
     * position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     * item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.onBind(item, listener)
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int {
        return items.size
    }

    /**
     * Sets items to the adapter and notifies that data set has been changed.
     *
     * @param items items to set to the adapter
     * @throws IllegalArgumentException in case of setting `null` items
     */
    fun setItems(items: List<T>?) {
        if (items == null) {
            throw IllegalArgumentException("Cannot set `null` item to the Recycler adapter")
        }
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    /**
     * Returns an items from the data set at a certain position.
     *
     * @return All of items in this adapter.
     */
    fun getItem(position: Int): T {
        return items[position]
    }

    /**
     * Adds item to the end of the data set.
     * Notifies that item has been inserted.
     *
     * @param item item which has to be added to the adapter.
     */
    fun add(item: T?) {
        if (item == null) {
            throw IllegalArgumentException("Cannot add null item to the Recycler adapter")
        }
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    /**
     * Adds list of items to the end of the adapter's data set.
     * Notifies that item has been inserted.
     *
     * @param items items which has to be added to the adapter.
     */
    fun addAll(items: List<T>?) {
        if (items == null) {
            throw IllegalArgumentException("Cannot add `null` items to the Recycler adapter")
        }
        this.items.addAll(items)
        notifyItemRangeInserted(this.items.size - items.size, items.size)
    }

    /**
     * Clears all the items in the adapter.
     */
    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    /**
     * Removes an item from the adapter.
     * Notifies that item has been removed.
     *
     * @param item to be removed
     */
    fun remove(item: T) {
        val position = items.indexOf(item)
        if (position > -1) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    /**
     * Moves an item in the data set and notifies that the item has been moved
     */
    fun move(item: T, from: Int, to: Int) {
        items.removeAt(from)
        items.add(to, item)
        notifyItemMoved(from, to)
    }

    /**
     * Provides a way to update the adapter's data in bulk without
     * having to call [notifyDataSetChanged]. Via the provided
     * [diffUtil], updates will only be dispatched to the views
     * are found to have changed.
     */
    fun update(items: List<T>, diffUtil: BaseRecyclerViewDiffUtil<T>) {
        val diffResult = DiffUtil.calculateDiff(diffUtil, true)
        this.items.clear()
        this.items.addAll(items)
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Set click listener, which must extend [BaseRecyclerViewClickListener]
     *
     * @param listener click listener
     */
    fun setListener(listener: L) {
        this.listener = listener
    }
}