package com.tapcounter.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView

class StrokeTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var strokeColor: Int = Color.TRANSPARENT
    private var strokeWidthPx: Float = 0f

    init {
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.StrokeTextView)
            strokeColor = ta.getColor(R.styleable.StrokeTextView_strokeColor, Color.TRANSPARENT)
            strokeWidthPx = ta.getDimension(R.styleable.StrokeTextView_strokeWidth, 0f)
            ta.recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        // 商务藏青为纯色背景，文字无需描边
        if (ThemeManager.current(context) != ThemeManager.BUSINESS &&
            strokeColor != Color.TRANSPARENT && strokeWidthPx > 0f
        ) {
            val layout = layout
            if (layout != null) {
                val tp = paint
                val savedStyle = tp.style
                tp.style = Paint.Style.STROKE
                tp.strokeWidth = strokeWidthPx
                tp.color = strokeColor
                tp.isAntiAlias = true
                val g = gravity
                for (i in 0 until layout.lineCount) {
                    val baseline = layout.getLineBaseline(i) + paddingTop.toFloat()
                    val lineWidth = layout.getLineWidth(i)
                    val x = when (g and Gravity.HORIZONTAL_GRAVITY_MASK) {
                        Gravity.CENTER_HORIZONTAL ->
                            paddingLeft + (width - paddingLeft - paddingRight - lineWidth) / 2f
                        Gravity.RIGHT -> width - lineWidth - paddingRight.toFloat()
                        else -> paddingLeft.toFloat()
                    }
                    canvas.drawText(
                        layout.text,
                        layout.getLineStart(i),
                        layout.getLineEnd(i),
                        x,
                        baseline,
                        tp
                    )
                }
                tp.style = savedStyle
            }
        }
        super.onDraw(canvas)
    }
}
