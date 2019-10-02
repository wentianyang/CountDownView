package com.ytw.countdownview.widget


import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import com.ytw.countdownview.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * @Author: Ytw
 * @Date: 2019/9/26 6:59
 * @Description:
 **/
private const val DEFAULT_SUFFIX = ":"
private const val DEFAULT_TIME_TEXT = "00"

class CountDownView : View {

  val TAG = "CountDownView"

  private val DEFAULT_SUFFIX_SIZE = 20f.sp2px()
  private val DEFAULT_TIME_TEXT_SIZE = 40f.sp2px()
  private val DEFAULT_BORDER_WIDTH = 1f.dp2px()

  private var mHour: Long = 0
    set(value) {
      if (field != value) {
        field = value
        invalidate()
      }
    }
  private var mMinute: Long = 0
    set(value) {
      if (field != value) {
        field = value
        invalidate()
      }
    }
  private var mSecond: Long = 0
    set(value) {
      if (field != value) {
        field = value
        invalidate()
      }
    }

  private var mPaddingLeft: Float = 0f.dp2px()
  private var mPaddingTop: Float = 0f.dp2px()
  private var mPaddingRight: Float = 0f.dp2px()
  private var mPaddingBottom: Float = 0f.dp2px()

  private var mSuffixMargin: Float = 10f.dp2px()

  private var mBorderRect: Rect = Rect()
  private val mTimeBounds: Rect = Rect()
  private val mSuffixBounds: Rect = Rect()

  private var mSuffixSize = DEFAULT_SUFFIX_SIZE
  private var mTimeTextSize = DEFAULT_TIME_TEXT_SIZE
  private var mBorderWidth = DEFAULT_BORDER_WIDTH

  private var mShowHour: Boolean = true
  private var mShowMinute: Boolean = true
  private var mShowSecond: Boolean = true
  private var mShowBorder: Boolean = true

  private val job by lazy { Job() }

