package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class Album(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_images")
data class StudyImage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val albumId: Int,
    val uriString: String,
    val sequenceOrder: Int,
    val note: String = "",
    val addedAt: Long = System.currentTimeMillis()
)
