package com.nochino.support.androidui.views.recyclerview.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.nochino.support.androidui.views.recyclerview.BaseRecyclerViewClickListener
import com.nochino.support.androidui.views.recyclerview.BaseViewHolder
import timber.log.Timber

/**
 * Defines the dimension axis to use for the view.
 */
enum class ScaleAxis {
    /**
     * The X (horizontal) axis
     */
    X,
    /**
     * The Y (vertical) axis
     */
    Y
}

/**
 * An adapter that basically defeats the purpose of a RecyclerView by attempting to
 * scale each item view's size so all of them are visible and fit into the size of the containing view.
 * @param context Context used for layout inflation and view creation
 * @param scaleAxis The axis on which view size scaling and distribution will occur.  See [ScaleAxis]
 * @param totalDistributionSizePixels The size in pixels available to draw all of the recycler views items
 * @param dataList The data
 */
abstract class DistributedItemSizeAdapter<T, L : BaseRecyclerViewClickListener<T>, VH : BaseViewHolder<T, L>>(
    context: Context,
    val scaleAxis: ScaleAxis,
    private val totalDistributionSizePixels: Int,
    dataList: MutableList<T>
) : BaseRecyclerViewAdapter<
        T,
        BaseRecyclerViewClickListener<T>,
        BaseViewHolder<T, BaseRecyclerViewClickListener<T>>>(context, dataList) {

    /** The x/y size for each of the RecyclerView items */
    private val averagedItemSize: Int by lazy {
        val averageSize = Math.floor(totalDistributionSizePixels.toDouble() / items.size.toDouble()).toInt()
        if (averageSize < 1) {
            return@lazy 1
        }
        return@lazy averageSize
    }

    /**
     * The number of remaining pixels left on the screen after calculating the [averagedItemSize] that each
     * RecyclerView item will be.
     */
    private var totalRemainderPixels = totalDistributionSizePixels % items.size

    /**
     * Keeps track of the data position used to calculate distribution ratio per item.
     * Needed by [remainderPixelsDistribution] to dynamically calculate how many pixels
     * are left to be distributed amongst the remaining RecyclerView item views.
     */
    private var currentDistributionPosition = 0

    /**
     * The remaining pixels left to be distributed among each remaining RecyclerView item view.
     * A value of .25 means that for every 4th item, 1 pixel is added. This value can (and will)
     * change as [totalRemainderPixels] are distributed amongst the RecyclerView item views.
     */
    private val remainderPixelsDistribution: Double
        get() {
            return if (totalRemainderPixels == 0) {
                0.toDouble()
            } else {
                totalRemainderPixels / (items.size.toDouble() - currentDistributionPosition)
            }
        }

    /** Running total of a remainder pixels that have been distributed.*/
    private var runningDistributionCount = remainderPixelsDistribution

    /**
     * The cumulative total size (in pixels) taken up by item views that have
     * been added to the RecyclerView.
     */
    private var currentCumTotalItemsSize = 0

    override fun onViewHolderCreated(parent: ViewGroup, holder: BaseViewHolder<T, BaseRecyclerViewClickListener<T>>) {
        // Set the sizes of the item view so it's scaled to allow all RecyclerView
        // items to be visible in the RecyclerView
        setScaleDistributionViewSize(averagedItemSize, holder.itemView, scaleAxis)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T, BaseRecyclerViewClickListener<T>>, position: Int) {
        super.onBindViewHolder(holder, position)
        if (position == 0) {
            // Log the data at the start
            Timber.i("Starting...")
            Timber.d("Total size available for distribution [$totalDistributionSizePixels]")
            Timber.d("Average Item Size [$averagedItemSize]")
            Timber.d("Total Remainder Pixels [$totalRemainderPixels]")
            Timber.d("Remainder Pixel Distribution [$remainderPixelsDistribution]")
        }

        scaleItemView(holder.itemView, position)
    }

    /**
     * @return True if the total number of items can be scaled to fit the available
     * space in the [totalDistributionSizePixels].
     * For example : totalDistributionSize = 1080 // itemCount == 1080
     * --> True...each RecyclerView item will be scaled to be 1 pixel in size on the [scaleAxis]
     */
    private fun isScalable(): Boolean {
        return totalDistributionSizePixels >= itemCount
                && totalRemainderPixels > 0
                && remainderPixelsDistribution != 0.0
    }

    /**
     * Scales the item view's size in a distribution of remaining pixels so
     * the RecyclerView can fill the screen with items.
     */
    private fun scaleItemView(itemView: View, position: Int) {
        // Add pixels to the item view until the remainder pixels has reached 0.
        // This ensures every list item fits in the display if the total items to
        // view dimension ratio isn't 1:1
        if (isScalable()) {

            currentDistributionPosition = position

            // Keep track of the current distribution count. Only until the
            // runningDistributionCount reaches a whole number will the item
            // view at this position be scaled.
            runningDistributionCount += remainderPixelsDistribution

            // Check if the distribution count has reached a whole number
            if (Math.floor(runningDistributionCount) > 1.0) {

                // Once the distribution count has reached a whole
                // number the current position's view size should be increased on the distribution axis

                val newSize = getScaleDistributionViewSize(itemView, scaleAxis) + 1
                setScaleDistributionViewSize(newSize, itemView, scaleAxis)

                // Decrement the remaining pixels left for distribution
                totalRemainderPixels --

                // Recalculate the running total distribution (see get method of remainderPixelsDistribution!)
                runningDistributionCount = remainderPixelsDistribution

                Timber.w("Modded view size on axis [${scaleAxis.name}] at position [$position] with 1 more pixel!")
                Timber.w("Remaining pixels left to distribute [$totalRemainderPixels]")
                Timber.w("Running distribution total [$runningDistributionCount]")
            }

            // If this is the last position and there's still some leftover pixels....just add them to the last item
            if (position == items.size - 1) {
                Timber.i("Finished...Total Remainder Pixels [$totalRemainderPixels]") // <- Should be close to 0 by now!

                if (totalRemainderPixels > 0) {
                    Timber.d("There are leftover pixels! Adding [%s] pixels to the size of the last item view's [%s] axis!",
                        totalRemainderPixels,
                        scaleAxis.name
                    )

                    setScaleDistributionViewSize(
                        getScaleDistributionViewSize(itemView, scaleAxis) + (totalRemainderPixels),
                        itemView,
                        scaleAxis
                    )
                }
            }
        }

        currentCumTotalItemsSize += getScaleDistributionViewSize(itemView, scaleAxis)

        Timber.d("Current cumulative item size in pixels [$currentCumTotalItemsSize] ")
    }

    /**
     * Sets the new size of the [view] according to the provided [scaleAxis]
     */
    private fun setScaleDistributionViewSize(sizePixel: Int, view: View, scaleAxis: ScaleAxis) {
        var scaled = false
        when (scaleAxis) {
            ScaleAxis.X -> view.layoutParams.width = sizePixel.also { scaled = true }
            ScaleAxis.Y -> view.layoutParams.height = sizePixel.also { scaled = true }
        }

        if (scaled) {
            onViewScaled(view)
        }
    }

    /**
     * @return The current size of the [view] according to the provided [scaleAxis]
     */
    fun getScaleDistributionViewSize(view: View, scaleAxis: ScaleAxis) =
        when (scaleAxis) {
            ScaleAxis.X -> view.layoutParams.width
            ScaleAxis.Y -> view.layoutParams.height
        }

    /**
     * Invoked after a view has been scaled
     */
    abstract fun onViewScaled(view: View)
}