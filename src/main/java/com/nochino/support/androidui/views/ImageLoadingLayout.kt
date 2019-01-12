package com.nochino.support.androidui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import androidx.core.widget.ContentLoadingProgressBar
import com.nochino.support.androidui.R
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.image_loading_layout.view.*

/**
 * Custom [RelativeLayout] that contains a [NetworkImageView] and a [ContentLoadingProgressBar]
 * Use it to display a loading state when an image is requested from an external network source.
 * It displays a [ContentLoadingProgressBar] at the start of the image request
 * and dismisses it when the request had completed.
 */
class ImageLoadingLayout : RelativeLayout {

    constructor(context: Context) : super(context) { init(null, 0) }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { init(attrs, 0) }
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun init(attrs: AttributeSet?, defStyle: Int) {

        View.inflate(context, R.layout.image_loading_layout, this)
//        // Load XML defined attributes for custom view
//        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.ImageLoadingLayout, defStyle, 0)
//        // ---> Init any XML defined attributes here before recycle
//        typedArray.recycle()
    }

    /**
     * Callback set on the [Picasso] request to load an image via [loadNetworkImage].
     */
    private val photoThumbTarget = object : Target {
        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            image_loading_layout_progress_bar.visibility = View.VISIBLE
        }

        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            // TODO :: Define drawable failed bitmap
        }

        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            onImageRequestComplete(bitmap)
        }
    }

    private fun onImageRequestComplete(bitmap: Bitmap?) {

        image_loading_layout_progress_bar.visibility = View.INVISIBLE

        bitmap?.let {
            // Set the bitmap before making the ImageView visible with animation
            image_loading_layout_network_image_view.setImageBitmap(it)
            image_loading_layout_network_image_view.visibility = View.VISIBLE
            // Animate the content view to 100% opacity, and clear any animation
            // listener set on the view.
            image_loading_layout_network_image_view.animate().alpha(1f)
                .setDuration(200) // TODO :: Allow XML attribute setting for duration
                .start()
        }
    }

    fun resetNetworkImage() {
        // Set any current thumb ImageView to invisible to prevent any
        // potentially recycled ImageViews from showing older / recycled images
        (View.VISIBLE == image_loading_layout_network_image_view.visibility).apply {
            image_loading_layout_network_image_view.visibility = View.INVISIBLE
        }

        // Clear any bitmaps that may have been set on the ImageView
        image_loading_layout_network_image_view?.drawable?.apply {
            image_loading_layout_network_image_view.setImageBitmap(null)
        }

        // Set the alpha of the ImageView to 0 so that
        // when the target image is loaded the animation
        // will occur
        (image_loading_layout_network_image_view.alpha > 0.0f).let {
            image_loading_layout_network_image_view.alpha = 0.0f
        }
    }

    fun loadNetworkImage(networkImageUrl: String) {

        resetNetworkImage()

        // Post a runnable on the ImageView's handler
        // (to load the network image) to ensure the
        // width and height of the rendered ImageView
        // can be derived before requesting a pixel-perfect
        // image from the network resource.
        post {

            // TODO :: TEST :: Write test when pixel image height and width is explicit and when it's match_parent...etc

            // Only allow requesting pixel-perfect height and width image if the
            // network_image_view has explicit height + width set (ie...
            // wrap_content and / or match_parent is not used as height or width)
            val validateAllowPixelPerfectRequest =
                image_loading_layout_network_image_view.width > 0 && image_loading_layout_network_image_view.height > 0

            if (validateAllowPixelPerfectRequest) {

                image_loading_layout_network_image_view.loadNetworkImage(networkImageUrl, photoThumbTarget)

            } else {
                // Load with Picasso's "fit"
                image_loading_layout_network_image_view.loadNetworkImageWithFit(networkImageUrl, photoThumbTarget)
            }
        }
    }

    fun loadNetworkImage(pathSegmentIdentifier: PathSegmentModifier?) {
        resetNetworkImage()
        image_loading_layout_network_image_view.loadNetworkImageWithPathSegmentModifier(
            pathSegmentIdentifier,
            photoThumbTarget
        )
    }
}
