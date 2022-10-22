package com.akshatbhuhagal.mynotes.presentation.create_notes

import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.akshatbhuhagal.mynotes.R
import com.akshatbhuhagal.mynotes.databinding.FragmentCreateNoteBinding
import com.akshatbhuhagal.mynotes.data.local.entities.NoteEntity
import com.akshatbhuhagal.mynotes.util.extensions.EMPTY_STRING
import com.akshatbhuhagal.mynotes.util.extensions.makeGone
import com.akshatbhuhagal.mynotes.util.extensions.makeVisible
import com.akshatbhuhagal.mynotes.util.viewBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_create_note.view.*
import kotlinx.coroutines.flow.collectLatest
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class CreateNoteFragment :
    Fragment(R.layout.fragment_create_note),
    EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {

    private val binding by viewBinding(FragmentCreateNoteBinding::bind)
    private val viewModel by viewModels<CreateNoteViewModel>()

    var selectedColor = "#3e434e"
    private var currentTime: String? = null

    // Permission Private Read & Write
    private var READ_STORAGE_PERM = 123
    private var REQUEST_CODE_IMAGE = 456

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

    private fun collectNotes() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
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
            makeVisible(layoutImage,imgNote.imgDelete)
        } else {
            /* Comment this function if application is crashing */
            makeGone(layoutImage,imgNote.imgDelete)
        }

        if (note.webLink != EMPTY_STRING) {
            webLink = note.webLink.orEmpty()
            tvWebLink.text = note.webLink
            makeVisible(layoutWebUrl,imgUrlDelete)
            etWebLink.setText(note.webLink)
        } else {
            /* Comment this function if application is crashing */
            makeGone(imgUrlDelete,layoutWebUrl)
        }
    }

    private fun initViews() = binding.apply {
        // Register & Unregister broadcast receiver
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            BroadcastReceiver, IntentFilter("bottom_sheet_action")
        )

        colorView.setBackgroundColor(Color.parseColor(selectedColor))

        // Date & Time
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ROOT)
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
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(etWebLink.text.toString()))
            startActivity(intent)
        }
    }

    private fun updateNote(note: NoteEntity) = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        note.apply {
            title = binding.etNoteTitle.text.toString()
            noteText = binding.etNoteDesc.text.toString()
            dateTime = currentTime
            color = selectedColor
            imgPath = selectedImagePath
            webLink = webLink
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
                    .setAction(getString(R.string.snackbarok)) { null }
                    .show()
            }
            etNoteDesc?.text.isNullOrEmpty() -> {
                Snackbar.make(
                    requireView(),
                    getString(R.string.empty_note_description_warning),
                    Snackbar.LENGTH_LONG
                ).setAction(getString(R.string.snackbarok)) { null }
                    .show()
            }
            else -> {
                viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                    NoteEntity().apply {
                        title = etNoteTitle?.text.toString()
                        noteText = etNoteDesc?.text.toString()
                        dateTime = currentTime
                        color = selectedColor
                        imgPath = selectedImagePath
                        webLink = webLink
                    }.also {
                        viewModel.saveNote(it)
                    }
                    etNoteTitle?.setText(EMPTY_STRING)
                    etNoteDesc?.setText(EMPTY_STRING)
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

    private fun deleteNote() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
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

    fun replaceFragment(fragment: Fragment, isTransition: Boolean) {

        val fragmentTransition = requireActivity().supportFragmentManager.beginTransaction()

        if (isTransition) {
            fragmentTransition.setCustomAnimations(
                android.R.anim.slide_out_right,
                android.R.anim.slide_in_left
            )
        }
        fragmentTransition.replace(R.id.flFragmenet, fragment)
            .addToBackStack(fragment.javaClass.simpleName)
        fragmentTransition.commit()
    }

    private val BroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {

            if (p1 == null)
                return

            val actionColor = p1.getStringExtra(getString(R.string.action))

            binding.apply {
                when (actionColor) {

                    getString(R.string.blue) -> {
                        selectedColor = p1.getStringExtra(SELECTED_COLOR) ?: ""
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                    }

                    getString(R.string.cyan) -> {
                        selectedColor = p1.getStringExtra(SELECTED_COLOR) ?: ""
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                    }

                    getString(R.string.green) -> {
                        selectedColor = p1.getStringExtra(SELECTED_COLOR) ?: ""
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                    }

                    getString(R.string.orange) -> {
                        selectedColor = p1.getStringExtra(SELECTED_COLOR) ?: ""
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                    }

                    getString(R.string.purple) -> {
                        selectedColor = p1.getStringExtra(SELECTED_COLOR) ?: ""
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                    }

                    getString(R.string.red) -> {
                        selectedColor = p1.getStringExtra(SELECTED_COLOR) ?: ""
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                    }

                    getString(R.string.yellow) -> {
                        selectedColor = p1.getStringExtra(SELECTED_COLOR) ?: ""
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                    }

                    getString(R.string.brown) -> {
                        selectedColor = p1.getStringExtra(SELECTED_COLOR) ?: ""
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                    }

                    getString(R.string.indigo) -> {
                        selectedColor = p1.getStringExtra(SELECTED_COLOR) ?: ""
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                    }

                    getString(R.string.image) -> {
                        readStorageTask()
                        binding.layoutWebUrl.makeGone()
                    }

                    getString(R.string.webUrl) -> {
                        binding.layoutWebUrl.visibility = View.VISIBLE
                    }

                    getString(R.string.webUrl) -> {
                        deleteNote()
                    }

                    else -> {
                        binding.layoutImage.visibility = View.GONE
                        imgNote.visibility = View.GONE
                        binding.layoutWebUrl.visibility = View.GONE
                        selectedColor = p1.getStringExtra(SELECTED_COLOR) ?: ""
                        makeGone(with(binding) {
                            layoutImage
                            imgNote
                            layoutWebUrl
                        })
                        selectedColor = p1.getStringExtra(SELECTED_COLOR).orEmpty()
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                    }
                }
            }
        }
    }

    private fun hasReadStoragePerm(): Boolean {
        return EasyPermissions.hasPermissions(
            requireContext(),
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private fun readStorageTask() {
        if (hasReadStoragePerm()) {

            pickImageFromGallery()
        } else {
            EasyPermissions.requestPermissions(
                requireActivity(), getString(R.string.storage_permission_text),
                READ_STORAGE_PERM, android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(intent, REQUEST_CODE_IMAGE)
        }
    }

    private fun getPathFromUri(contentUri: Uri): String? {
        var filePath: String?
        val cursor = requireActivity().contentResolver.query(contentUri, null, null, null, null)
        if (cursor == null) {
            filePath = contentUri.path
        } else {
            cursor.moveToFirst()
            val index = cursor.getColumnIndex("_data")
            filePath = cursor.getString(index)
            cursor.close()
        }
        return filePath
    }

    // Setup About Image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {

                val selectedImageUrl = data.data

                if (selectedImageUrl != null) {
                    try {

                        val inputStream =
                            requireActivity().contentResolver.openInputStream(selectedImageUrl)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        binding.imgNote.setImageBitmap(bitmap)
                        makeVisible(with(binding) {
                            imgNote
                            layoutImage
                        })
                        selectedImagePath = getPathFromUri(selectedImageUrl).orEmpty()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            requireActivity()
        )
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(requireActivity(), perms)) {
            AppSettingsDialog.Builder(requireActivity()).build().show()
        }
    }

    override fun onRationaleAccepted(requestCode: Int) {
    }

    override fun onRationaleDenied(requestCode: Int) {
    }
}
