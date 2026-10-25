package com.example.posisi

object Status {
    @Volatile var text: String = "Berhenti"
    @Volatile var lastFixMs: Long = 0L
}
