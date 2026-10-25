package com.example.posisi

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.*

class PositionService : Service() {
    companion object { const val ACTION_STOP = "stop" }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var job: Job? = null
    private var mock: MockPublisher? = null

    override fun onBind(i: Intent?): IBinder? = null

    override fun onStartCommand(i: Intent?, flags: Int, id: Int): Int {
        if (i?.action == ACTION_STOP) { stopSelf(); return START_NOT_STICKY }
        startAsForeground()
        if (job != null) return START_STICKY

        val m = MockPublisher(this)
        try { m.start() } catch (e: SecurityException) {
            Status.text = "Pilih aplikasi ini di Developer Options > Select mock location app"
            stopSelf(); return START_NOT_STICKY
        }
        mock = m

        job = scope.launch {
            var n = 0
            PhoneGnssSource(this@PositionService).fixes().collect { fx ->
                m.publish(fx.lat, fx.lon, fx.sigmaH.toFloat())   // Tahap 1: diteruskan apa adanya
                n++
                Status.lastFixMs = System.currentTimeMillis()
                Status.text = "epoch=$n  fix=${fx.fix}  nsat=${fx.nsat}\n" +
                    "hdop=%.1f  sigma=%.1f m  cn0=%.0f\n".format(fx.hdop, fx.sigmaH, fx.cn0) +
                    "raw: %.6f, %.6f".format(fx.lat, fx.lon)
            }
        }
        return START_STICKY
    }

    private fun startAsForeground() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("pos", "Posisi", NotificationManager.IMPORTANCE_LOW))
        val n = Notification.Builder(this, "pos")
            .setContentTitle("Posisi pendamping aktif")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation).build()
        if (Build.VERSION.SDK_INT >= 29)
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        else startForeground(1, n)
    }

    override fun onDestroy() {
        job?.cancel(); mock?.stop(); scope.cancel()
        Status.text = "Berhenti"
    }
}
