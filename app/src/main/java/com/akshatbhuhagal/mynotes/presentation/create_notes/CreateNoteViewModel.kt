package com.akshatbhuhagal.mynotes.presentation.create_notes

import android.provider.ContactsContract.CommonDataKinds.Note
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akshatbhuhagal.mynotes.data.local.entities.NoteEntity
import com.akshatbhuhagal.mynotes.data.repo.NotesRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateNoteViewModel @Inject constructor(private val notesRepo: NotesRepo) : ViewModel() {

    val noteId = MutableStateFlow<Int?>(null)

    val note = noteId.flatMapLatest {
        val note = it?.let { notesRepo.getNote(it) }
        flowOf(note)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setNoteId(id:Int) = viewModelScope.launch {
        noteId.emit(id)
    }

    suspend fun updateNote(noteEntity: NoteEntity) = notesRepo.updateNotes(noteEntity)

    suspend fun saveNote(noteEntity: NoteEntity) = notesRepo.insertNote(noteEntity)

    suspend fun deleteNote() = noteId.value?.let { notesRepo.deleteNoteById(it) }

}