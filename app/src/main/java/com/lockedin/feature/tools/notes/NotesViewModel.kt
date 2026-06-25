package com.lockedin.feature.tools.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lockedin.data.db.entity.NoteEntity
import com.lockedin.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class NotesUiState(
    val notes: List<NoteEntity> = emptyList(),
    val editingNote: NoteEntity? = null,
    val isEditing: Boolean = false
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private var saveJob: Job? = null

    init {
        viewModelScope.launch {
            noteRepository.getAllNotes().collect { notes ->
                _uiState.update { it.copy(notes = notes) }
            }
        }
    }

    fun createNewNote() {
        val newNote = NoteEntity(
            title = "",
            content = "",
            updatedAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            val id = noteRepository.insertNote(newNote)
            val created = newNote.copy(id = id)
            _uiState.update { it.copy(editingNote = created, isEditing = true) }
        }
    }

    fun openNote(note: NoteEntity) {
        _uiState.update { it.copy(editingNote = note, isEditing = true) }
    }

    fun closeEditor() {
        saveJob?.cancel()
        _uiState.update { it.copy(editingNote = null, isEditing = false) }
    }

    fun updateNoteTitle(title: String) {
        val current = _uiState.value.editingNote ?: return
        val updated = current.copy(title = title, updatedAt = System.currentTimeMillis())
        _uiState.update { it.copy(editingNote = updated) }
        debounceSave(updated)
    }

    fun updateNoteContent(content: String) {
        val current = _uiState.value.editingNote ?: return
        val updated = current.copy(content = content, updatedAt = System.currentTimeMillis())
        _uiState.update { it.copy(editingNote = updated) }
        debounceSave(updated)
    }

    private fun debounceSave(note: NoteEntity) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500L) // 500ms debounce
            noteRepository.updateNote(note)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            noteRepository.deleteNote(note)
            if (_uiState.value.editingNote?.id == note.id) {
                closeEditor()
            }
        }
    }

    // Returns the deleted note for undo
    fun deleteNoteWithUndo(note: NoteEntity): NoteEntity {
        viewModelScope.launch {
            noteRepository.deleteNote(note)
        }
        return note
    }

    fun undoDelete(note: NoteEntity) {
        viewModelScope.launch {
            noteRepository.insertNote(note)
        }
    }
}
