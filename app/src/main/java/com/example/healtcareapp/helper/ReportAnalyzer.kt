package com.example.healtcareapp.helper

import com.example.healtcareapp.model.LabReport

data class AnalysisResult(val score: Int, val status: String, val summary: String)

object ReportAnalyzer {

    fun analyze(report: LabReport): AnalysisResult {
        var score = 10
        val summary = StringBuilder()

        fun adjust(condition: Boolean, message: String) {
            if (condition) {
                score -= 1
                summary.append("⚠️ $message\n")
            }
        }

        // 🩸 Blood parameters
        adjust(report.hemoglobin != null && report.hemoglobin!! < 13.0, "Low Hemoglobin (Anemia risk)")
        adjust(report.hemoglobin != null && report.hemoglobin!! > 17.0, "High Hemoglobin")
        adjust(report.hematocrit != null && report.hematocrit!! < 45.0, "Low Hematocrit (Low liver function")
        adjust(report.rbc != null && report.rbc!! < 4.5, "Low RBC count")
        adjust(report.wbc != null && report.wbc!! > 11, "Possible infection (High WBC)")
        adjust(report.platelets != null && report.platelets!! < 150, "Low Platelet count")


        adjust(report.fastingGlucose != null && report.fastingGlucose!! > 126, "High Fasting Glucose (Possible Diabetes)")
        adjust(report.hba1c != null && report.hba1c!! > 6.5, "Elevated HbA1c (Diabetes indicator)")


        adjust(report.cholesterolTotal != null && report.cholesterolTotal!! > 200, "High Cholesterol")
        adjust(report.triglycerides != null && report.triglycerides!! > 150, "High Triglycerides")
        adjust(report.ldl != null && report.ldl!! > 130, "High LDL Cholesterol")
        adjust(report.hdl != null && report.hdl!! < 40, "Low HDL Cholesterol")

        // 🧠 Liver & Kidney
        adjust(report.bilirubinTotal != null && report.bilirubinTotal!! > 1.2, "Elevated Bilirubin (Liver stress)")
        adjust(report.sgpt != null && report.sgpt!! > 45, "High SGPT (Liver enzyme)")
        adjust(report.creatinine != null && report.creatinine!! > 1.3, "High Creatinine (Kidney issue)")
        adjust(report.bloodUrea != null && report.bloodUrea!! > 40, "High Blood Urea")

        // 🦴 Minerals
        adjust(report.calcium != null && report.calcium!! < 8.5, "Low Calcium level")
        adjust(report.vitaminD != null && report.vitaminD!! < 30, "Vitamin D Deficiency")

        if (score < 0) score = 0

        val healthStatus = when {
            score >= 8 -> "🟢 Healthy"
            score in 5..7 -> "🟡 Moderate — consult doctor for mild anomalies"
            else -> "🔴 Critical — medical consultation recommended"
        }

        return AnalysisResult(score, healthStatus, summary.toString().ifEmpty { "All values are within normal range ✅" })
    }
}
