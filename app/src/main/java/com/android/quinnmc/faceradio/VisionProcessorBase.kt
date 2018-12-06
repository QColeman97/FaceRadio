package com.android.quinnmc.faceradio

//import android.graphics.Bitmap
//import android.media.Image
//import com.google.android.gms.tasks.Task
//import com.google.firebase.ml.vision.common.FirebaseVisionImage
//import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
//import java.nio.ByteBuffer
//import java.util.concurrent.atomic.AtomicBoolean

//package com.google.firebase.samples.apps.mlkit.kotlin

import android.graphics.Bitmap
import android.media.Image
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
//import com.google.firebase.samples.apps.mlkit.common.FrameMetadata
//import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay
//import com.google.firebase.samples.apps.mlkit.common.VisionImageProcessor
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Abstract base class for ML Kit frame processors. Subclasses need to implement {@link
 * #onSuccess(T, FrameMetadata, GraphicOverlay)} to define what they want to with the detection
 * results and {@link #detectInImage(FirebaseVisionImage)} to specify the detector object.
 *
 * @param <T> The type of the detected feature.
 */


// Used by FaceDetectionProcessor


abstract class VisionProcessorBase<T> : VisionImageProcessor {

    // Whether we should ignore process(). This is usually caused by feeding input data faster than
    // the model can handle.
    private val shouldThrottle = AtomicBoolean(false)

    override fun process(
        data: ByteBuffer,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {
        if (shouldThrottle.get()) {
            return
        }
        val metadata = FirebaseVisionImageMetadata.Builder()
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
            .setWidth(frameMetadata.width)
            .setHeight(frameMetadata.height)
            .setRotation(frameMetadata.rotation)
            .build()

        detectInVisionImage(
            FirebaseVisionImage.fromByteBuffer(data, metadata), frameMetadata, graphicOverlay)
    }

    // Bitmap version
    override fun process(bitmap: Bitmap, graphicOverlay: GraphicOverlay) {
        if (shouldThrottle.get()) {
            return
        }
        detectInVisionImage(FirebaseVisionImage.fromBitmap(bitmap), null, graphicOverlay)
    }

    /**
     * Detects feature from given media.Image
     *
     * @return created FirebaseVisionImage
     */
    override fun process(image: Image, rotation: Int, graphicOverlay: GraphicOverlay) {
        if (shouldThrottle.get()) {
            return
        }
        // This is for overlay display's usage
        val frameMetadata = FrameMetadata.Builder()
            .setWidth(image.width)
            .setHeight(image.height)
            .build()
        val fbVisionImage = FirebaseVisionImage.fromMediaImage(image, rotation)
        detectInVisionImage(fbVisionImage, frameMetadata, graphicOverlay)
    }

    private fun detectInVisionImage(
        image: FirebaseVisionImage,
        metadata: FrameMetadata?,
        graphicOverlay: GraphicOverlay
    ) {
        detectInImage(image)
            .addOnSuccessListener { results ->
                shouldThrottle.set(false)
                metadata?.let {
                    onSuccess(results, it, graphicOverlay)
                }
            }
            .addOnFailureListener { e ->
                shouldThrottle.set(false)
                this@VisionProcessorBase.onFailure(e)
            }
        // Begin throttling until this frame of input has been processed, either in onSuccess or
        // onFailure.
        shouldThrottle.set(true)
    }

    override fun stop() {}

    protected abstract fun detectInImage(image: FirebaseVisionImage): Task<T>

    protected abstract fun onSuccess(
        results: T,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    )

    protected abstract fun onFailure(e: Exception)
}



// OLD ^ 699

//import android.graphics.Bitmap
//import android.support.annotation.GuardedBy
//import android.util.Log
//import com.google.android.gms.tasks.Task
//import com.google.firebase.ml.vision.common.FirebaseVisionImage
//import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
//import java.nio.ByteBuffer
//
///**
// * Abstract base class for ML Kit frame processors. Subclasses need to implement {@link
// * #onSuccess(T, FrameMetadata, GraphicOverlay)} to define what they want to with the detection
// * results and {@link #detectInImage(FirebaseVisionImage)} to specify the detector object.
// *
// * @param <T> The type of the detected feature.
// */
//abstract class VisionProcessorBase<T> : VisionImageProcessor {
//
//    // To keep the latest images and its metadata.
//    @GuardedBy("this")
//    private var latestImage: ByteBuffer? = null
//
//    @GuardedBy("this")
//    private var latestImageMetaData: FrameMetadata? = null
//
//    // To keep the images and metadata in process.
//    @GuardedBy("this")
//    private var processingImage: ByteBuffer? = null
//
//    @GuardedBy("this")
//    private var processingMetaData: FrameMetadata? = null
//
//    // Step 2) Run the face detector fromBitmap/ByteBuffer
//
//    @Synchronized
//    override fun process(
//        data: ByteBuffer,
//        frameMetadata: FrameMetadata,
//        graphicOverlay: GraphicOverlay
//    ) {
//        latestImage = data
//        latestImageMetaData = frameMetadata
//        if (processingImage == null && processingMetaData == null) {
//            processLatestImage(graphicOverlay)
//        }
//    }
//
//    // Step 2) Run the face detector
//
//    // Bitmap version
//    override fun process(bitmap: Bitmap, graphicOverlay: GraphicOverlay) {
//        Log.d(TAG, "Processing image")
//        detectInVisionImage(
//            null /* bitmap */,
//            FirebaseVisionImage.fromBitmap(bitmap),
//            null,
//            graphicOverlay
//        )
//    }
//
//
//    @Synchronized
//    private fun processLatestImage(graphicOverlay: GraphicOverlay) {
//        processingImage = latestImage
//        processingMetaData = latestImageMetaData
//        latestImage = null
//        latestImageMetaData = null
//        if (processingImage != null && processingMetaData != null) {
//            processImage(
//                processingImage as ByteBuffer,
//                processingMetaData as FrameMetadata,
//                graphicOverlay
//            )
//        }
//    }
//
//    private fun processImage(
//        data: ByteBuffer,
//        frameMetadata: FrameMetadata,
//        graphicOverlay: GraphicOverlay
//    ) {
//        val metadata = FirebaseVisionImageMetadata.Builder()
//            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
//// Went for direct access
////            .setWidth(frameMetadata.getWidth())
//            .setWidth(frameMetadata.width)
//            .setHeight(frameMetadata.height)
//            .setRotation(frameMetadata.rotation)
//            .build()
//
//        val bitmap = BitmapUtils.getBitmap(data, frameMetadata)
//        detectInVisionImage(
//            bitmap,
//            FirebaseVisionImage.fromByteBuffer(data, metadata),
//            frameMetadata,
//            graphicOverlay
//        )
//    }
//
//    private fun detectInVisionImage(
//        originalCameraImage: Bitmap?,
//        image: FirebaseVisionImage,
//        metadata: FrameMetadata?,
//        graphicOverlay: GraphicOverlay
//    ) {
//        detectInImage(image)
//            .addOnSuccessListener { results ->
//                metadata?.let {
//                    onSuccess(originalCameraImage!!, results, it, graphicOverlay)
//                }
//            }
//            .addOnFailureListener { e ->
//                this@VisionProcessorBase.onFailure(e)
//            }
//    }
//
//    override fun stop() {}
//
//    protected abstract fun detectInImage(image: FirebaseVisionImage): Task<T>
//
//    protected abstract fun onSuccess(
//        originalCameraImage: Bitmap,
//        results: T,
//        frameMetadata: FrameMetadata,
//        graphicOverlay: GraphicOverlay
//    )
//
//    protected abstract fun onFailure(e: Exception)
//
//    companion object {
//        private const val TAG = "MLKit - VisionProcBase"
//    }
//}