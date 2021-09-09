package com.akshatbhuhagal.mynotes

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.akshatbhuhagal.mynotes.adapter.NotesAdapter
import com.akshatbhuhagal.mynotes.database.NotesDataBase
import com.akshatbhuhagal.mynotes.databinding.FragmentHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.launch


class HomeFragment : BaseFragment() {

    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HomeFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)


        launch {
            context?.let {

                var notes = NotesDataBase.getDataBase(it).noteDao().getAllNotes()
                binding.recyclerView.adapter = NotesAdapter(notes)

            }
        }




        // Find View By ID
        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val fabCreateNoteBtn = view.findViewById<FloatingActionButton>(R.id.fabCreateNoteBtn)

        // Bottom Nav Setup
        bottomNavigationView.background = null
        bottomNavigationView.menu.getItem(4).isEnabled = false

        // FAB CREATE NOTE FRAGMENT
        fabCreateNoteBtn.setOnClickListener {
            replaceFragment(CreateNoteFragment.newInstance(),true)
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}