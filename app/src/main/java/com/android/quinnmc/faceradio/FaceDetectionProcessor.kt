package com.android.quinnmc.faceradio

//import android.util.Log
//import com.google.android.gms.tasks.Task
//import com.google.firebase.ml.vision.FirebaseVision
//import com.google.firebase.ml.vision.common.FirebaseVisionImage
//import com.google.firebase.ml.vision.face.FirebaseVisionFace
//import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
//import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
//import java.io.IOException

//package com.google.firebase.samples.apps.mlkit.kotlin.facedetection

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
//import com.google.firebase.samples.apps.mlkit.common.FrameMetadata
//import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay
//import com.google.firebase.samples.apps.mlkit.kotlin.VisionProcessorBase
import java.io.IOException

// Used by CameraSource

/** Face Detector Demo.  */
class FaceDetectionProcessor : VisionProcessorBase<List<FirebaseVisionFace>>() {

    interface FaceDetectionListener {
        fun onNewFace(face: FirebaseVisionFace)
    }

    private val detector: FirebaseVisionFaceDetector

    init {
        val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
//            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
//            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)

            .setTrackingEnabled(true)
            .build()

        detector = FirebaseVision.getInstance().getVisionFaceDetector(options)
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: $e")
        }
    }

    override fun detectInImage(image: FirebaseVisionImage): Task<List<FirebaseVisionFace>> {
        return detector.detectInImage(image)
    }

    override fun onSuccess(
        faces: List<FirebaseVisionFace>,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {
        graphicOverlay.clear()
        for (i in faces.indices) {
            val face = faces[i]
            //RadioActivity.deejay.updateMusic(latestFace!!)
            //Deejay.updateMusic(face)
            //println("NEW FACE, NEW CALL")
            (Deejay as FaceDetectionListener).onNewFace(face)
            // test
//            val faceGraphic = FaceGraphic(graphicOverlay)
//            graphicOverlay.add(faceGraphic)
//            faceGraphic.updateFace(face, frameMetadata.cameraFacing)
        }
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Face detection failed $e")
    }

    companion object {

        private const val TAG = "FaceDetectionProcessor"
    }
}



// OLD ^ 699

//import android.graphics.Bitmap
//import android.util.Log
//import com.google.android.gms.tasks.Task
//import com.google.firebase.ml.vision.FirebaseVision
//import com.google.firebase.ml.vision.common.FirebaseVisionImage
//import com.google.firebase.ml.vision.face.FirebaseVisionFace
//import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
//import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
//import java.io.IOException
//
//class FaceDetectionProcessor : VisionProcessorBase<List<FirebaseVisionFace>>() {
//
//    private val detector: FirebaseVisionFaceDetector
//
//    // Step 1) Configure the face detector
//    init {
//        Log.d(TAG, "In the face detection processor")
//
//        val options = FirebaseVisionFaceDetectorOptions.Builder()
//            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
//            .build()
//
//        // Step 2) run facedetector - get instance of FirebaseVisionFaceDetector
//        detector = FirebaseVision.getInstance().getVisionFaceDetector(options)
//
//    }
//
//    override fun stop() {
//        try {
//            detector.close()
//        } catch (e: IOException) {
//            Log.e(TAG, "Exception thrown while trying to close Face Detector: $e")
//        }
//    }
//
//    // Step 2) run face detector - pass image to detectInImage()
//    override fun detectInImage(image: FirebaseVisionImage): Task<List<FirebaseVisionFace>> {
//        Log.d(TAG, "detecting face in image")
//        return detector.detectInImage(image)
//    }
//
//    override fun onSuccess(
//        originalCameraImage: Bitmap,
//        results: List<FirebaseVisionFace>,
//        frameMetadata: FrameMetadata,
//        graphicOverlay: GraphicOverlay
//    ) {
//        graphicOverlay.clear()
//        originalCameraImage.let { image ->
//            val imageGraphic = CameraImageGraphic(graphicOverlay, image)
//            graphicOverlay.add(imageGraphic)
//        }
//        for (i in results.indices) {
//            val face = results[i]
//            val cameraFacing = frameMetadata.cameraFacing
//            val faceGraphic = FaceGraphic(graphicOverlay, face, cameraFacing)
//            graphicOverlay.add(faceGraphic)
//            Log.d(TAG, "HAPPINESS PROBABILITY = " + face.smilingProbability.toString())
//            Log.d(TAG, "LEFT EYE OPEN PROBABILITY = " + face.leftEyeOpenProbability.toString())
//            Log.d(TAG, "RIGHT EYE OPEN PROBABILITY = " + face.rightEyeOpenProbability.toString())
//        }
//        graphicOverlay.postInvalidate()
//        Log.d(TAG, "successful face detection")
//    }
//
//    override fun onFailure(e: Exception) {
//        Log.e(TAG, "Face detection failed $e")
//    }
//
//    companion object {
//        private const val TAG = "MLKit - Face Det Proc"
//    }
//}