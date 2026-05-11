package com.androidkotlin.timestampkt.domain.model

import java.util.UUID

data class LeaveRecord(
    val id: String = UUID.randomUUID().toString(),
    val type: String = "휴가",
    val date: String,
    val amount: Double = 1.0,
    val note: String = ""
)
