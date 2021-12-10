package com.intretech.app.umsdashboard_new.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.ProgressBar
import com.intretech.app.umsdashboard_new.R
import kotlin.math.abs

class UpdateVersionProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.progressBarStyleHorizontal
) : ProgressBar(context, attrs, defStyleAttr) {

    private var mSettings: ProgressBarSettings = ProgressBarSettings(context, attrs)

    private var mPaint: Paint = Paint()   //所有画图所用的画笔
    private var mRealWidth = 0    //真正的宽度值是减去左右padding

    init {
        mPaint.textSize = mSettings.mProgressTextSize.toFloat()
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.isDither = true
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height: Int = measureHeight(heightMeasureSpec)
        setMeasuredDimension(width, height)
        mRealWidth = measuredWidth - paddingRight - paddingLeft
    }


    private fun measureHeight(measureSpec: Int): Int {
        var result: Int
        val specMode = MeasureSpec.getMode(measureSpec) //父布局告诉我们控件的类型
        val specSize = MeasureSpec.getSize(measureSpec) //父布局传过来的视图大小
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            val textHeight = (mPaint.descent() - mPaint.ascent()).toInt() // 设置文本的高度
            val barMaxHeight = getMax(mSettings.mReachedHeight , mSettings.mUnreachedHeight)
            val tempH = paddingTop + paddingBottom + mSettings.mProgressTextMarginTop + barMaxHeight
            result = tempH.toInt() + if (mSettings.mNeedDrawText) abs(textHeight) else 0
            if (specMode == MeasureSpec.AT_MOST) {
                result =getMin( result , specSize)
            }
        }
        return result
    }


    override fun onDraw(canvas: Canvas) {
        /**
         * 设置偏移后的坐标原点 以原来为基础上偏移后， 例如： (100,100), translate(1,1), 坐标原点(101,101);
         */
        canvas.save()
        val bH = getMax(  mSettings.mReachedHeight , mSettings.mUnreachedHeight)
        val halfURH = mSettings.mUnreachedHeight / 2
        val halfRH = mSettings.mReachedHeight / 2
        canvas.translate(paddingLeft.toFloat(), bH / 2)

        val radio = progress * 1.0f / max //设置进度
        val pX = mRealWidth * radio //设置当前进度的宽度

        // draw unreached bar
        mPaint.color = mSettings.mUnreachedColor
        mPaint.strokeWidth = mSettings.mUnreachedHeight
        canvas.drawLine(pX + halfURH, 0f, mRealWidth - halfURH, 0f, mPaint)

        // draw reached bar
        if (pX > 0) {
            mPaint.color = mSettings.mReachedColor
            mPaint.strokeWidth = mSettings.mReachedHeight
            val end = if (pX <= halfRH * 2) halfRH else pX - halfRH
            canvas.drawLine(halfRH, 0f, end, 0f, mPaint)
        }

        // draw text
        val text = "${(radio * 100).toInt()}%"
        val textWidth = mPaint.measureText(text)                    //返回文本的宽度
        val textHeight = measureTextHeight(mPaint)  //设置文本的高度
        if (mSettings.mNeedDrawText) { //绘制文本
            mPaint.color = mSettings.mProgressTextColor
            val y = bH / 2.5f + textHeight + mSettings.mProgressTextMarginTop
            val x = if (pX < textWidth) 0f else pX - textWidth
            canvas.drawText(text, x, y, mPaint)
        }
        canvas.restore()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mRealWidth = w - paddingRight - paddingLeft
    }


    /**
     * 测量文字的高度
     */
    private fun measureTextHeight(paint: Paint): Float {
        val height: Float
        val fontMetrics = paint.fontMetrics
        height = fontMetrics.descent - fontMetrics.ascent
        return height
    }

    fun getMax(one: Float, two: Float) = if (one > two) one else two
    fun getMin(one: Int, two: Int) = if (one < two) one else two

    class ProgressBarSettings internal constructor(
        context: Context, attrs: AttributeSet?
    ) {
        val mUnreachedHeight: Float
        val mReachedHeight: Float
        val mProgressTextColor: Int
        val mProgressTextSize: Int
        val mUnreachedColor: Int
        val mProgressTextMarginTop: Int
        val mReachedColor: Int
        val mNeedDrawText: Boolean

        init {
            val attributes =
                context.obtainStyledAttributes(attrs, R.styleable.UpdateVersionProgressBar)

            mProgressTextSize = attributes.getDimensionPixelSize(
                R.styleable.UpdateVersionProgressBar_uvProgressTextSize,
                px2sp(context, 16)
            )
            mUnreachedHeight = attributes.getDimension(
                R.styleable.UpdateVersionProgressBar_uvUnreachedBarHeight,
                px2dip(context, 2).toFloat()
            )
            mProgressTextMarginTop = attributes.getDimensionPixelSize(
                R.styleable.UpdateVersionProgressBar_uvProgressTextMarginTop,
                px2dip(context, 2)
            )
            mReachedHeight = attributes.getDimension(
                R.styleable.UpdateVersionProgressBar_uvReachedBarHeight,
                px2dip(context, 12).toFloat()
            )
            mProgressTextColor =
                attributes.getColor(
                    R.styleable.UpdateVersionProgressBar_uvProgressTextColor,
                    Color.BLACK
                )
            mUnreachedColor =
                attributes.getColor(
                    R.styleable.UpdateVersionProgressBar_uvUnreachedColor,
                    Color.GREEN
                )
            mReachedColor =
                attributes.getColor(
                    R.styleable.UpdateVersionProgressBar_uvReachedColor,
                    Color.YELLOW
                )
            mNeedDrawText = attributes.getInt(
                R.styleable.UpdateVersionProgressBar_uvTextVisibility,
                VISIBLE
            ) == 0
            attributes.recycle()
        }

        // 将px值转换为dip或dp值
        fun px2dip(context: Context, pxValue: Int): Int {
            val scale = context.resources.displayMetrics.density
            return (pxValue.toFloat() / scale + 0.5f).toInt()
        }

        // 将px值转换为sp值
        fun px2sp(context: Context, pxValue: Int): Int {
            val fontScale = context.resources.displayMetrics.scaledDensity
            return (pxValue.toFloat() / fontScale + 0.5f).toInt()
        }
    }
}