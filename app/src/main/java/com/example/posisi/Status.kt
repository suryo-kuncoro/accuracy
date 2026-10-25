package com.example.posisi

object Status {
    @Volatile var text: String = "Berhenti"
    @Volatile var running: Boolean = false
    @Volatile var failed: Boolean = false
    @Volatile var startMs: Long = 0L
    @Volatile var lastFixMs: Long = 0L
}
