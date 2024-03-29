package com.akshatbhuhagal.mynotes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.akshatbhuhagal.mynotes.data.local.dao.NoteDao
import com.akshatbhuhagal.mynotes.data.local.entities.NoteEntity

@Database(entities = [NoteEntity::class], version = 1, exportSchema = false)
abstract class NotesDataBase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
}
