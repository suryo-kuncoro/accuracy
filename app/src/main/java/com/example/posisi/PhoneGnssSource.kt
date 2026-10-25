package com.example.posisi

import android.annotation.SuppressLint
import android.content.Context
import android.location.GnssStatus
import android.location.LocationListener
import android.location.LocationManager
import android.location.OnNmeaMessageListener
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Posisi asli dari NMEA chip (bukan dari Location, karena provider "gps"
 * digantikan oleh mock kita sendiri).
 */
class PhoneGnssSource(private val ctx: Context) : GnssSource {
    @Volatile private var cn0 = 25.0

    @SuppressLint("MissingPermission")
    override fun fixes(): Flow<RawFix> = callbackFlow {
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val h = Handler(Looper.getMainLooper())
        val parser = NmeaParser { cn0 }

        val nmea = OnNmeaMessageListener { msg, _ ->
            parser.feed(msg, System.currentTimeMillis())?.let { trySend(it) }
        }
        val status = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(s: GnssStatus) {
                var sum = 0.0; var n = 0
                for (i in 0 until s.satelliteCount)
                    if (s.usedInFix(i)) { sum += s.getCn0DbHz(i); n++ }
                if (n > 0) cn0 = sum / n
            }
        }
        // Permintaan lokasi kosong: hanya agar mesin GNSS tetap menyala.
        val keepAlive = LocationListener { }

        lm.addNmeaListener(nmea, h)
        lm.registerGnssStatusCallback(status, h)
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, keepAlive, Looper.getMainLooper())

        awaitClose {
            lm.removeNmeaListener(nmea)
            lm.unregisterGnssStatusCallback(status)
            lm.removeUpdates(keepAlive)
        }
    }
}
