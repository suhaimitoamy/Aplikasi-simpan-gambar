package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Album
import com.example.data.StudyImage
import com.example.data.StudyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudyViewModel(private val repository: StudyRepository) : ViewModel() {
    val allAlbums: StateFlow<List<Album>> = repository.allAlbums
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createAlbum(title: String) {
        viewModelScope.launch {
            repository.insertAlbum(Album(title = title))
        }
    }

    fun deleteAlbum(albumId: Int) {
        viewModelScope.launch {
            repository.deleteAlbumById(albumId)
        }
    }

    fun getImagesForAlbum(albumId: Int): StateFlow<List<StudyImage>> {
        return repository.getImagesForAlbum(albumId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
    
    fun getAlbumById(albumId: Int): StateFlow<Album?> {
        return repository.getAlbumById(albumId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    fun addImageToAlbum(albumId: Int, uriString: String, currentCount: Int) {
        viewModelScope.launch {
            repository.insertStudyImage(
                StudyImage(
                    albumId = albumId,
                    uriString = uriString,
                    sequenceOrder = currentCount
                )
            )
        }
    }

    fun addImagesToAlbum(albumId: Int, uriStrings: List<String>) {
        viewModelScope.launch {
            val startOrder = repository.getNextSequenceOrder(albumId)
            uriStrings.forEachIndexed { index, uriString ->
                repository.insertStudyImage(
                    StudyImage(
                        albumId = albumId,
                        uriString = uriString,
                        sequenceOrder = startOrder + index
                    )
                )
            }
        }
    }

    fun deleteImage(imageId: Int) {
        viewModelScope.launch {
            repository.deleteStudyImageById(imageId)
        }
    }

    fun moveImage(images: List<StudyImage>, fromIndex: Int, toIndex: Int) {
        if (fromIndex < 0 || toIndex < 0 || fromIndex >= images.size || toIndex >= images.size) return
        val updatedList = images.toMutableList()
        val item = updatedList.removeAt(fromIndex)
        updatedList.add(toIndex, item)
        
        val reordered = updatedList.mapIndexed { index, studyImage ->
            studyImage.copy(sequenceOrder = index)
        }
        viewModelScope.launch {
            repository.updateStudyImages(reordered)
        }
    }
}

class StudyViewModelFactory(private val repository: StudyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
