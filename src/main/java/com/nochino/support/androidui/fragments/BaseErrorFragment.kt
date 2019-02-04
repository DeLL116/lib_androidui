package com.nochino.support.androidui.fragments

import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.nochino.support.androidui.R

/**
 * Extension of [BaseFragment] that can be subclassed to show / handle
 * an error. It has the capabilities to display an image representing the
 * error, the reason for the error with text, and a button to allow the user
 * to perform an action.
 */
abstract class BaseErrorFragment : BaseFragment() {

    private var mImageView: ImageView? = null
    private var mTextView: TextView? = null
    private var mErrorFrame: View? = null
    private var mButton: Button? = null

    var mButtonClickListener: View.OnClickListener? = null
        set(value) {
            field = value
            updateButton()
        }

    var mMessage: CharSequence? = null
        set(value) {
            field = value
            updateMessage()
        }

    var mButtonText: String? = null
        set(value) {
            field = value
            updateButton()
        }

    var mDrawable: Drawable? = null
        set(value) {
            field = value
            updateImageDrawable()
        }

    var mBackgroundDrawable: Drawable? = null
        set(value) {
            field = value
            if (value != null) {
                val opacity = field?.opacity
                mIsBackgroundTranslucent = opacity == PixelFormat.TRANSLUCENT || opacity == PixelFormat.TRANSPARENT
            }
            updateBackground()
            updateMessage()
        }

    private var mIsBackgroundTranslucent = true

    /**
     * Sets the default background.
     *
     * @param translucent True to set a translucent background.
     */
    fun setDefaultBackground(translucent: Boolean) {
        mIsBackgroundTranslucent = translucent
        mBackgroundDrawable = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.error_layout, container, false)

        mErrorFrame = root.findViewById(R.id.error_frame_root)
        updateBackground()

        mImageView = root.findViewById(R.id.error_image)
        updateImageDrawable()

        mTextView = root.findViewById(R.id.error_message)
        updateMessage()

        mButton = root.findViewById(R.id.error_action_button)
        updateButton()

        mTextView?.let { textView ->
            val metrics = getFontMetricsInt(textView)
            val underImageBaselineMargin = container?.resources?.getDimensionPixelSize(
                R.dimen.error_fragment_under_image_baseline_margin
            )
            underImageBaselineMargin?.let {
                setTopMargin(textView, underImageBaselineMargin + metrics.ascent)
            }

            val underMessageBaselineMargin = container?.resources?.getDimensionPixelSize(
                R.dimen.error_fragment_under_message_baseline_margin
            )

            underMessageBaselineMargin?.let { margin ->
                mButton?.let { button ->
                    setTopMargin(button, margin - metrics.descent)
                }
            }
        }

        return root
    }

    override fun onStart() {
        super.onStart()
        mErrorFrame?.requestFocus()
    }

    private fun updateBackground() {
        mErrorFrame?.let { errorFrame ->
            if (mBackgroundDrawable != null) {
                errorFrame.background = mBackgroundDrawable
            } else {
                val color =
                    if (mIsBackgroundTranslucent) {
                        R.color.error_fragment_background_translucent
                    } else {
                        R.color.error_fragment_background_opaque
                    }
                errorFrame.setBackgroundColor(
                    ContextCompat.getColor(errorFrame.context, color)
                )
            }
        }
    }

    private fun updateMessage() {
        mTextView?.let {
            it.text = mMessage
            it.visibility = if (TextUtils.isEmpty(mMessage)) View.GONE else View.VISIBLE
        }
    }

    private fun updateImageDrawable() {
        mImageView?.let {
            it.setImageDrawable(mDrawable)
            it.visibility = if (mDrawable == null) View.GONE else View.VISIBLE
        }
    }

    private fun updateButton() {
        mButton?.let {
            it.text = mButtonText
            it.setOnClickListener(mButtonClickListener)
            it.visibility = if (TextUtils.isEmpty(mButtonText)) View.GONE else View.VISIBLE
            it.requestFocus()
        }
    }

    private fun getFontMetricsInt(textView: TextView): FontMetricsInt {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textSize = textView.textSize
        paint.typeface = textView.typeface
        return paint.fontMetricsInt
    }

    private fun setTopMargin(textView: TextView, topMargin: Int) {
        val lp = textView.layoutParams as ViewGroup.MarginLayoutParams
        lp.topMargin = topMargin
        textView.layoutParams = lp
    }
}