package com.nochino.support.androidui.views

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.AttributeSet
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import com.squareup.picasso.Target

class NetworkImageView: ImageView, PathSegmentProvider {

    // Doing this will create a map when height and width is 0 (View isn't initialized yet)
    // Use it to write a test
//    override val map: Map<String, String> = mapOf(
//                PathSegmentModifier.widthPathSegment to width.toString(),
//                PathSegmentModifier.heightPathSegment to height.toString()
//            )


    // Must be initialized in onAttachedToWindow in a custom view!
    override lateinit var moddedSegmentsMap: Map<String, String>

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var networkImageUrl: String? = null

    private val picasso: Picasso
        get() = Picasso.get()
            .apply {
                // Automatically apply debugging if build config is debug
                if (0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) {
                    isLoggingEnabled = true
                    setIndicatorsEnabled(true)
                }
            }

    private var currentRequest: RequestCreator? = null

    /**
     * Loads an image from the network with Picasso defaults and Picasso FIT
     */
    fun loadNetworkImageWithFit(networkImageUrl: String?, target: Target? = null) {
        loadRequest(networkImageUrl)
        if (target != null) currentRequest?.into(target) else currentRequest?.into(this)
    }

    /**
     * Loads an image from the network with Picasso defaults
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun loadNetworkImage(networkImageUrl: String?, target: Target? = null) {
        loadRequest(networkImageUrl)
        if (target != null) currentRequest?.into(target) else currentRequest?.into(this)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        changed.let {
            // This map must be initialized in onLayout so the width and the height
            // of the *drawn* view can be derived!
            moddedSegmentsMap = mapOf(
                PathSegmentModifier.widthPathSegment to width.toString(),
                PathSegmentModifier.heightPathSegment to height.toString()
            )
        }
    }

    fun loadNetworkImageWithPathSegmentModifier(pathSegmentModifier: PathSegmentModifier?, target: Target) {

        this.networkImageUrl = pathSegmentModifier?.getModdedSegmentsUrl(moddedSegmentsMap)

        // TODO :: Write Test when height and/or width is 0
        // ---> Comment-out the initialization of the moddedSegmentsMap in onLayout
        // ---> Initialize the map in class variable (commented-out above)

        this.networkImageUrl?.let { loadNetworkImage(it, target) }
    }

    private fun loadRequest(networkImageUrl: String?): RequestCreator {

        // Cancel any previous request (useful in AdapterView's that recycle views)
        cancelPreviousRequest(this.networkImageUrl)

        this.networkImageUrl = networkImageUrl

        this.currentRequest = networkImageUrl?.let { picasso.load(networkImageUrl).tag(it) }

        this.currentRequest?.let {
            return it
        }
    }

    private fun cancelPreviousRequest(tag: String?) {
        tag?.let {
            picasso.cancelTag(it)
        }
    }
}