  private var mTimePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.parseColor("#000000")
    textSize = mTimeTextSize
  }

  private var mSuffixPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.parseColor("#000000")
    textSize = mSuffixSize
  }

  private var mBorderPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.parseColor("#000000")
    strokeWidth = mBorderWidth
    style = Paint.Style.STROKE
  }

  init {
    mTimePaint.getTextBounds(DEFAULT_TIME_TEXT, 0, DEFAULT_TIME_TEXT.length, mTimeBounds)
    mSuffixPaint.getTextBounds(DEFAULT_SUFFIX, 0, DEFAULT_SUFFIX.length, mSuffixBounds)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    job.complete()
  }

  constructor(context: Context) : this(context, null)

  constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet!!, 0)

  constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr) {
    init(context, attributeSet)
  }

  private fun init(context: Context, attributeSet: AttributeSet?) {
    val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.CountDownView)
    mShowHour = typedArray.getBoolean(R.styleable.CountDownView_showHour, true)
    mShowMinute = typedArray.getBoolean(R.styleable.CountDownView_showMinute, true)
    mShowSecond = typedArray.getBoolean(R.styleable.CountDownView_showSecond, true)
    mShowBorder = typedArray.getBoolean(R.styleable.CountDownView_showBorder, true)
    typedArray.recycle()
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    Log.d(TAG, "onSizeChanged width is $w")
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    Log.d(TAG, "onMeasure")
    val widthSize = getContentWidth()
    val heightSize = getContentHeight()
    val measuredWidth = resolveSize(widthSize.toInt(), widthMeasureSpec)
    val measuredHeight = resolveSize(heightSize.toInt(), heightMeasureSpec)
    setMeasuredDimension(measuredWidth, measuredHeight)
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    Log.d(TAG, "onDraw")
    canvas.drawLine(0f, (height / 2).toFloat(), width.toFloat(), (height / 2).toFloat() + 1f.dp2px(), mBorderPaint)
    drawBorder(canvas, mBorderWidth / 2)
    drawText(canvas, formatTime(mHour), mBorderRect.left + mBorderWidth / 2)
    drawSuffix(canvas, mBorderRect.right + mBorderWidth / 2 + mSuffixMargin.toInt() - mSuffixBounds.left)

    drawBorder(canvas, mBorderRect.right + mBorderWidth + mSuffixMargin.toInt() * 2 + mSuffixBounds.width())
    drawText(canvas, formatTime(mMinute), mBorderRect.left + mBorderWidth / 2)
    drawSuffix(canvas, mBorderRect.right + mBorderWidth / 2 + mSuffixMargin.toInt() - mSuffixBounds.left)

    drawBorder(canvas, mBorderRect.right + mBorderWidth + mSuffixMargin.toInt() * 2 + mSuffixBounds.width())
    drawText(canvas, formatTime(mSecond), mBorderRect.left + mBorderWidth / 2)
  }

  private fun getContentWidth(): Float {
    return ((timeTextWidth() + mPaddingLeft.toInt() + mPaddingRight.toInt()) * 3 + mSuffixMargin.toInt() * 4 + mBorderWidth.toInt() * 6 + mTimeBounds.left * 3 + mSuffixBounds.width() * 2).toFloat()
  }

  private fun getContentHeight(): Float {
    return (timeTextHeight() + mBorderWidth * 2)
  }

  private fun textBaseLine(): Float {
    return ((height / 2f) - (timeTextHeightFont() / 2f))
  }

  private fun timeTextHeight(): Int {
    return mTimeBounds.height() + mPaddingTop.toInt() + mPaddingBottom.toInt()
  }

  private fun timeTextWidth(): Int {
    return mTimeBounds.width() - mTimeBounds.left
  }

  private fun timeTextHeightFont(): Float {
    val fontMetrics = Paint.FontMetrics()
    mTimePaint.getFontMetrics(fontMetrics)
    return fontMetrics.ascent + fontMetrics.descent
  }

  private fun suffixWidth(): Int {
    return mSuffixBounds.width() - mSuffixBounds.left
  }

  private fun drawSuffix(canvas: Canvas, x: Float) {
    canvas.drawText(DEFAULT_SUFFIX, x, height / 2f + mSuffixBounds.height() / 2f, mSuffixPaint)
  }

  private fun drawText(canvas: Canvas, text: String, x: Float) {
    canvas.drawText(text, x - mTimeBounds.left + mPaddingLeft, textBaseLine(), mTimePaint)
  }

  private fun drawBorder(canvas: Canvas, x: Float) {
    mBorderRect.left = x.toInt()
    mBorderRect.top = (0 + mBorderWidth / 2).toInt()
    mBorderRect.right = (mBorderRect.left + timeTextWidth() + mBorderWidth.toInt() + mTimeBounds.left + mPaddingLeft.toInt() + mPaddingRight.toInt())
    mBorderRect.bottom = (height / 2 - timeTextHeightFont() / 2f + mBorderWidth + mPaddingTop.toInt()).toInt()
    canvas.drawRect(mBorderRect, mBorderPaint)
  }

  private fun formatTime(time: Long): String {
    return if (time < 10) "0$time" else time.toString()
  }

  private fun setCountTime(ms: Long, delay: Long = 1000) {
    if (ms <= 0) {
      mListener?.onFinish()
      return
    }
    CoroutineScope(job).launch(Dispatchers.Main) {
      for (currTime in ms downTo 0 step delay) {
        updateTime(currTime)
        if (currTime <= 0) {
          mListener?.onFinish()
        } else {
          mListener?.onTiming(currTime)
          kotlinx.coroutines.delay(delay)
        }
      }
    }
  }

  private fun updateTime(ms: Long) {
    if (mShowHour) {
      mHour = ms / (1000 * 60 * 60)
      if (mShowMinute) {
        mMinute = (ms % (1000 * 60 * 60)) / (1000 * 60)
        if (mShowSecond) {
          mSecond = (ms % (1000 * 60)) / 1000
        }
      }
    } else {
      if (mShowMinute) {
        mMinute = (ms / (1000 * 60))
        if (mShowSecond) {
          mSecond = (ms % (1000 * 60)) / 1000
        }
      } else {
        mSecond = ms / 1000
      }
    }
  }

  fun start(ms: Long) {
    setCountTime(ms)
  }

  private fun Float.sp2px() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics)

  private fun Float.dp2px() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)

  private var mListener: Listener? = null

  interface Listener {
    fun onTiming(currentTimeMillis: Long)

    fun onFinish()
  }
}
