package com.android.quinnmc.faceradio

import android.graphics.*
import android.hardware.Camera
import android.util.Log
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

/** Utils functions for bitmap conversions. */
class BitmapUtils {

    // Put in companion object to make static methods
    companion object {
        // Convert NV21 format byte buffer to bitmap.
        fun getBitmap(data: ByteBuffer, metadata: FrameMetadata): Bitmap? {
            data.rewind()
            val imageInBuffer = ByteArray(data.limit())
            data.get(imageInBuffer, 0, imageInBuffer.size)
            try {
                val image = YuvImage(
                    imageInBuffer, ImageFormat.NV21, metadata.width, metadata.height, null
                )
                if (image != null) {
                    val stream = ByteArrayOutputStream()
                    image.compressToJpeg(Rect(0, 0, metadata.width, metadata.height), 80, stream)

                    val bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())

                    stream.close()
                    return rotateBitmap(bmp, metadata.rotation, metadata.cameraFacing)
                }
            } catch (e: Exception) {
                Log.e("VisionProcessorBase", "Error: " + e.message)
            }

            return null
        }

        // Rotates a bitmap if it is converted from a bytebuffer.
        private fun rotateBitmap(bitmap: Bitmap, rotation: Int, facing: Int): Bitmap {
            val matrix = Matrix()
            var rotationDegree = 0
            when (rotation) {
                FirebaseVisionImageMetadata.ROTATION_90 -> rotationDegree = 90
                FirebaseVisionImageMetadata.ROTATION_180 -> rotationDegree = 180
                FirebaseVisionImageMetadata.ROTATION_270 -> rotationDegree = 270
                else -> {
                }
            }

            // Rotate the image back to straight.}
            matrix.postRotate(rotationDegree.toFloat())
            if (facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                // Mirror the image along X axis for front-facing camera image.
                matrix.postScale(-1.0f, 1.0f)
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
        }
    }
}