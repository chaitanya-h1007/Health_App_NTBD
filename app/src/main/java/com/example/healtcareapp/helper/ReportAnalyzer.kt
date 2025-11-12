package com.example.healtcareapp.helper

import com.example.healtcareapp.model.LabReport

data class AnalysisResult(val score: Int, val status: String, val summary: String)

object ReportAnalyzer {

    fun analyze(report: LabReport): AnalysisResult {
        var score = 10
        val summary = StringBuilder()
        var testedParams = 0

        fun adjust(condition: Boolean?, message: String) {
            if (condition == null) return
            testedParams++
            if (condition) {
                score -= 2
                summary.append("⚠️ $message\n")
            }
        }

        // 🩸 Blood parameters
        adjust(report.hemoglobin?.let { it < 13.0 }, "Low Hemoglobin (Anemia risk)")
        adjust(report.hemoglobin?.let { it > 17.0 }, "High Hemoglobin")
        adjust(report.hematocrit?.let { it < 45.0 }, "Low Hematocrit (Possible low liver function)")
        adjust(report.rbc?.let { it < 4.5 }, "Low RBC count")
        adjust(report.wbc?.let { it > 11 }, "Possible infection (High WBC)")
        adjust(report.platelets?.let { it < 150 }, "Low Platelet count")

        // 🍬 Sugar parameters
        adjust(report.fastingGlucose?.let { it > 126 }, "High Fasting Glucose (Possible Diabetes)")
        adjust(report.hba1c?.let { it > 6.5 }, "Elevated HbA1c (Diabetes indicator)")

        // 🫀 Cholesterol
        adjust(report.cholesterolTotal?.let { it > 200 }, "High Total Cholesterol")
        adjust(report.triglycerides?.let { it > 150 }, "High Triglycerides")
        adjust(report.ldl?.let { it > 130 }, "High LDL Cholesterol")
        adjust(report.hdl?.let { it < 40 }, "Low HDL Cholesterol")

        // 🧠 Liver & Kidney
        adjust(report.bilirubinTotal?.let { it > 1.2 }, "Elevated Bilirubin (Liver stress)")
        adjust(report.sgpt?.let { it > 45 }, "High SGPT (Liver enzyme)")
        adjust(report.creatinine?.let { it > 1.3 }, "High Creatinine (Kidney issue)")
        adjust(report.bloodUrea?.let { it > 40 }, "High Blood Urea")

        // 🦴 Minerals
        adjust(report.calcium?.let { it < 8.5 }, "Low Calcium level")
        adjust(report.vitaminD?.let { it < 30 }, "Vitamin D Deficiency")

        // Avoid dividing by zero
        if (testedParams == 0) return AnalysisResult(10, "🟢 No data provided", "No test parameters found.")

        // Normalize score only based on tested parameters
        if (score < 0) score = 0

        // Dynamic status logic — if few parameters but severe issue → mark critical
        val healthStatus = when {
            summary.contains("Diabetes", ignoreCase = true) ||
                    summary.contains("High Fasting Glucose", ignoreCase = true) -> "🔴 Critical — High Sugar Levels Detected"
            score >= 9 -> "🟢 Healthy"
            score in 5..8 -> "🟡 Moderate — Consult doctor for mild anomalies"
            else -> "🔴 Critical — Medical consultation recommended"
        }

        return AnalysisResult(
            score = if (testedParams < 3 && summary.isNotEmpty()) 2 else score, // Adjust for single-test reports
            status = healthStatus,
            summary = summary.toString().ifEmpty { "All values are within normal range ✅" }
        )
    }
}
