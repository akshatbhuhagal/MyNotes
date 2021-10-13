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
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.akshatbhuhagal.mynotes.R
import com.akshatbhuhagal.mynotes.database.NotesDataBase
import com.akshatbhuhagal.mynotes.databinding.FragmentCreateNoteBinding
import com.akshatbhuhagal.mynotes.entities.Notes
import com.akshatbhuhagal.mynotes.util.viewBinding
import com.google.android.material.snackbar.Snackbar
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.*

class CreateNoteFragment :
    Fragment(R.layout.fragment_create_note),
    EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {

    private val binding by viewBinding(FragmentCreateNoteBinding::bind)

    var selectedColor = "#3e434e"
    private var currentTime: String? = null

    // Permission Private Read & Write
    private var READ_STORAGE_PERM = 123
    private var REQUEST_CODE_IMAGE = 456

    private var webLink = ""
    private var selectedImagePath = ""

    private var noteId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        noteId = requireArguments().getInt("noteId", -1)
    }

    companion object {
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
    }

    private fun initViews() = binding.apply {
        if (noteId != -1) {

            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                context?.let {

                    val notes = NotesDataBase.getDataBase(it).noteDao().getSpecificNote(noteId)

                    colorView.setBackgroundColor(Color.parseColor(notes.color))

                    etNoteTitle.setText(notes.title)
                    etNoteDesc.setText(notes.noteText)

                    if (notes.imgPath != "") {
                        selectedImagePath = notes.imgPath!!
                        imgNote.setImageBitmap(BitmapFactory.decodeFile(notes.imgPath))
                        layoutImage.visibility = View.VISIBLE
                        imgNote.visibility = View.VISIBLE
                        imgDelete.visibility = View.VISIBLE
                    } else {
                        layoutImage.visibility = View.GONE
                        imgNote.visibility = View.GONE
                        imgDelete.visibility = View.GONE
                    }

                    if (notes.webLink != "") {
                        webLink = notes.webLink!!
                        tvWebLink.text = notes.webLink
                        layoutWebUrl.visibility = View.VISIBLE
                        imgUrlDelete.visibility = View.VISIBLE
                        etWebLink.setText(notes.webLink)
                    } else {
                        imgUrlDelete.visibility = View.GONE
                        layoutWebUrl.visibility = View.GONE
                    }
                }
            }
        }

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

            if (noteId != -1) {
                updateNote()
            } else {
                saveNote()
            }
        }

        // Back Button
        imgBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Show More Button
        imgMore.setOnClickListener {
            val noteBottomSheetFragment = NoteBottomSheetFragment.newInstance(noteId)
            noteBottomSheetFragment.show(
                requireActivity().supportFragmentManager,
                "Note Bottom Sheet Fragment"
            )
        }

        // Delete Image
        imgDelete.setOnClickListener {
            selectedImagePath = ""
            layoutImage.visibility = View.GONE
        }

        btnOk.setOnClickListener {
            if (etWebLink.text.toString().trim().isNotEmpty()) {
                checkWebUrl()
            } else {
                Toast.makeText(requireContext(), "Url is Required", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            if (noteId != -1) {
                tvWebLink.visibility = View.VISIBLE
                layoutWebUrl.visibility = View.GONE
            } else {
                layoutWebUrl.visibility = View.GONE
            }
        }

        imgUrlDelete.setOnClickListener {
            webLink = ""
            tvWebLink.visibility = View.GONE
            imgUrlDelete.visibility = View.GONE
            layoutWebUrl.visibility = View.GONE
        }

        tvWebLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(etWebLink.text.toString()))
            startActivity(intent)
        }
    }

    private fun updateNote() {

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            context?.let {
                val notes = NotesDataBase.getDataBase(it).noteDao().getSpecificNote(noteId)

                notes.title = binding.etNoteTitle.text.toString()
                notes.noteText = binding.etNoteDesc.text.toString()
                notes.dateTime = currentTime
                notes.color = selectedColor
                notes.imgPath = selectedImagePath
                notes.webLink = webLink

                NotesDataBase.getDataBase(it).noteDao().updateNotes(notes)
                binding.etNoteTitle.setText("")
                binding.etNoteDesc.setText("")
                binding.layoutImage.visibility = View.GONE
                binding.imgNote.visibility = View.GONE
                binding.tvWebLink.visibility = View.GONE
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    private fun saveNote() {

        val etNoteTitle = view?.findViewById<EditText>(R.id.etNoteTitle)
        val etNoteDesc = view?.findViewById<EditText>(R.id.etNoteDesc)

        when {
            etNoteTitle?.text.isNullOrEmpty() -> {
                Snackbar.make(requireView(), "Title is Required", Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.snackbarok)) { null }
                    .show()
            }
            etNoteDesc?.text.isNullOrEmpty() -> {
                Snackbar.make(
                    requireView(),
                    "Notes Description Must Not Be Empty",
                    Snackbar.LENGTH_LONG
                ).setAction(getString(R.string.snackbarok)) { null }
                    .show()
            }
            else -> {
                viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                    val notes = Notes()
                    notes.title = etNoteTitle?.text.toString()
                    notes.noteText = etNoteDesc?.text.toString()
                    notes.dateTime = currentTime
                    notes.color = selectedColor
                    notes.imgPath = selectedImagePath
                    notes.webLink = webLink

                    context?.let {
                        NotesDataBase.getDataBase(it).noteDao().insertNotes(notes)
                        etNoteTitle?.setText("")
                        etNoteDesc?.setText("")
                        binding.layoutImage.visibility = View.GONE
                        binding.imgNote.visibility = View.GONE
                        binding.tvWebLink.visibility = View.GONE
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                }
            }
        }
    }

    private fun deleteNote() {

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            context?.let {
                NotesDataBase.getDataBase(it).noteDao().deleteSpecificNote(noteId)
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    private fun checkWebUrl() {
        if (Patterns.WEB_URL.matcher(binding.etWebLink.text.toString()).matches()) {
            binding.layoutWebUrl.visibility = View.GONE
            binding.etWebLink.isEnabled = false
            webLink = binding.etWebLink.text.toString()
            binding.tvWebLink.visibility = View.VISIBLE
            binding.tvWebLink.text = binding.etWebLink.text.toString()
        } else {
            Toast.makeText(requireContext(), "Url is not Valid", Toast.LENGTH_SHORT).show()
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

            val actionColor = p1!!.getStringExtra("action")

            binding.apply {
                when (actionColor!!) {

                    "Blue" -> {
                        selectedColor = p1.getStringExtra("selectedColor")!!
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                    }

                    "Cyan" -> {
                        selectedColor = p1.getStringExtra("selectedColor")!!
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                    }

                    "Green" -> {
                        selectedColor = p1.getStringExtra("selectedColor")!!
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                    }

                    "Orange" -> {
                        selectedColor = p1.getStringExtra("selectedColor")!!
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                    }

                    "Purple" -> {
                        selectedColor = p1.getStringExtra("selectedColor")!!
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                    }

                    "Red" -> {
                        selectedColor = p1.getStringExtra("selectedColor")!!
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                    }

                    "Yellow" -> {
                        selectedColor = p1.getStringExtra("selectedColor")!!
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                    }

                    "Brown" -> {
                        selectedColor = p1.getStringExtra("selectedColor")!!
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                    }

                    "Indigo" -> {
                        selectedColor = p1.getStringExtra("selectedColor")!!
                        colorView.setBackgroundColor(Color.parseColor(selectedColor))
                    }

                    "Image" -> {
                        readStorageTask()
                        binding.layoutWebUrl.visibility = View.GONE
                    }

                    "WebUrl" -> {
                        binding.layoutWebUrl.visibility = View.VISIBLE
                    }

                    "DeleteNote" -> {
                        deleteNote()
                    }

                    else -> {
                        binding.layoutImage.visibility = View.GONE
                        imgNote.visibility = View.GONE
                        binding.layoutWebUrl.visibility = View.GONE
                        selectedColor = p1.getStringExtra("selectedColor")!!
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
                        binding.imgNote.visibility = View.VISIBLE
                        binding.layoutImage.visibility = View.VISIBLE

                        selectedImagePath = getPathFromUri(selectedImageUrl)!!
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
