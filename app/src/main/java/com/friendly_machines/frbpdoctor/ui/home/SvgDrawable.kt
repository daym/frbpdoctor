package com.friendly_machines.frbpdoctor.ui.home

import android.graphics.drawable.Drawable
import com.caverock.androidsvg.SVG
import android.graphics.Canvas
import android.graphics.drawable.PictureDrawable

class SvgDrawable(private val svg: SVG, private val width: Int, private val height: Int) :
    PictureDrawable(svg.renderToPicture()) {
    override fun draw(canvas: Canvas) {
        val aspectRatio = 1 // TODO: svg.documentWidth / svg.documentHeight
        var targetWidth = if (width > 0) width else canvas.width
        var targetHeight = (targetWidth / aspectRatio).toInt()

        if (targetHeight > canvas.height) {
            targetHeight = canvas.height
            targetWidth = (targetHeight * aspectRatio).toInt()
        }

        svg.setDocumentWidth(targetWidth.toFloat())
        svg.setDocumentHeight(targetHeight.toFloat())

        canvas.save()
        svg.renderToCanvas(canvas)
        canvas.restore()
    }
}
