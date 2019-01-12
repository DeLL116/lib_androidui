package com.nochino.support.androidui.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import com.nochino.support.androidui.R
import kotlinx.android.synthetic.main.image_loading_layout.view.*
import kotlinx.android.synthetic.main.swappable_image_card_view.view.*

/**
 * A [CardView] that can be used in 2 different orientations
 */
class SwappableImageCardView : CardView {

    private var _orientation: Int = LinearLayout.HORIZONTAL
    private var _cardImageWidth: Int = LinearLayout.LayoutParams.MATCH_PARENT

    var orientation: Int
        get() = _orientation
        set(value) {
            _orientation = value
        }

    var cardImageWidth: Int
        get() = _cardImageWidth
        set(value) {
            _cardImageWidth = value
        }

    constructor(context: Context) : super(context) { init(null, 0) }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { init(attrs, 0) }
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {

        View.inflate(context, R.layout.swappable_image_card_view, this)

        // Load attributes
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.SwappableImageCardView, defStyle, 0)

        _orientation = typedArray.getInt(R.styleable.SwappableImageCardView_cardOrientation, _orientation)
        _cardImageWidth = typedArray.getDimensionPixelSize(R.styleable.SwappableImageCardView_cardImageWidth, _cardImageWidth)

        typedArray.recycle()

        // TODO :: Test orientation actually fits layout content
        card_content_holder_layout.orientation = orientation

        if (_orientation == LinearLayout.VERTICAL) {
            card_content_holder_layout.gravity = Gravity.CENTER
        }

        if (_cardImageWidth != LinearLayout.LayoutParams.MATCH_PARENT) {
            val layoutParams = RelativeLayout.LayoutParams(_cardImageWidth, _cardImageWidth)
            card_image_loading_layout.image_loading_layout_network_image_view.layoutParams = layoutParams
            card_image_loading_layout.image_loading_layout_network_image_view.requestLayout()
        }
    }

    fun setData(cardTitle: String?, cardDesc1: String?, cardDesc2: String?, cardDesc3: String?) {
        post {
            cardTitle?.let { card_title.text = cardTitle }
            cardDesc1?.let { card_desc_1.text = cardDesc1 }
            cardDesc2?.let { card_desc_2.text = cardDesc2 }
            cardDesc3?.let { card_desc_3.text = cardDesc3 }
        }
    }

    fun loadNetworkImage(imageUrl: String) {
        post {
            card_image_loading_layout.loadNetworkImage(imageUrl)
        }
    }

    fun loadNetworkImage(pathSegmentIdentifier: PathSegmentModifier) {
        post {
            card_image_loading_layout.loadNetworkImage(pathSegmentIdentifier)
        }
    }
}
