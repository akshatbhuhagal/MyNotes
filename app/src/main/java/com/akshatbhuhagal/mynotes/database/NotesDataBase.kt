package com.akshatbhuhagal.mynotes.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.akshatbhuhagal.mynotes.dao.NoteDao
import com.akshatbhuhagal.mynotes.entities.Notes

@Database(entities = [Notes::class], version = 1, exportSchema = false)
abstract class NotesDataBase : RoomDatabase() {

    companion object {

        private var notesDataBase: NotesDataBase? = null

        @Synchronized
        fun getDataBase(context: Context): NotesDataBase {

            if (notesDataBase == null) {
                notesDataBase = Room.databaseBuilder(context, NotesDataBase::class.java, "notes.db").build()
            }
            return notesDataBase!!
        }
    }

    abstract fun noteDao(): NoteDao
}
