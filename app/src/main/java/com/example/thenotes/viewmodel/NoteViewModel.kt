package com.example.thenotes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.thenotes.model.Note
import com.example.thenotes.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(app: Application, private val noteRepository: NoteRepository): AndroidViewModel(app) {

    fun addNote(note: Note) = viewModelScope.launch {
        noteRepository.insertNote(note)
    }

    fun deleteNote(note: Note) = viewModelScope.launch {
        noteRepository.deleteNote(note)
    }

//    fun updateNote(note: Note) = viewModelScope.launch {
//        noteRepository.updateNote(note)
//    }
    fun updateNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.updateNote(note)
        }
    }

    fun getAllNotes() = noteRepository.getAllNotes()

    fun searchNotes(query: String?) = noteRepository.searchNotes(query)

    fun getFavoriteNotes() = noteRepository.getFavNote()


}