package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "hosts")
data class Host(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val specialties: String, // Comma-separated specialties
    val rating: Double,
    val pricingPerMinute: Double,
    val wisdomScore: Int,
    val trustScore: Int,
    val bio: String,
    val isVerified: Boolean = true
) : Serializable

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String, // e.g. "Self-Inquiry", "Logic", "Vedanta"
    val hostName: String? = null
) : Serializable

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hostName: String,
    val durationSeconds: Int,
    val cost: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val summary: String,
    val listeningScore: Int, // 1-100 scale
    val curiosityScore: Int, // 1-100 scale
    val respectScore: Int // 1-100 scale
) : Serializable

@Entity(tableName = "user_badges")
data class UserBadge(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val icon: String, // Icon name
    val description: String,
    val unlockedDate: Long = System.currentTimeMillis()
) : Serializable
