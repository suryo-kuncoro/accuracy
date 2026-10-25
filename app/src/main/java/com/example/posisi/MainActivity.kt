package com.example.posisi

import android.Manifest
import android.content.Intent
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
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val tv = TextView(this).apply { textSize = 16f; setPadding(32, 32, 32, 32) }
        val start = Button(this).apply {
            text = "MULAI"
            setOnClickListener {
                ContextCompat.startForegroundService(
                    this@MainActivity, Intent(this@MainActivity, PositionService::class.java))
            }
        }
        val stop = Button(this).apply {
            text = "BERHENTI"
            setOnClickListener {
                startService(Intent(this@MainActivity, PositionService::class.java)
                    .setAction(PositionService.ACTION_STOP))
            }
        }
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(start); addView(stop); addView(tv)
        })
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS), 1)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    val t = Status.lastFixMs
                    val age = if (t == 0L) "-" else "${(System.currentTimeMillis() - t) / 1000} dtk"
                    tv.text = Status.text + "\numur NMEA terakhir: $age"
                    delay(1000)
                }
            }
        }
    }
}
