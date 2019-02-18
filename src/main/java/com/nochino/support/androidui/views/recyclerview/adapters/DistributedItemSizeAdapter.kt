package com.nochino.support.androidui.views.recyclerview.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.nochino.support.androidui.views.recyclerview.BaseRecyclerViewClickListener
import com.nochino.support.androidui.views.recyclerview.BaseViewHolder
import timber.log.Timber

/**
 * An adapter that basically defeats the purpose of a RecyclerView by attempting to
 * scale each item view to proportionately fit the size of the containing view.
 * @param context Context used for layout inflation and view creation
 * @param containerViewSize The size of the containing view. Used to determine how much
 * @param dataList The data set list
 * to scale each item view so that all item views are sized proportionately to fill the
 * container view. All items will be visible in the RecyclerView if this is possible.
 */
abstract class DistributedItemSizeAdapter<T, L : BaseRecyclerViewClickListener<T>, VH : BaseViewHolder<T, L>>
    (context: Context, private val containerViewSize: Int, dataList: MutableList<T>) :
    BaseRecyclerViewAdapter<
            T,
            BaseRecyclerViewClickListener<T>,
            BaseViewHolder<T, BaseRecyclerViewClickListener<T>>>(context, dataList) {

    /** The x/y size for each of the RecyclerView items */
    private val averagedItemSize: Int = Math.floor(
        containerViewSize.toDouble() / items.size.toDouble()
    ).toInt()

    /** The number of remaining pixels left on the screen after calculating the [averagedItemSize] **/
    private var totalRemainderPixels = containerViewSize % items.size

    /** The data position used to calculate the remaining pixel distribution **/
    private var distributionPosition = 0

    /**
     * The value by which the remaining pixels will be distributed amongst the remaining
     * values in the data set.
     */
    private val remainderPixelsDistribution: Double
        get() {
            return if (totalRemainderPixels == 0) {
                0.toDouble()
            } else {
                totalRemainderPixels / (items.size.toDouble() - distributionPosition)
            }
        }

    /** Running total of the remainder pixel distribution. */
    private var runningDistributionTotal = remainderPixelsDistribution

    /**
     * The cumulative total size of pixels taken up by views that have
     * been drawn on the screen.
     */
    private var currentCumTotalItemsSize = 0

    override fun onViewHolderCreated(parent: ViewGroup, holder: BaseViewHolder<T, BaseRecyclerViewClickListener<T>>) {
        val parentViewHeight = (parent.parent as ViewGroup).height

        var scaleItemSize = averagedItemSize
        if (scaleItemSize < 1) {
            scaleItemSize = 1
        }

        // Set the sizes of the item view so it's scaled to allow all RecyclerView
        // items to be visible in the RecyclerView
        holder.itemView.layoutParams.width = scaleItemSize
        holder.itemView.layoutParams.height = parentViewHeight
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T, BaseRecyclerViewClickListener<T>>, position: Int) {
        super.onBindViewHolder(holder, position)
        if (position == 0) {
            // Log the data at the start
            Timber.i("Starting...")
            Timber.d("Total width [$containerViewSize]")
            Timber.d("Average Item Size [$averagedItemSize]")
            Timber.d("Total Remainder Pixels [$totalRemainderPixels]")
            Timber.d("Remainder Pixel Distribution [$remainderPixelsDistribution]")
        }

        scaleItemView(holder.itemView, position)
    }

    private fun isScalable(containerViewSize: Int, totalRemainderPixels: Int): Boolean {
        return containerViewSize > totalRemainderPixels
    }

    /**
     * Scales the item view's size in a distribution of remaining pixels so
     * the RecyclerView can fill the screen with items.
     */
    private fun scaleItemView(itemView: View, position: Int) {
        // Add pixels to the item view until the remainder pixels has reached 0.
        // This ensures every list item fits in the display if the total items to
        // view dimension ratio isn't 1:1
        if (isScalable(containerViewSize, totalRemainderPixels)
            && totalRemainderPixels > 0
            && remainderPixelsDistribution != 0.0) {

            distributionPosition = position

            // Keep track of the current distribution count. Only until the
            // runningDistributionTotal reaches a whole number will the item
            // view at this position be scaled.
            runningDistributionTotal += remainderPixelsDistribution

            // Check if the distribution count has reached a whole
            if (Math.floor(runningDistributionTotal) > 1.0) {

                // Once the distribution count has reached a whole
                // number the current position's view should be increased
                val newWidth = itemView.layoutParams.width + 1
                itemView.layoutParams.width = newWidth

                // Decrement the remaining pixels left for distribution
                totalRemainderPixels --

                // Recalculate the running total distribution (see get method of remainderPixelsDistribution!)
                runningDistributionTotal = remainderPixelsDistribution

                Timber.w("Modded width at position [$position] with 1 more pixel!")
                Timber.w("Remaining pixels [$totalRemainderPixels]")
                Timber.w("Running distribution total [$runningDistributionTotal]")
            }
        }

        currentCumTotalItemsSize += itemView.layoutParams.width

        Timber.d("Current cumulative item size in pixels [$currentCumTotalItemsSize] ")

        if (position == items.size - 1) {
            Timber.i("Finished...")
            Timber.d("Total Remainder Pixels [$totalRemainderPixels]") // <- Should be 0 by now!

            val endPixelDifference = containerViewSize - currentCumTotalItemsSize

            // If there's some leftover pixels....just add them to the last item
            if (endPixelDifference > 0) {
                Timber.d("There are leftover pixels! Adding [$endPixelDifference] pixels to the last item width!")
                itemView.layoutParams.width += containerViewSize - currentCumTotalItemsSize
            }
        }
    }
}