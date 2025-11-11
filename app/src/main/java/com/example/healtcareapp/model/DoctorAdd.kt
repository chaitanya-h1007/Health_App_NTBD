package com.example.healtcareapp.model

data class DoctorAdd(
    val name : String,
    val address : String,
    val rating : Double,
    val specialization : String,
    val distance : String,
    val mapsUrl : String
)