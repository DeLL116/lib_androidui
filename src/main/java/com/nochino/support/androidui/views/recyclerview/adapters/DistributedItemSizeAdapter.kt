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
enum class DistributionAxis {
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
 * @param distributionAxis The axis on which view size scaling and distribution will occur.  See [DistributionAxis]
 * @param totalDistributionSizePixels The size in pixels available to draw all of the recycler views items
 * @param dataList The data
 */
abstract class DistributedItemSizeAdapter<T, L : BaseRecyclerViewClickListener<T>, VH : BaseViewHolder<T, L>>(
    context: Context,
    private val distributionAxis: DistributionAxis,
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

    /** The number of remaining pixels left on the screen after calculating the [averagedItemSize] **/
    private var totalRemainderPixels = totalDistributionSizePixels % items.size

    /** The data position used to calculate the remaining pixel distribution **/
    private var distributionPosition = 0

    /**
     * The value by which the remaining pixels will be distributed amongst the remaining
     * values in the data set based on the current position and remaining pixels left to
     * distribute
     */
    private val remainderPixelsDistribution: Double
        get() {
            return if (totalRemainderPixels == 0) {
                0.toDouble()
            } else {
                totalRemainderPixels / (items.size.toDouble() - distributionPosition)
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
        setScaleDistributionViewSize(averagedItemSize, holder.itemView, distributionAxis)
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
        if (isScalable(totalDistributionSizePixels, totalRemainderPixels)
            && totalRemainderPixels > 0
            && remainderPixelsDistribution != 0.0) {

            distributionPosition = position

            // Keep track of the current distribution count. Only until the
            // runningDistributionCount reaches a whole number will the item
            // view at this position be scaled.
            runningDistributionCount += remainderPixelsDistribution

            // Check if the distribution count has reached a whole number
            if (Math.floor(runningDistributionCount) > 1.0) {

                // Once the distribution count has reached a whole
                // number the current position's view size should be increased on the distribution axis

                val newSize = getScaleDistributionViewSize(itemView, distributionAxis) + 1
                setScaleDistributionViewSize(newSize, itemView, distributionAxis)

                // Decrement the remaining pixels left for distribution
                totalRemainderPixels --

                // Recalculate the running total distribution (see get method of remainderPixelsDistribution!)
                runningDistributionCount = remainderPixelsDistribution

                Timber.w("Modded view size on axis [${distributionAxis.name}] at position [$position] with 1 more pixel!")
                Timber.w("Remaining pixels left to distribute [$totalRemainderPixels]")
                Timber.w("Running distribution total [$runningDistributionCount]")
            }
        }

        currentCumTotalItemsSize += getScaleDistributionViewSize(itemView, distributionAxis)

        Timber.d("Current cumulative item size in pixels [$currentCumTotalItemsSize] ")

        // If this is the last position and there's still some leftover pixels....just add them to the last item
        if (position == items.size - 1) {
            Timber.i("Finished...Total Remainder Pixels [$totalRemainderPixels]") // <- Should be close to 0 by now!

            if (totalRemainderPixels > 0) {
                Timber.d("There are leftover pixels! Adding [%s] pixels to the size of the last item view's [%s] axis!",
                    totalRemainderPixels,
                    distributionAxis.name
                )

                setScaleDistributionViewSize(
                    getScaleDistributionViewSize(itemView, distributionAxis) + (totalRemainderPixels),
                    itemView,
                    distributionAxis
                )
            }
        }
    }

    /**
     * Sets the new size of the [view] according to the provided [distributionAxis]
     */
    private fun setScaleDistributionViewSize(sizePixel: Int, view: View, distributionAxis: DistributionAxis) =
        when (distributionAxis) {
            DistributionAxis.X -> view.layoutParams.width = sizePixel
            DistributionAxis.Y -> view.layoutParams.height = sizePixel
        }

    /**
     * @return The current size of the [view] according to the provided [distributionAxis]
     */
    private fun getScaleDistributionViewSize(view: View, distributionAxis: DistributionAxis) =
        when (distributionAxis) {
            DistributionAxis.X -> view.layoutParams.width
            DistributionAxis.Y -> view.layoutParams.height
        }
}