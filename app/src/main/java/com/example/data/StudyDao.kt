package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {
    @Query("SELECT * FROM albums ORDER BY createdAt DESC")
    fun getAllAlbums(): Flow<List<Album>>

    @Query("SELECT * FROM albums WHERE id = :id")
    fun getAlbumById(id: Int): Flow<Album?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: Album): Long

    @Query("DELETE FROM albums WHERE id = :id")
    suspend fun deleteAlbumById(id: Int)

    @Query("SELECT * FROM study_images WHERE albumId = :albumId ORDER BY sequenceOrder ASC")
    fun getImagesForAlbum(albumId: Int): Flow<List<StudyImage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyImage(studyImage: StudyImage)

    @Update
    suspend fun updateStudyImage(studyImage: StudyImage)

    @Update
    suspend fun updateStudyImages(studyImages: List<StudyImage>)

    @Query("DELETE FROM study_images WHERE id = :id")
    suspend fun deleteStudyImageById(id: Int)
}
