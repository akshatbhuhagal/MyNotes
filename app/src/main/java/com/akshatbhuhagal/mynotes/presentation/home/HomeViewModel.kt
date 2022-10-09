package com.akshatbhuhagal.mynotes.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akshatbhuhagal.mynotes.data.repo.NotesRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val notesRepo: NotesRepo) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val notes = searchQuery.flatMapLatest { query->
        notesRepo.notes.map { it.filter { it.title?.contains(query, ignoreCase = true) == true } }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun onSearchQueryChanged(query:String) = viewModelScope.launch {
        searchQuery.emit(query)
    }

}