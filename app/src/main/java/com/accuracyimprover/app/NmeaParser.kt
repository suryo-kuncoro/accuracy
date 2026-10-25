package com.accuracyimprover.app

import kotlin.math.hypot

/** Dipakai bersama oleh sumber HP dan (nanti) receiver Bluetooth. */
class NmeaParser(private val cn0: () -> Double = { 25.0 }) {
    private var gstSigma = Double.NaN
    private var gstT = 0L

    fun feed(sentence: String, tMs: Long): RawFix? {
        val s = sentence.trim()
        if (!s.startsWith("$") || !checksumOk(s)) return null
        val star = s.indexOf('*')
        val f = s.substring(1, if (star > 0) star else s.length).split(",")
        when (f[0].takeLast(3)) {
            "GST" -> {
                val a = f.getOrNull(6)?.toDoubleOrNull()
                val b = f.getOrNull(7)?.toDoubleOrNull()
                if (a != null && b != null) { gstSigma = hypot(a, b); gstT = tMs }
            }
            "GGA" -> {
                val q = f.getOrNull(6)?.toIntOrNull() ?: return null
                if (q == 0) return null
                val lat = dm(f.getOrNull(2), f.getOrNull(3)) ?: return null
                val lon = dm(f.getOrNull(4), f.getOrNull(5)) ?: return null
                val nsat = f.getOrNull(7)?.toIntOrNull() ?: 0
                val hdop = f.getOrNull(8)?.toDoubleOrNull() ?: 99.0
                // Tanpa GST, sigma diperkirakan dari HDOP (asumsi UERE ~4 m, perlu dikalibrasi)
                val sigma = if (!gstSigma.isNaN() && tMs - gstT < 2000) gstSigma else hdop * 4.0
                return RawFix(tMs, lat, lon, sigma, q, nsat, hdop, cn0())
            }
        }
        return null
    }

    private fun dm(v: String?, h: String?): Double? {
        val x = v?.toDoubleOrNull() ?: return null
        val d = (x / 100).toInt()
        val r = d + (x - d * 100) / 60.0
        return if (h == "S" || h == "W") -r else r
    }

    private fun checksumOk(s: String): Boolean {
        val star = s.indexOf('*')
        if (star < 0) return true
        var x = 0
        for (i in 1 until star) x = x xor s[i].code
        return s.substring(star + 1).trim().take(2).equals("%02X".format(x), ignoreCase = true)
    }
}
