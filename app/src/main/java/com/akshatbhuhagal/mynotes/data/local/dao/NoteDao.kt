package com.akshatbhuhagal.mynotes.data.local.dao

import androidx.room.*
import com.akshatbhuhagal.mynotes.data.local.entities.NoteEntity

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY id DESC")
    suspend fun getAllNotes(): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getSpecificNote(id: Int): NoteEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(note: NoteEntity)

    @Delete
    suspend fun deleteNotes(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteSpecificNote(id: Int)

    @Update
    suspend fun updateNotes(note: NoteEntity)
}
