package com.akshatbhuhagal.mynotes.presentation.create_notes

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.akshatbhuhagal.mynotes.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_notes_bottom_sheet.*

class NoteBottomSheetFragment : BottomSheetDialogFragment() {

    private var selectedColor = "#3e434e"

    companion object {

        var noteId:Int? = null

        fun newInstance(id: Int?): NoteBottomSheetFragment {
            val args = Bundle()
            val fragment = NoteBottomSheetFragment()
            fragment.arguments = args
            noteId = id
            return fragment
        }
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        val view = LayoutInflater.from(context).inflate(R.layout.fragment_notes_bottom_sheet, null)
        dialog.setContentView(view)

        val param = (view.parent as View).layoutParams as CoordinatorLayout.LayoutParams

        val behavior = param.behavior

        if (behavior is BottomSheetBehavior<*>) {

            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    var state = ""
                    while (newState == newState) {
                        BottomSheetBehavior.STATE_DRAGGING.apply {
                            state = "DRAGGING"
                        }
                        BottomSheetBehavior.STATE_SETTLING.apply {
                            state = "SETTILING"
                        }
                        BottomSheetBehavior.STATE_EXPANDED.apply {
                            state = "EXPANDED"
                        }
                        BottomSheetBehavior.STATE_COLLAPSED.apply {
                            state = "COLLAPSED"
                        }
                        BottomSheetBehavior.STATE_HIDDEN.apply {
                            state = "HIDDEN"
                            dismiss()
                            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                }
            })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notes_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (noteId != null) {
            layoutDeleteNote.visibility = View.VISIBLE
        } else {
            layoutDeleteNote.visibility = View.GONE
        }
        setListener()
    }

