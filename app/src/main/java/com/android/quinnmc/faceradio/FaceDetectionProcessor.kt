package com.android.quinnmc.faceradio

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import java.io.IOException

class FaceDetectionProcessor : VisionProcessorBase<List<FirebaseVisionFace>>() {

    private val detector: FirebaseVisionFaceDetector

    // Step 1) Configure the face detector
    init {
        Log.d(TAG, "In the face detection processor")

        val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .build()

        // Step 2) run facedetector - get instance of FirebaseVisionFaceDetector
        detector = FirebaseVision.getInstance().getVisionFaceDetector(options)

    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: $e")
        }
    }

    // Step 2) run face detector - pass image to detectInImage()
    override fun detectInImage(image: FirebaseVisionImage): Task<List<FirebaseVisionFace>> {
        Log.d(TAG, "detecting face in image")
        return detector.detectInImage(image)
    }

    override fun onSuccess(
        originalCameraImage: Bitmap,
        results: List<FirebaseVisionFace>,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {
        graphicOverlay.clear()
        originalCameraImage.let { image ->
            val imageGraphic = CameraImageGraphic(graphicOverlay, image)
            graphicOverlay.add(imageGraphic)
        }
        for (i in results.indices) {
            val face = results[i]
            val cameraFacing = frameMetadata.cameraFacing
            val faceGraphic = FaceGraphic(graphicOverlay, face, cameraFacing)
            graphicOverlay.add(faceGraphic)
        }
        graphicOverlay.postInvalidate()
        Log.d(TAG, "successful face detection")
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Face detection failed $e")
    }

    companion object {
        private const val TAG = "MLKit - Face Det Proc"
    }
}