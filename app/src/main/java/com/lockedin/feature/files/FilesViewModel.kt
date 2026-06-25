package com.lockedin.feature.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lockedin.data.db.entity.FileEntity
import com.lockedin.data.repository.FileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilesViewModel @Inject constructor(
    private val fileRepository: FileRepository
) : ViewModel() {

    val files: StateFlow<List<FileEntity>> = fileRepository.getAllFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteFile(file: FileEntity) {
        viewModelScope.launch {
            fileRepository.deleteFile(file)
        }
    }

    fun getFileById(id: Long, onResult: (FileEntity?) -> Unit) {
        viewModelScope.launch {
            val file = fileRepository.getFileById(id)
            onResult(file)
        }
    }
}
