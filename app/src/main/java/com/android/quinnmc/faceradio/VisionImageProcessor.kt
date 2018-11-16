package com.android.quinnmc.faceradio

import android.graphics.Bitmap
import java.nio.ByteBuffer

/** An inferface to process the images with different ML Kit detectors and custom image models. */
interface VisionImageProcessor {

    /** Processes the images with the underlying machine learning models.  */
    //@Throws(FirebaseMLException::class)
    abstract fun process(data: ByteBuffer, frameMetadata: FrameMetadata, graphicOverlay: GraphicOverlay)

    /** Processes the bitmap images.  */
    abstract fun process(bitmap: Bitmap, graphicOverlay: GraphicOverlay)

    /** Stops the underlying machine learning model and release resources.  */
    abstract fun stop()
}