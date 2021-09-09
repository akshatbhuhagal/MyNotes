package com.akshatbhuhagal.mynotes


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
import android.text.Layout
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.akshatbhuhagal.mynotes.database.NotesDataBase
import com.akshatbhuhagal.mynotes.entities.Notes
import com.akshatbhuhagal.mynotes.util.NoteBottomSheetFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_create_note.*
import kotlinx.android.synthetic.main.fragment_notes_bottom_sheet.*
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.android.synthetic.main.fragment_notes_bottom_sheet.layoutWebUrl as layoutWebUrl1


class CreateNoteFragment : BaseFragment(), EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

    var selectedColor = "#3e434e"
    var currentTime: String? = null

    // Permission Private Read & Write
    private var READ_STORAGE_PERM = 123
    private var REQUEST_CODE_IMAGE = 456

    private var webLink = ""
    private var selectedImagePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_note, container, false)
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

        // Register & Unregister broadcast receiver
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            BroadcastReceiver, IntentFilter("bottom_sheet_action")
        )


        // Find View By ID
        val tvDateTime = view.findViewById<TextView>(R.id.tvDateTime)
        val imgDone = view.findViewById<ImageView>(R.id.imgDone)
        val imgBack = view.findViewById<ImageView>(R.id.imgBack)


        colorView.setBackgroundColor(Color.parseColor(selectedColor))


        // Date & Time
        val sdf = SimpleDateFormat("dd-MM-yyyy")
        currentTime = sdf.format(Date())

        tvDateTime.text = currentTime

        // Done
        imgDone.setOnClickListener {
            saveNote()
        }

        // Back Button
        imgBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Show More Button
        imgMore.setOnClickListener {
            var noteBottomSheetFragment = NoteBottomSheetFragment.newInstance()
            noteBottomSheetFragment.show(requireActivity().supportFragmentManager, "Note Bottom Sheet Fragment")
        }

        btnOk.setOnClickListener {
            if (etWebLink.text.toString().trim().isNotEmpty()) {
                checkWebUrl()
            } else {
                Toast.makeText(requireContext(), "Url is Required", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            layoutWebUrl.visibility = View.GONE
        }

        tvWebLink.setOnClickListener {
            var intent = Intent(Intent.ACTION_VIEW, Uri.parse(etWebLink.text.toString()))
            startActivity(intent)
        }

    }

    private fun saveNote(){

        val etNoteTitle = view?.findViewById<EditText>(R.id.etNoteTitle)
        val etNoteDesc = view?.findViewById<EditText>(R.id.etNoteDesc)

        if (etNoteTitle?.text.isNullOrEmpty()){
            Snackbar.make(requireView(), "Title is Required", Snackbar.LENGTH_LONG).setAction(getString(R.string.snackbarok), View.OnClickListener { null })
                .show()
            }

        else if (etNoteDesc?.text.isNullOrEmpty()){
            Snackbar.make(requireView(), "Notes Description Must Not Be Empty", Snackbar.LENGTH_LONG).setAction(getString(R.string.snackbarok), View.OnClickListener { null })
                .show()
        }

        else {

            launch {
                var notes = Notes()
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
                    imgNote.visibility = View.GONE
                    tvWebLink.visibility = View.GONE
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }

    }

    private fun checkWebUrl() {
        if (Patterns.WEB_URL.matcher(etWebLink.text.toString()).matches()) {
            layoutWebUrl.visibility = View.GONE
            etWebLink.isEnabled = false
            webLink = etWebLink.text.toString()
            tvWebLink.visibility = View.VISIBLE
            tvWebLink.text = etWebLink.text.toString()
        } else {
            Toast.makeText(requireContext(), "Url is not Valid", Toast.LENGTH_SHORT).show()
        }
    }


    fun replaceFragment(fragment: Fragment, istransition: Boolean) {

        val fragmentTransition = requireActivity().supportFragmentManager.beginTransaction()

        if (istransition) {
            fragmentTransition.setCustomAnimations(
                android.R.anim.slide_out_right,
                android.R.anim.slide_in_left
            )
        }
        fragmentTransition.replace(R.id.flFragmenet, fragment).addToBackStack(fragment.javaClass.simpleName)
        fragmentTransition.commit()
    }



    private val BroadcastReceiver : BroadcastReceiver = object :  BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {

            var actionColor = p1!!.getStringExtra("action")

            when(actionColor!!){

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
                    layoutWebUrl.visibility = View.GONE
                }

                "WebUrl" -> {
                    layoutWebUrl.visibility = View.VISIBLE
                }

                else -> {
                    imgNote.visibility = View.GONE
                    layoutWebUrl.visibility = View.GONE
                    selectedColor = p1.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))
                }

            }

        }
    }

    private fun hasReadStoragePerm(): Boolean {
        return EasyPermissions.hasPermissions(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }


    private fun readStorageTask() {
        if (hasReadStoragePerm()) {

            pickImageFromGallery()

        }else {
            EasyPermissions.requestPermissions(requireActivity(), getString(R.string.storage_permission_text),
                READ_STORAGE_PERM, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun pickImageFromGallery() {
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(intent, REQUEST_CODE_IMAGE)
        }
    }

    private fun getPathFromUri(contentUri : Uri): String? {
        var filePath : String? = null
        var cursor = requireActivity().contentResolver.query(contentUri, null, null, null, null)
        if (cursor == null) {
            filePath = contentUri.path
        } else {
            cursor.moveToFirst()
            var index = cursor.getColumnIndex("_data")
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

                var selectedImageUrl = data.data

                if (selectedImageUrl != null) {
                    try {

                        var inputStream = requireActivity().contentResolver.openInputStream(selectedImageUrl)
                        var bitmap = BitmapFactory.decodeStream(inputStream)
                        imgNote.setImageBitmap(bitmap)
                        imgNote.visibility = View.VISIBLE

                        selectedImagePath = getPathFromUri(selectedImageUrl)!!

                    }catch (e:Exception) {
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(BroadcastReceiver)
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, requireActivity())

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