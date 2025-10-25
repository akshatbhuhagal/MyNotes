package com.axatabyss.mynotes.presentation.create_notes

import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.axatabyss.mynotes.R
import com.axatabyss.mynotes.databinding.FragmentCreateNoteBinding
import com.axatabyss.mynotes.data.local.entities.NoteEntity
import com.axatabyss.mynotes.util.extensions.EMPTY_STRING
import com.axatabyss.mynotes.util.extensions.makeGone
import com.axatabyss.mynotes.util.extensions.makeVisible
import com.axatabyss.mynotes.util.viewBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri

@AndroidEntryPoint
class CreateNoteFragment : Fragment(R.layout.fragment_create_note) {

    private val binding by viewBinding(FragmentCreateNoteBinding::bind)
    private val viewModel by viewModels<CreateNoteViewModel>()

    var selectedColor = "#3e434e"
    private var currentTime: String? = null

    private var webLink = EMPTY_STRING
    private var selectedImagePath = EMPTY_STRING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireArguments().getInt(getString(R.string.noteID), -1).also {
            if(it != -1) viewModel.setNoteId(it)
        }
    }

    companion object {

        const val NOTE_BOTTOM_SHEET_TAG = "Note Bottom Sheet Fragment"
        const val SELECTED_COLOR = "selectedColor"

        @JvmStatic
        fun newInstance() =
            CreateNoteFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        collectNotes()
    }

    private fun collectNotes() = viewLifecycleOwner.lifecycleScope.launch {
        viewModel.note.collectLatest {
            it?.let(this@CreateNoteFragment::setNoteDataInUI)
        }
    }

    private fun setNoteDataInUI(note: NoteEntity) = binding.apply {
        colorView.setBackgroundColor(Color.parseColor(note.color))
        etNoteTitle.setText(note.title)
        etNoteDesc.setText(note.noteText)

        if (note.imgPath != EMPTY_STRING) {
            selectedImagePath = note.imgPath.orEmpty()
            imgNote.setImageBitmap(BitmapFactory.decodeFile(note.imgPath))
            makeVisible(layoutImage, binding.imgNote, binding.imgDelete)
        } else {
            makeGone(layoutImage, binding.imgNote, binding.imgDelete)
        }

        if (note.storeWebLink != EMPTY_STRING) {
            webLink = note.storeWebLink.orEmpty()
            tvWebLink.text = note.storeWebLink
            makeVisible(layoutWebUrl,imgUrlDelete)
            etWebLink.setText(note.storeWebLink)
        } else {
            makeGone(imgUrlDelete,layoutWebUrl)
        }
    }

    private fun initViews() = binding.apply {
        // Register & Unregister broadcast receiver
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            broadcastReceiver, IntentFilter("bottom_sheet_action")
        )

        colorView.setBackgroundColor(selectedColor.toColorInt())

        // Date & Time
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        currentTime = sdf.format(Date())

        tvDateTime.text = currentTime

        // Done
        imgDone.setOnClickListener {
            viewModel.note.value?.let { updateNote(it) } ?: saveNote()
        }

        // Back Button
        imgBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Show More Button
        imgMore.setOnClickListener {
            val noteBottomSheetFragment = NoteBottomSheetFragment.newInstance(viewModel.noteId.value)
            noteBottomSheetFragment.show(
                requireActivity().supportFragmentManager,
                NOTE_BOTTOM_SHEET_TAG

            )
        }

        // Delete Image
        imgDelete.setOnClickListener {
            selectedImagePath = EMPTY_STRING
            layoutImage.visibility = View.GONE
        }

        btnOk.setOnClickListener {
            if (etWebLink.text.toString().trim().isNotEmpty()) {
                checkWebUrl()
            } else {
                Toast.makeText(requireContext(), getString(R.string.url_require), Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            if (viewModel.noteId.value != null) {
                tvWebLink.makeVisible()
                layoutWebUrl.makeGone()
            } else {
                layoutWebUrl.makeGone()
            }
        }

        imgUrlDelete.setOnClickListener {
            webLink = EMPTY_STRING
            makeGone(tvWebLink, imgUrlDelete, layoutWebUrl)
        }

        tvWebLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, etWebLink.text.toString().toUri())
            startActivity(intent)
        }
    }

    private fun updateNote(note: NoteEntity) = viewLifecycleOwner.lifecycleScope.launch {
        note.apply {
            title = binding.etNoteTitle.text.toString()
            noteText = binding.etNoteDesc.text.toString()
            dateTime = currentTime
            color = selectedColor
            imgPath = selectedImagePath
            storeWebLink = webLink
        }.also {
            viewModel.updateNote(it)
        }
        binding.etNoteTitle.setText(EMPTY_STRING)
        binding.etNoteDesc.setText(EMPTY_STRING)
        makeGone(
            with(binding) {
                layoutImage
                imgNote
                tvWebLink
            }
        )
        requireActivity().supportFragmentManager.popBackStack()
    }


    private fun saveNote() {

        val etNoteTitle = view?.findViewById<EditText>(R.id.etNoteTitle)
        val etNoteDesc = view?.findViewById<EditText>(R.id.etNoteDesc)

        when {
            etNoteTitle?.text.isNullOrEmpty() -> {
                Snackbar.make(requireView(), getString(R.string.title_require), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.snackbarok)) {

                    }.show()
            }
            etNoteDesc?.text.isNullOrEmpty() -> {
                Snackbar.make(
                    requireView(),
                    getString(R.string.empty_note_description_warning),
                    Snackbar.LENGTH_LONG
                ).setAction(getString(R.string.snackbarok)) {

                }.show()
            }
            else -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    NoteEntity().apply {
                        title = etNoteTitle.text.toString()
                        noteText = etNoteDesc.text.toString()
                        dateTime = currentTime
                        color = selectedColor
                        imgPath = selectedImagePath
                        storeWebLink = webLink
                    }.also {
                        viewModel.saveNote(it)
                    }
                    etNoteTitle.setText(EMPTY_STRING)
                    etNoteDesc.setText(EMPTY_STRING)
                    makeGone(with(binding) {
                        layoutImage
                        imgNote
                        tvWebLink
                    })
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun deleteNote() = viewLifecycleOwner.lifecycleScope.launch {
        viewModel.deleteNote()
        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun checkWebUrl() {
        if (Patterns.WEB_URL.matcher(binding.etWebLink.text.toString()).matches()) {
            binding.layoutWebUrl.makeGone()
            binding.etWebLink.isEnabled = false
            webLink = binding.etWebLink.text.toString()
            binding.tvWebLink.makeVisible()
            binding.tvWebLink.text = binding.etWebLink.text.toString()
        } else {
            Toast.makeText(requireContext(), getString(R.string.url_validation), Toast.LENGTH_SHORT).show()
        }
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {

            if (p1 == null || view == null)
                return

            val actionColor = p1.getStringExtra(getString(R.string.action))


            view?.let { fragmentView ->
                val binding = FragmentCreateNoteBinding.bind(fragmentView)

                when (actionColor) {

                    getString(R.string.blue) -> {
                        selectedColor = p1.getStringExtra(SELECTED_COLOR) ?: ""
                        binding.colorView.setBackgroundColor(selectedColor.toColorInt())
                    }

                    getString(R.string.cyan) -> {
                        selectedColor = p1.getStringExtra(SELECTED_COLOR) ?: ""
                        binding.colorView.setBackgroundColor(selectedColor.toColorInt())
                    }

                    getString(R.string.green) -> {
                        selectedColor = p1.getStringExtra(SELECTED_COLOR) ?: ""
                        binding.colorView.setBackgroundColor(selectedColor.toColorInt())
                    }

                    getString(R.string.orange) -> {
                        selectedColor = p1.getStringExtra(SELECTED_COLOR) ?: ""
                        binding.colorView.setBackgroundColor(selectedColor.toColorInt())
                    }

                    getString(R.string.purple) -> {
                        selectedColor = p1.getStringExtra(SELECTED_COLOR) ?: ""
                        binding.colorView.setBackgroundColor(selectedColor.toColorInt())
                    }

                    getString(R.string.red) -> {
                        selectedColor = p1.getStringExtra(SELECTED_COLOR) ?: ""
                        binding.colorView.setBackgroundColor(selectedColor.toColorInt())
                    }

                    getString(R.string.yellow) -> {
                        selectedColor = p1.getStringExtra(SELECTED_COLOR) ?: ""
                        binding.colorView.setBackgroundColor(selectedColor.toColorInt())
                    }

                    getString(R.string.brown) -> {
                        selectedColor = p1.getStringExtra(SELECTED_COLOR) ?: ""
                        binding.colorView.setBackgroundColor(selectedColor.toColorInt())
                    }

                    getString(R.string.indigo) -> {
                        selectedColor = p1.getStringExtra(SELECTED_COLOR) ?: ""
                        binding.colorView.setBackgroundColor(selectedColor.toColorInt())
                    }

                    getString(R.string.image) -> {
                        readStorageTask()
                        binding.layoutWebUrl.makeGone()
                    }

                    getString(R.string.webUrl) -> {
                        binding.layoutWebUrl.visibility = View.VISIBLE
                    }

                    getString(R.string.deleteNote) -> {
                        deleteNote()
                    }

                    else -> {
                        binding.layoutImage.visibility = View.GONE
                        binding.imgNote.visibility = View.GONE
                        binding.layoutWebUrl.visibility = View.GONE
                        selectedColor = p1.getStringExtra(SELECTED_COLOR) ?: ""
                        makeGone(with(binding) {
                            layoutImage
                            imgNote
                            layoutWebUrl
                        })
                        selectedColor = p1.getStringExtra(SELECTED_COLOR).orEmpty()
                        binding.colorView.setBackgroundColor(selectedColor.toColorInt())
                    }
                }
            }
        }
    }

    private fun pickImageFromGallery() {
        // Launch photo picker for images only
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    // Image Integration
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after user selects media or closes picker
        uri?.let {
            try {
                val inputStream = requireActivity().contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                binding.imgNote.setImageBitmap(bitmap)
                binding.imgNote.makeVisible()
                binding.layoutImage.makeVisible()

                // For Android 13+, use the URI directly instead of file path
                selectedImagePath = it.toString()

                // Optional: Get persistent access to the URI
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                requireActivity().contentResolver.takePersistableUriPermission(it, takeFlags)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            // User cancelled - no media selected
            Log.d("PhotoPicker", "No media selected")
        }
    }

    private fun readStorageTask() {
        pickImageFromGallery()
    }

}
