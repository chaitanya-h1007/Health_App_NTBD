package com.example.healtcareapp.helper

import android.content.Context
import android.net.Uri
import android.util.Log
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper

object PdfTextExtractor {

    fun extractText(context: Context, pdfUri: Uri): String {
        return try {
            // ✅ Initialize PDFBox resources before using
            PDFBoxResourceLoader.init(context)

            // ✅ Open the PDF document from the URI
            context.contentResolver.openInputStream(pdfUri)?.use { input ->
                PDDocument.load(input).use { document ->
                    val stripper = PDFTextStripper()
                    val text = stripper.getText(document)

                    Log.d("PdfTextExtractor", "Extracted text length: ${text.length}")
                    text
                }
            } ?: ""
        } catch (e: Exception) {
            Log.e("PdfTextExtractor", "Error extracting text: ${e.message}", e)
            ""
        }
    }
}
