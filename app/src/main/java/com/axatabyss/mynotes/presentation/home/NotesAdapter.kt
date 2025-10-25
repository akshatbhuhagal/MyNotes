package com.axatabyss.mynotes.presentation.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.axatabyss.mynotes.data.local.entities.NoteEntity
import com.axatabyss.mynotes.databinding.ItemRvNotesBinding
import androidx.core.net.toUri

class NotesAdapter : ListAdapter<NoteEntity, NotesAdapter.NotesViewHolder>(diffCallback) {

    private lateinit var listener: OnItemClickListener

    inner class NotesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ItemRvNotesBinding.bind(view)

        fun bind(note: NoteEntity) {
            with(binding) {
                tvTitle.text = note.title
                tvDesc.text = note.noteText
                tvDateTime.text = note.dateTime

                note.color?.let {
                    cardView.setCardBackgroundColor(Color.parseColor(note.color))
                }

                if (note.imgPath.isNullOrEmpty().not()) {
                    binding.root.context.getBitmapFromPath(note.imgPath!!)?.let { bitmap ->
                        imgNote.setImageBitmap(bitmap)
                        imgNote.visibility = View.VISIBLE
                    } ?: run {
                        imgNote.visibility = View.GONE
                    }
                } else {
                    imgNote.visibility = View.GONE
                }

                if (note.storeWebLink.isNullOrEmpty().not()) {
                    tvWebLink.text = note.storeWebLink
                    tvWebLink.visibility = View.VISIBLE
                } else {
                    tvWebLink.visibility = View.GONE
                }

                cardView.setOnClickListener {
                    listener.onClicked(note.id)
                }
            }
        }
    }

    fun Context.getBitmapFromPath(path: String): Bitmap? {
        return try {
            if (path.startsWith("content://")) {
                contentResolver.openInputStream(path.toUri())?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            } else {
                BitmapFactory.decodeFile(path)
            }
        } catch (e: Exception) {
            Log.e("BitmapHelper", "Failed to load bitmap from: $path", e)
            null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val binding = ItemRvNotesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotesViewHolder(binding.root)
    }


    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setOnClickListener(listener1: OnItemClickListener) {
        listener = listener1
    }

    interface OnItemClickListener {
        fun onClicked(notesId: Int)
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<NoteEntity>() {
            override fun areItemsTheSame(oldItem: NoteEntity, newItem: NoteEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: NoteEntity, newItem: NoteEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}
