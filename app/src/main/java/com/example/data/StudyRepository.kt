package com.example.data

import kotlinx.coroutines.flow.Flow

class StudyRepository(private val studyDao: StudyDao) {
    val allAlbums: Flow<List<Album>> = studyDao.getAllAlbums()

    fun getAlbumById(id: Int) = studyDao.getAlbumById(id)
    fun getImagesForAlbum(albumId: Int) = studyDao.getImagesForAlbum(albumId)

    suspend fun insertAlbum(album: Album) = studyDao.insertAlbum(album)
    suspend fun deleteAlbumById(id: Int) = studyDao.deleteAlbumById(id)

    suspend fun getNextSequenceOrder(albumId: Int) = studyDao.getNextSequenceOrder(albumId)
    suspend fun insertStudyImage(studyImage: StudyImage) = studyDao.insertStudyImage(studyImage)
    suspend fun updateStudyImage(studyImage: StudyImage) = studyDao.updateStudyImage(studyImage)
    suspend fun updateStudyImages(studyImages: List<StudyImage>) = studyDao.updateStudyImages(studyImages)
    suspend fun deleteStudyImageById(id: Int) = studyDao.deleteStudyImageById(id)
}
