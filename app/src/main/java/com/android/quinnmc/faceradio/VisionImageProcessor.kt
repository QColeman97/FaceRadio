package com.android.quinnmc.faceradio

//import android.graphics.Bitmap
//import java.nio.ByteBuffer

import android.graphics.Bitmap
import android.media.Image

import com.google.firebase.ml.common.FirebaseMLException

import java.nio.ByteBuffer

/** An inferface to process the images with different ML Kit detectors and custom image models.  */
interface VisionImageProcessor {

    /** Processes the images with the underlying machine learning models.  */
    @Throws(FirebaseMLException::class)
    fun process(data: ByteBuffer, frameMetadata: FrameMetadata, graphicOverlay: GraphicOverlay)

    /** Processes the bitmap images.  */
    fun process(bitmap: Bitmap, graphicOverlay: GraphicOverlay)

    /** Processes the images.  */
    fun process(bitmap: Image, rotation: Int, graphicOverlay: GraphicOverlay)

    /** Stops the underlying machine learning model and release resources.  */
    fun stop()
}


///** An inferface to process the images with different ML Kit detectors and custom image models. */
//interface VisionImageProcessor {
//
//    /** Processes the images with the underlying machine learning models.  */
//    //@Throws(FirebaseMLException::class)
//    abstract fun process(data: ByteBuffer, frameMetadata: FrameMetadata, graphicOverlay: GraphicOverlay)
//
//    /** Processes the bitmap images.  */
//    abstract fun process(bitmap: Bitmap, graphicOverlay: GraphicOverlay)
//
//    /** Stops the underlying machine learning model and release resources.  */
//    abstract fun stop()
//}