    private fun setListener() {

        fNoteBlue.setOnClickListener {

            imgNoteBlue.setImageResource(R.drawable.donecheck)
            imgNoteCyan.setImageResource(0)
            imgNoteGreen.setImageResource(0)
            imgNoteOrange.setImageResource(0)
            imgNotePurple.setImageResource(0)
            imgNoteRed.setImageResource(0)
            imgNoteYellow.setImageResource(0)
            imgNoteBrown.setImageResource(0)
            imgNoteIndigo.setImageResource(0)
            selectedColor = "#2196f3"

            val intent = Intent("bottom_sheet_action")
            intent.putExtra("action", "Blue")
            intent.putExtra("selectedColor", selectedColor)
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        }

        fNoteCyan.setOnClickListener {

            imgNoteBlue.setImageResource(0)
            imgNoteCyan.setImageResource(R.drawable.donecheck)
            imgNoteGreen.setImageResource(0)
            imgNoteOrange.setImageResource(0)
            imgNotePurple.setImageResource(0)
            imgNoteRed.setImageResource(0)
            imgNoteYellow.setImageResource(0)
            imgNoteBrown.setImageResource(0)
            imgNoteIndigo.setImageResource(0)
            selectedColor = "#00e5ff"

            val intent = Intent("bottom_sheet_action")
            intent.putExtra("action", "Cyan")
            intent.putExtra("selectedColor", selectedColor)
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        }

        fNoteGreen.setOnClickListener {

            imgNoteBlue.setImageResource(0)
            imgNoteCyan.setImageResource(0)
            imgNoteGreen.setImageResource(R.drawable.donecheck)
            imgNoteOrange.setImageResource(0)
            imgNotePurple.setImageResource(0)
            imgNoteRed.setImageResource(0)
            imgNoteYellow.setImageResource(0)
            imgNoteBrown.setImageResource(0)
            imgNoteIndigo.setImageResource(0)
            selectedColor = "#00c853"

            val intent = Intent("bottom_sheet_action")
            intent.putExtra("action", "Green")
            intent.putExtra("selectedColor", selectedColor)
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        }

        fNoteOrange.setOnClickListener {

            imgNoteBlue.setImageResource(0)
            imgNoteCyan.setImageResource(0)
            imgNoteGreen.setImageResource(0)
            imgNoteOrange.setImageResource(R.drawable.donecheck)
            imgNotePurple.setImageResource(0)
            imgNoteRed.setImageResource(0)
            imgNoteYellow.setImageResource(0)
            imgNoteBrown.setImageResource(0)
            imgNoteIndigo.setImageResource(0)
            selectedColor = "#ff6d00"

            val intent = Intent("bottom_sheet_action")
            intent.putExtra("action", "Orange")
            intent.putExtra("selectedColor", selectedColor)
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        }

        fNotePurple.setOnClickListener {

            imgNoteBlue.setImageResource(0)
            imgNoteCyan.setImageResource(0)
            imgNoteGreen.setImageResource(0)
            imgNoteOrange.setImageResource(0)
            imgNotePurple.setImageResource(R.drawable.donecheck)
            imgNoteRed.setImageResource(0)
            imgNoteYellow.setImageResource(0)
            imgNoteBrown.setImageResource(0)
            imgNoteIndigo.setImageResource(0)
            selectedColor = "#aa00ff"

            val intent = Intent("bottom_sheet_action")
            intent.putExtra("action", "Purple")
            intent.putExtra("selectedColor", selectedColor)
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        }

        fNoteRed.setOnClickListener {

            imgNoteBlue.setImageResource(0)
            imgNoteCyan.setImageResource(0)
            imgNoteGreen.setImageResource(0)
            imgNoteOrange.setImageResource(0)
            imgNotePurple.setImageResource(0)
            imgNoteRed.setImageResource(R.drawable.donecheck)
            imgNoteYellow.setImageResource(0)
            imgNoteBrown.setImageResource(0)
            imgNoteIndigo.setImageResource(0)
            selectedColor = "#d50000"

            val intent = Intent("bottom_sheet_action")
            intent.putExtra("action", "Red")
            intent.putExtra("selectedColor", selectedColor)
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        }

        fNoteYellow.setOnClickListener {

            imgNoteBlue.setImageResource(0)
            imgNoteCyan.setImageResource(0)
            imgNoteGreen.setImageResource(0)
            imgNoteOrange.setImageResource(0)
            imgNotePurple.setImageResource(0)
            imgNoteRed.setImageResource(0)
            imgNoteYellow.setImageResource(R.drawable.donecheck)
            imgNoteBrown.setImageResource(0)
            imgNoteIndigo.setImageResource(0)
            selectedColor = "#ffeb3b"

            val intent = Intent("bottom_sheet_action")
            intent.putExtra("action", "Yellow")
            intent.putExtra("selectedColor", selectedColor)
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        }

        fNoteBrown.setOnClickListener {

            imgNoteBlue.setImageResource(0)
            imgNoteCyan.setImageResource(0)
            imgNoteGreen.setImageResource(0)
            imgNoteOrange.setImageResource(0)
            imgNotePurple.setImageResource(0)
            imgNoteRed.setImageResource(0)
            imgNoteYellow.setImageResource(0)
            imgNoteBrown.setImageResource(R.drawable.donecheck)
            imgNoteIndigo.setImageResource(0)
            selectedColor = "#3e2723"

            val intent = Intent("bottom_sheet_action")
            intent.putExtra("action", "Brown")
            intent.putExtra("selectedColor", selectedColor)
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        }

        fNoteIndigo.setOnClickListener {

            imgNoteBlue.setImageResource(0)
            imgNoteCyan.setImageResource(0)
            imgNoteGreen.setImageResource(0)
            imgNoteOrange.setImageResource(0)
            imgNotePurple.setImageResource(0)
            imgNoteRed.setImageResource(0)
            imgNoteYellow.setImageResource(0)
            imgNoteBrown.setImageResource(0)
            imgNoteIndigo.setImageResource(R.drawable.donecheck)
            selectedColor = "#1a237e"

            val intent = Intent("bottom_sheet_action")
            intent.putExtra("action", "Indigo")
            intent.putExtra("selectedColor", selectedColor)
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        }
        // FINISH COLORS

        // ADD IMAGE
        layoutImage.setOnClickListener {

            val intent = Intent("bottom_sheet_action")
            intent.putExtra("action", "Image")
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            dismiss()
        }

        // ADD URL
        layoutWebUrl.setOnClickListener {

            val intent = Intent("bottom_sheet_action")
            intent.putExtra("action", "WebUrl")
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            dismiss()
        }

        // Delete Notes
        layoutDeleteNote.setOnClickListener {

            val intent = Intent("bottom_sheet_action")
            intent.putExtra("action", "DeleteNote")
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            dismiss()
        }
    }
}
