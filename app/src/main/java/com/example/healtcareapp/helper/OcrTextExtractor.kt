package com.example.healtcareapp.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

object OcrTextExtractor {

    suspend fun extractTextFromPdf(context: Context, pdfUri: Uri): String {
        val fileDescriptor: ParcelFileDescriptor? =
            context.contentResolver.openFileDescriptor(pdfUri, "r")

        if (fileDescriptor == null) {
            Log.e("OcrTextExtractor", "File descriptor is null for $pdfUri")
            return ""
        }

        val renderer = PdfRenderer(fileDescriptor)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val extractedText = StringBuilder()

        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            val image = InputImage.fromBitmap(bitmap, 0)
            try {
                val result = recognizer.process(image).await()
                extractedText.append(result.text).append("\n")
            } catch (e: Exception) {
                Log.e("OcrTextExtractor", "Error reading page $i: ${e.message}")
            }
        }

        renderer.close()
        fileDescriptor.close()

        return extractedText.toString()
    }
}
