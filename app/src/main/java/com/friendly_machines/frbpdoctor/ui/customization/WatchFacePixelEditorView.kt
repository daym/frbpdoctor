package com.friendly_machines.frbpdoctor.ui.customization

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class WatchFacePixelEditorView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var bitmap: Bitmap? = null
    private val paint = Paint().apply {
        isAntiAlias = true
    }

    // Set the bitmap to be displayed in the view
    fun setImageBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        invalidate() // Redraw the view
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, paint)
        }
    }

    // Handle touch events for basic editing functionality
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                // Example: Draw a circle where the user touches
                bitmap?.let {
                    val x = event.x.toInt()
                    val y = event.y.toInt()
                    it.setPixel(x, y, 0xFFFF0000.toInt()) // Set pixel to red
                    invalidate() // Redraw the view
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
