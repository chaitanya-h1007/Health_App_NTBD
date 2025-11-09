package com.example.healtcareapp.model

data class LabReport(

    // 🔹 Glucose & Diabetes
    val fastingGlucose: Double? = null,
    val hba1c: Double? = null,
    val randomGlucose: Double? = null,

    // 🔹 Lipid Profile
    val cholesterolTotal: Double? = null,
    val triglycerides: Double? = null,
    val hdl: Double? = null,
    val ldl: Double? = null,
    val vldl: Double? = null,
    val nonHdl: Double? = null,

    // 🔹 Liver Function (LFT)
    val bilirubinTotal: Double? = null,
    val bilirubinDirect: Double? = null,
    val bilirubinIndirect: Double? = null,
    val sgpt: Double? = null,
    val sgot: Double? = null,
    val ggt: Double? = null,
    val alkalinePhosphatase: Double? = null,

    // 🔹 Kidney Function (KFT)
    val bloodUrea: Double? = null,
    val bun: Double? = null,
    val creatinine: Double? = null,
    val uricAcid: Double? = null,
    val sodium: Double? = null,
    val potassium: Double? = null,
    val chloride: Double? = null,

    // 🔹 CBC (Complete Blood Count)
    val hemoglobin: Double? = null,
    val rbc: Double? = null,
    val wbc: Double? = null,
    val platelets: Double? = null,
    val hematocrit: Double? = null,
    val mcv: Double? = null,
    val mch: Double? = null,
    val mchc: Double? = null,
    val neutrophils: Double? = null,
    val lymphocytes: Double? = null,
    val monocytes: Double? = null,
    val eosinophils: Double? = null,
    val basophils: Double? = null,

    // 🔹 Minerals & Vitamins
    val calcium: Double? = null,
    val phosphorus: Double? = null,
    val vitaminD: Double? = null,
    val vitaminB12: Double? = null,

    // 🔹 Proteins
    val totalProtein: Double? = null,
    val albumin: Double? = null,
    val globulin: Double? = null,
    val agRatio: Double? = null
)
