package com.example.posisi

import kotlinx.coroutines.flow.Flow

data class RawFix(
    val tMs: Long, val lat: Double, val lon: Double,
    val sigmaH: Double,   // meter, 1-sigma horizontal
    val fix: Int,         // kualitas GGA: 1=single, 2=DGPS, 4=RTK fix, 5=RTK float
    val nsat: Int, val hdop: Double, val cn0: Double
)

interface GnssSource { fun fixes(): Flow<RawFix> }
