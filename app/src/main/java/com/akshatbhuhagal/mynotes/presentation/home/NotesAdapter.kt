package com.akshatbhuhagal.mynotes.presentation.home

import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.akshatbhuhagal.mynotes.R
import com.akshatbhuhagal.mynotes.data.local.entities.NoteEntity
import kotlinx.android.synthetic.main.item_rv_notes.view.cardView
import kotlinx.android.synthetic.main.item_rv_notes.view.imgNote
import kotlinx.android.synthetic.main.item_rv_notes.view.tvDateTime
import kotlinx.android.synthetic.main.item_rv_notes.view.tvDesc
import kotlinx.android.synthetic.main.item_rv_notes.view.tvTitle
import kotlinx.android.synthetic.main.item_rv_notes.view.tvWebLink
import com.akshatbhuhagal.mynotes.util.extensions.makeGone
import com.akshatbhuhagal.mynotes.util.extensions.makeInvisible
import com.akshatbhuhagal.mynotes.util.extensions.makeVisible
import kotlinx.android.synthetic.main.item_rv_notes.view.*

class NotesAdapter : ListAdapter<NoteEntity, NotesAdapter.NotesViewHolder>(diffCallback) {

    private lateinit var listener: OnItemClickListener

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        return NotesViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_rv_notes, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {

        val item = getItem(position)

        holder.itemView.tvTitle.text = item.title
        holder.itemView.tvDesc.text = item.noteText
        holder.itemView.tvDateTime.text = item.dateTime

        if (item.color != null) {
            holder.itemView.cardView.setCardBackgroundColor(Color.parseColor(item.color))
        }
        if (item.imgPath != null) {
            holder.itemView.imgNote.setImageBitmap(BitmapFactory.decodeFile(item.imgPath))
            holder.itemView.imgNote.makeVisible()
        } else {
            holder.itemView.imgNote.makeGone()
        }

        if (item.webLink != null) {
            holder.itemView.tvWebLink.text = item.webLink
            holder.itemView.tvWebLink.makeVisible()
        } else {
            holder.itemView.tvWebLink.makeGone()
        }

        holder.itemView.cardView.setOnClickListener {
            listener.onClicked(item.id)
        }
    }

    fun setOnClickListener(listener1: OnItemClickListener) {
        listener = listener1
    }

    inner class NotesViewHolder(view: View) : RecyclerView.ViewHolder(view)

    interface OnItemClickListener {
        fun onClicked(notesId: Int)
    }
}
