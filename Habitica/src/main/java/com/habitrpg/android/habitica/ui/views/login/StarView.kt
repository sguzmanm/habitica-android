package com.habitrpg.android.habitica.ui.views.login


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper

class StarView : AppCompatImageView {

    private var animator:ObjectAnimator?=null
    private var blinkDurations: List<Int>? = null
    private var blinkIndex = 0
    private var runBlink = false

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context) : super(context) {
        this.scaleType = ScaleType.CENTER
    }

    fun setStarSize(size: Int) {
        when (size) {
            0 -> {
                this.setImageBitmap(HabiticaIconsHelper.imageOfStarSmall())
            }
            1 -> {
                this.setImageBitmap(HabiticaIconsHelper.imageOfStarMedium())
            }
            2 -> {
                this.setImageBitmap(HabiticaIconsHelper.imageOfStarLarge())
            }
        }
    }

    fun setBlinkDurations(blinkDurations: List<Int>) {
        this.blinkDurations = blinkDurations
        runBlink()
    }

    private fun runBlink() {
        if (blinkIndex >= blinkDurations?.size ?: 0) {
            blinkIndex = 0
        }
        if(!runBlink){
            animator = ObjectAnimator.ofFloat<View>(this, View.ALPHA, 0f)
            animator?.let{
                it.duration = 1000
                it.repeatCount = 1
                it.repeatMode = ValueAnimator.REVERSE
                it.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        blinkIndex++
                        runBlink()
                    }
                })
                runBlink=true
            }
        }
        try {
            animator?.startDelay = blinkDurations?.get(blinkIndex)?.toLong() ?: 0
            animator?.start()
        } catch (ignored: NullPointerException) {
        }

    }

    override fun onDetachedFromWindow() {
        animator=null
        blinkDurations=null
        super.onDetachedFromWindow()
    }
}
