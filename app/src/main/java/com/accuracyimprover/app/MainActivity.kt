package com.accuracyimprover.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private fun hasLocation() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun askPermissions() = ActivityCompat.requestPermissions(this,
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS), 1)

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val tv = TextView(this).apply { textSize = 16f; setPadding(32, 32, 32, 32) }

        val start = Button(this).apply {
            text = "MULAI"
            setOnClickListener {
                if (!hasLocation()) {
                    Toast.makeText(this@MainActivity,
                        "Izinkan Lokasi dengan opsi PRESISI (bukan Perkiraan), lalu tekan MULAI lagi", Toast.LENGTH_LONG).show()
                    askPermissions(); return@setOnClickListener
                }
                Toast.makeText(this@MainActivity, "Memulai layanan...", Toast.LENGTH_SHORT).show()
                Status.text = "Memulai layanan..."
                ContextCompat.startForegroundService(
                    this@MainActivity, Intent(this@MainActivity, PositionService::class.java))
            }
        }
        val stop = Button(this).apply {
            text = "BERHENTI"
            setOnClickListener {
                Toast.makeText(this@MainActivity, "Menghentikan layanan...", Toast.LENGTH_SHORT).show()
                startService(Intent(this@MainActivity, PositionService::class.java)
                    .setAction(PositionService.ACTION_STOP))
            }
        }
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(start); addView(stop); addView(tv)
        })
        if (!hasLocation()) askPermissions()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    val now = System.currentTimeMillis()
                    val t = Status.lastFixMs
                    val age = if (t == 0L) "-" else "${(now - t) / 1000} dtk"
                    var msg = "Izin lokasi: " + (if (hasLocation()) "OK" else "BELUM DIBERIKAN") +
                        "\nLayanan: " + (if (Status.running) "BERJALAN" else "berhenti") +
                        "\n\n" + Status.text + "\numur NMEA terakhir: $age"
                    if (Status.running && t == 0L && now - Status.startMs > 10_000)
                        msg += "\n\nPERINGATAN: belum ada NMEA setelah 10 dtk. Cek: langit terbuka, " +
                            "Lokasi HP aktif, izin Presisi. Jika semua benar, mock mungkin " +
                            "mematikan mesin GNSS (hasil Uji 3: gagal)."
                    tv.text = msg
                    delay(1000)
                }
            }
        }
    }
}
