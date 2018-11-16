package com.android.quinnmc.faceradio

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect

// FIXED POSSIBLY
// GraphicOverlay.Graphic instead of Graphic, and put override in front of fun draw

/** Draw camera image to background.  */
class CameraImageGraphic(overlay: GraphicOverlay, private val bitmap: Bitmap) : GraphicOverlay.Graphic(overlay) {

    override fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, null, Rect(0, 0, canvas.width, canvas.height), null)
    }
}