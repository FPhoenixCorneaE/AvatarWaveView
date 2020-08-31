package com.fphoenixcorneae.wave

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin

/**
 * Created by yuanshuobin on 16/8/26.
 */
class AvatarWaveView : View {
    // if true, the shader will display the wave
    var isShowWave = false

    // shader containing repeated waves
    private var mWaveShader: BitmapShader? = null

    // shader matrix
    private var mShaderMatrix: Matrix? = null

    // paint to draw wave
    private var mViewPaint: Paint? = null
    private var mDefaultAmplitude = 0f
    private var mDefaultWaterLevel = 0f
    private var mDefaultWaveLength = 0f
    private var mDefaultAngularFrequency = 0.0
    private var mAmplitudeRatio = DEFAULT_AMPLITUDE_RATIO
    private val mWaveLengthRatio = DEFAULT_WAVE_LENGTH_RATIO
    private var mWaterLevelRatio = DEFAULT_WATER_LEVEL_RATIO
    private var mWaveShiftRatio = DEFAULT_WAVE_SHIFT_RATIO
    private var mBehindWaveColor = DEFAULT_BEHIND_WAVE_COLOR
    private var mFrontWaveColor = DEFAULT_FRONT_WAVE_COLOR

    constructor(
        context: Context?
    ) : super(context) {
        init()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        init()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        mShaderMatrix = Matrix()
        mViewPaint = Paint()
        mViewPaint!!.isAntiAlias = true
    }

    /**
     * Shift the wave horizontally according to `waveShiftRatio`.
     *
     * @param waveShiftRatio Should be 0 ~ 1. Default to be 0.
     * <br></br>Result of waveShiftRatio multiples width of WaveView is the length to shift.
     */
    var waveShiftRatio: Float
        get() = mWaveShiftRatio
        set(waveShiftRatio) {
            if (mWaveShiftRatio != waveShiftRatio) {
                mWaveShiftRatio = waveShiftRatio
                invalidate()
            }
        }

    /**
     * Set water level according to `waterLevelRatio`.
     *
     * @param waterLevelRatio Should be 0 ~ 1. Default to be 0.5.
     * <br></br>Ratio of water level to WaveView height.
     */
    var waterLevelRatio: Float
        get() = mWaterLevelRatio
        set(waterLevelRatio) {
            if (mWaterLevelRatio != waterLevelRatio) {
                mWaterLevelRatio = waterLevelRatio
                invalidate()
            }
        }

    //sin函数的height
    val sinHeight: Float
        get() = (height * mWaterLevelRatio
                + mAmplitudeRatio / DEFAULT_AMPLITUDE_RATIO * mDefaultAmplitude * sin(
            mWaveShiftRatio * width * mDefaultAngularFrequency
        )).toFloat()

    /**
     * Set vertical size of wave according to `amplitudeRatio`
     *
     * @param amplitudeRatio Default to be 0.05. Result of amplitudeRatio + waterLevelRatio should be less than 1.
     * <br></br>Ratio of amplitude to height of WaveView.
     */
    var amplitudeRatio: Float
        get() = mAmplitudeRatio
        set(amplitudeRatio) {
            if (mAmplitudeRatio != amplitudeRatio) {
                mAmplitudeRatio = amplitudeRatio
                invalidate()
            }
        }

    fun setWaveColor(behindWaveColor: Int, frontWaveColor: Int) {
        mBehindWaveColor = behindWaveColor
        mFrontWaveColor = frontWaveColor
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        createShader()
    }

    /**
     * Create the shader with default waves which repeat horizontally, and clamp vertically
     */
    private fun createShader() {
        mDefaultAngularFrequency =
            2.0f * Math.PI / DEFAULT_WAVE_LENGTH_RATIO / width
        mDefaultAmplitude = height * DEFAULT_AMPLITUDE_RATIO
        mDefaultWaterLevel = height * DEFAULT_WATER_LEVEL_RATIO
        mDefaultWaveLength = width.toFloat()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val wavePaint = Paint()
        wavePaint.strokeWidth = 2f
        wavePaint.isAntiAlias = true
        wavePaint.color = mFrontWaveColor

        // Draw default waves into the bitmap
        // y=Asin(ωx+φ)+h
        val endX = width + 1
        val endY = height + 1
        val waveY = FloatArray(endX)
        wavePaint.color = mBehindWaveColor
        for (beginX in 0 until endX) {
            val wx = beginX * mDefaultAngularFrequency
            val beginY =
                (mDefaultWaterLevel + mDefaultAmplitude * sin(wx)).toFloat()
            canvas.drawLine(beginX.toFloat(), beginY, beginX.toFloat(), -1f, wavePaint)
            waveY[beginX] = beginY
        }
        wavePaint.color = mFrontWaveColor
        val wave2Shift = (mDefaultWaveLength / 4).toInt()
        for (beginX in 0 until endX) {
            canvas.drawLine(
                beginX.toFloat(),
                waveY[(beginX + wave2Shift) % endX],
                beginX.toFloat(),
                -1f,
                wavePaint
            )
        }

        // use the bitmap to create the shader
        mWaveShader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP)
        mViewPaint!!.shader = mWaveShader
    }

    override fun onDraw(canvas: Canvas) {
        // modify paint shader according to mShowWave state
        if (isShowWave && mWaveShader != null) {
            // first call after mShowWave, assign it to our paint
            if (mViewPaint!!.shader == null) {
                mViewPaint!!.shader = mWaveShader
            }

            // scale shader according to mWaveLengthRatio and mAmplitudeRatio
            // this decides the size(mWaveLengthRatio for width, mAmplitudeRatio for height) of waves
            mShaderMatrix!!.setScale(
                mWaveLengthRatio / DEFAULT_WAVE_LENGTH_RATIO,
                mAmplitudeRatio / DEFAULT_AMPLITUDE_RATIO, 0f,
                mDefaultWaterLevel
            )
            // translate shader according to mWaveShiftRatio and mWaterLevelRatio
            // this decides the start position(mWaveShiftRatio for x, mWaterLevelRatio for y) of waves
            mShaderMatrix!!.postTranslate(
                mWaveShiftRatio * width,
                (DEFAULT_WATER_LEVEL_RATIO - (1f - mWaterLevelRatio)) * height
            )
            // assign matrix to invalidate the shader
            mWaveShader!!.setLocalMatrix(mShaderMatrix)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), mViewPaint!!)
        } else {
            mViewPaint!!.shader = null
        }
    }

    companion object {
        private const val DEFAULT_AMPLITUDE_RATIO = 0.05f
        private const val DEFAULT_WATER_LEVEL_RATIO = 0.5f
        private const val DEFAULT_WAVE_LENGTH_RATIO = 1.0f
        private const val DEFAULT_WAVE_SHIFT_RATIO = 0.0f
        val DEFAULT_BEHIND_WAVE_COLOR = Color.parseColor("#40b7d28d")
        val DEFAULT_FRONT_WAVE_COLOR = Color.parseColor("#80b7d28d")
    }
}