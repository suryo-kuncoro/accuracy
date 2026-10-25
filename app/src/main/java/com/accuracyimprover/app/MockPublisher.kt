package com.accuracyimprover.app

import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.SystemClock

class MockPublisher(ctx: Context, private val name: String = LocationManager.GPS_PROVIDER) {
    private val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    /** SecurityException jika aplikasi belum dipilih sebagai mock location app. */
    fun start() {
        try { lm.removeTestProvider(name) } catch (_: Exception) {}
        lm.addTestProvider(name, false, false, false, false, true, true, true,
            Criteria.POWER_LOW, Criteria.ACCURACY_FINE)
        lm.setTestProviderEnabled(name, true)
    }

    fun publish(lat: Double, lon: Double, accuracyM: Float) {
        val l = Location(name).apply {
            latitude = lat; longitude = lon; accuracy = accuracyM
            time = System.currentTimeMillis()
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
        }
        lm.setTestProviderLocation(name, l)
    }

    fun stop() {
        try { lm.setTestProviderEnabled(name, false); lm.removeTestProvider(name) } catch (_: Exception) {}
    }
}
