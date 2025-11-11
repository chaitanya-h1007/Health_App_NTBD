package com.example.healtcareapp.helper

import android.util.Log
import com.example.healtcareapp.model.LabReport

object LabReportParser {

    fun parse(rawText: String): LabReport {
        var text = rawText.replace("\n", " ")
            .replace(Regex("\\s+"), " ")
            .replace(Regex("(?i)method.*?(?=\\b[A-Z])"), "")
            .replace(Regex("(?i)unit.*?(?=\\b[A-Z])"), "")
            .replace(Regex("(?i)bio ref.*?(?=\\b[A-Z])"), "")
            .replace(Regex("(?i)ref interval.*?(?=\\b[A-Z])"), "")
            .replace(Regex("(?i)report.*?(?=\\b[A-Z])"), "")
            .trim()

        Log.d("CleanedText", text.take(500)) // Log first 500 chars

        // Step 2️⃣: Extract values with more flexible matching
        fun findValue(vararg keywords: String): Double? {
            for (keyword in keywords) {
                // Match pattern like "HbA1c 7.4" or "HbA1c: 7.4" or "HbA1c ... 7.4"
                val regex = Regex("$keyword[^0-9]{0,10}([0-9]+\\.?[0-9]*)", RegexOption.IGNORE_CASE)
                val match = regex.find(text)
                if (match != null) {
                    val value = match.groupValues[1].toDoubleOrNull()
                    if (value != null) return value
                }
            }
            return null
        }

        return LabReport(
            fastingGlucose = findValue("Fasting Glucose", "Blood Glucose", "Plasma Glucose"),
            hba1c = findValue("HbA1c", "HBAIC", "Hb Alc"),
            randomGlucose = findValue("Random Glucose", "2-hour Plasma Glucose", "OGTT"),
            cholesterolTotal = findValue("Cholesterol Total", "Total Cholesterol"),
            triglycerides = findValue("Triglycerides", "Triglyceride"),
            hdl = findValue("HDL", "HDL Cholesterol"),
            ldl = findValue("LDL", "LDL Cholesterol"),
            vldl = findValue("VLDL", "VLDL Cholesterol"),
            nonHdl = findValue("Non HDL", "Non-HDL"),
            bilirubinTotal = findValue("Bilirubin Total", "Bilirubin, Total"),
            bilirubinDirect = findValue("Bilirubin Direct", "Direct Bilirubin"),
            bilirubinIndirect = findValue("Bilirubin Indirect", "Indirect Bilirubin"),
            sgpt = findValue("SGPT", "ALT", "Alanine Transaminase"),
            sgot = findValue("SGOT", "AST", "Aspartate Transaminase"),
            ggt = findValue("GGT", "Gamma Glutamyl Transferase"),
            alkalinePhosphatase = findValue("Alkaline Phosphatase", "ALP"),
            bloodUrea = findValue("Blood Urea", "Urea"),
            bun = findValue("Blood Urea Nitrogen", "BUN"),
            creatinine = findValue("Creatinine", "Serum Creatinine"),
            uricAcid = findValue("Uric Acid", "Serum Uric Acid"),
            sodium = findValue("Sodium", "Na"),
            potassium = findValue("Potassium", "K"),
            chloride = findValue("Chloride", "Cl"),
            hemoglobin = findValue("Hemoglobin", "Hacmoglobin", "HB"),
            rbc = findValue("RBC", "Red Blood Cell"),
            wbc = findValue("WBC", "Leucocyte Count", "TLC"),
            platelets = findValue("Platelet Count", "Platelets"),
            hematocrit = findValue("Hematocrit", "PCV"),
            mcv = findValue("MCV"),
            mch = findValue("MCH"),
            mchc = findValue("MCHC"),
            neutrophils = findValue("Neutrophils"),
            lymphocytes = findValue("Lymphocytes"),
            monocytes = findValue("Monocytes"),
            eosinophils = findValue("Eosinophils"),
            basophils = findValue("Basophils"),
            calcium = findValue("Calcium"),
            phosphorus = findValue("Phosphorus"),
            vitaminD = findValue("Vitamin D"),
            vitaminB12 = findValue("Vitamin B12"),
            totalProtein = findValue("Total Protein"),
            albumin = findValue("Albumin"),
            globulin = findValue("Globulin"),
            agRatio = findValue("Albumin/Globulin", "A/G Ratio")
        )
    }
}
