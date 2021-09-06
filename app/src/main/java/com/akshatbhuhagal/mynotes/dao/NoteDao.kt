package com.akshatbhuhagal.mynotes.dao

import androidx.room.*
import com.akshatbhuhagal.mynotes.entities.Notes


@Dao
interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY id DESC")
    suspend fun getAllNotes() : List<Notes>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(note: Notes)

    @Delete
    suspend fun deleteNotes(note: Notes)


}