package com.friendly_machines.frbpdoctor.ui.home

import android.graphics.Canvas
import android.graphics.drawable.PictureDrawable
import com.caverock.androidsvg.SVG

class SvgDrawable(private val svg: SVG, private val width: Int, private val height: Int) :
    PictureDrawable(svg.renderToPicture()) {
    override fun draw(canvas: Canvas) {
        val aspectRatio = 1 // TODO: svg.documentWidth / svg.documentHeight
        var targetWidth = if (width > 0) width else bounds.width()
        var targetHeight = (targetWidth / aspectRatio)

        if (targetHeight > bounds.height()) {
            targetHeight = bounds.height()
            targetWidth = (targetHeight * aspectRatio)
        }

        svg.documentWidth = targetWidth.toFloat()
        svg.documentHeight = targetHeight.toFloat()

        canvas.save()
        svg.renderToCanvas(canvas)
        canvas.restore()
    }
}
