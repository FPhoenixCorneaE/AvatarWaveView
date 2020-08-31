package com.fphoenixcorneae.wave

import android.animation.*
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import java.util.*

/**
 * Created by yuanshuobin on 16/8/26.
 */
class AvatarWaveHelper(
    private val mAvatarWaveView: AvatarWaveView,
    private val mFloatView: View,
    behindWaveColor: Int,
    frontWaveColor: Int
) {
    private var mAnimatorSet: AnimatorSet? = null
    private var mAmplitudeAnim: ObjectAnimator? = null
    private var mAmplitudeChangeAnim: ObjectAnimator? = null
    private var mHasCancel = false
    private var mHasStart = false
    private var mIsFirst = false

    //水位高低 waterLevelRatio
    private var mDefaultWaterLevelRatioF = 0.8f
    private var mDefaultWaterLevelRatioT = 0.8f

    //波浪大小振幅 amplitudeRatio
    private var mDefaultAmplitudeRatioF = 0.05f
    private var mDefaultAmplitudeRatioT = 0.05f

    //floatView 旋转调整角
    private var mDefaultFloatViewRotation = 85f
    fun setDefaultWaterLevelRatio(
        defaultWaterLevelRatioF: Float,
        defaultWaterLevelRatioT: Float
    ) {
        mDefaultWaterLevelRatioF = defaultWaterLevelRatioF
        mDefaultWaterLevelRatioT = defaultWaterLevelRatioT
    }

    fun setDefaultAmplitudeRatio(
        defaultAmplitudeRatioF: Float,
        defaultAmplitudeRatioT: Float
    ) {
        mDefaultAmplitudeRatioF = defaultAmplitudeRatioF
        mDefaultAmplitudeRatioT = defaultAmplitudeRatioT
    }

    fun setDefaultFloatViewRotation(defaultFloatViewRotation: Float) {
        mDefaultFloatViewRotation = defaultFloatViewRotation
    }

    private fun initAnimation() {
        val animators: MutableList<Animator?> =
            ArrayList()
        // vertical animation.
        // water level increases from 0 to center of HeaderWaveView
        val waterLevelAnim = ObjectAnimator.ofFloat(
            mAvatarWaveView, "waterLevelRatio", mDefaultWaterLevelRatioF, mDefaultWaterLevelRatioT
        )
        waterLevelAnim.duration = 10000
        waterLevelAnim.interpolator = DecelerateInterpolator()


        // amplitude animation.
        // wave grows big then grows small, repeatedly
        mAmplitudeAnim = ObjectAnimator.ofFloat(
            mAvatarWaveView, "amplitudeRatio", mDefaultAmplitudeRatioF, mDefaultAmplitudeRatioT
        )
        mAmplitudeAnim!!.repeatCount = ValueAnimator.INFINITE
        mAmplitudeAnim!!.repeatMode = ValueAnimator.REVERSE
        mAmplitudeAnim!!.duration = 5000
        mAmplitudeAnim!!.interpolator = LinearInterpolator()


        // horizontal animation.
        // wave waves infinitely.
        val waveShiftAnim = ObjectAnimator.ofFloat(
            mAvatarWaveView, "waveShiftRatio", 0f, 1f
        )
        waveShiftAnim.repeatCount = ValueAnimator.INFINITE
        waveShiftAnim.duration = 2000
        waveShiftAnim.interpolator = LinearInterpolator()
        waveShiftAnim.addUpdateListener { //获取sin函数的height，更新mFloatView
            val value =
                mAvatarWaveView.sinHeight - mFloatView.measuredHeight
            mFloatView.rotation = value + mDefaultFloatViewRotation
            mFloatView.translationY = value
            mFloatView.invalidate()
        }
        animators.add(waveShiftAnim)
        animators.add(mAmplitudeAnim)
        animators.add(waterLevelAnim)
        mAnimatorSet = AnimatorSet()
        mAnimatorSet!!.playTogether(animators)
    }

    fun start() {
        mAvatarWaveView.isShowWave = true
        if (mAnimatorSet != null && !mHasStart) {
            mHasStart = true
            mHasCancel = false
            if (mAmplitudeChangeAnim != null) {
                mAmplitudeChangeAnim = ObjectAnimator.ofFloat(
                    mAvatarWaveView, "amplitudeRatio", 0.00001f, mDefaultAmplitudeRatioF
                )
                mAmplitudeChangeAnim!!.duration = 1000
                mAmplitudeChangeAnim!!.start()
            }
            if (!mIsFirst) {
                mAnimatorSet!!.start()
                mIsFirst = true
            }
        }
    }

    fun cancel() {
        if (mAnimatorSet != null && !mHasCancel) {
            mHasCancel = true
            mHasStart = false
            mAmplitudeAnim?.cancel()
            mAmplitudeChangeAnim = ObjectAnimator.ofFloat(
                mAvatarWaveView, "amplitudeRatio", mDefaultAmplitudeRatioT, 0.00001f
            )
            mAmplitudeChangeAnim!!.duration = 1000
            mAmplitudeChangeAnim!!.addListener(object : AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                    if (mFloatView.animation != null) {
                        mFloatView.animation.cancel()
                    }
                }
            })
            mAmplitudeChangeAnim!!.start()
        }
    }

    init {
        mAvatarWaveView.setWaveColor(behindWaveColor, frontWaveColor)
        initAnimation()
    }
}