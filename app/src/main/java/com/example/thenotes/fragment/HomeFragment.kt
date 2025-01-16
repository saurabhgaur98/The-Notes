package com.example.thenotes.fragment

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.thenotes.FavoriteNotes
import com.example.thenotes.MainActivity
import com.example.thenotes.R
import com.example.thenotes.adapter.NoteAdapter
import com.example.thenotes.databinding.FragmentHomeBinding
import com.example.thenotes.model.Note
import com.example.thenotes.viewmodel.NoteViewModel


class HomeFragment : Fragment(R.layout.fragment_home), SearchView.OnQueryTextListener, MenuProvider {

    private var homeBinding: FragmentHomeBinding? = null
    private val binding get() = homeBinding!!

    private lateinit var notesViewModel: NoteViewModel
    private lateinit var noteAdapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this,viewLifecycleOwner, Lifecycle.State.RESUMED)

        notesViewModel = (activity as MainActivity).noteViewModel

        binding.addNote.setOnClickListener{
            it.findNavController().navigate(R.id.action_homeFragment_to_addNoteFragment)
        }

        binding.seeFavNotes.setOnClickListener{
            val intent = Intent(requireContext(), FavoriteNotes::class.java)
            startActivity(intent)
        }

        setupHomeRecyclerView()
    }

    private fun updateUI(note: List<Note>){//note != null
        if (true){
            if (note.isNotEmpty()){
                binding.emptyNotesImage.visibility = View.GONE
                binding.homeRecyclerView.visibility = View.VISIBLE
            }else{
                binding.emptyNotesImage.visibility = View.VISIBLE
                binding.homeRecyclerView.visibility = View.GONE
            }
        }
    }

    private fun setupHomeRecyclerView(){
        noteAdapter = NoteAdapter(onFavoriteClick = { note ->
            handleFavoriteClick(note)
        })
        binding.homeRecyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(true)
            adapter = noteAdapter
        }
        activity?.let {
            notesViewModel.getAllNotes().observe(viewLifecycleOwner){ note ->
                noteAdapter.differ.submitList(note as List<Note?>?)
                updateUI(note)
            }
        }
    }

    private fun handleFavoriteClick(note: Note) {
        // Toggle the favorite status
        note.isFavorite = !note.isFavorite
        note.isFavorite = !note.isFavorite

        // Update the note in the database
        notesViewModel.updateNote(note)

        // Reload the list to reflect changes
        notesViewModel.getAllNotes().observe(viewLifecycleOwner) { allNotes ->
            noteAdapter.differ.submitList(allNotes)
        }

        // Show a Toast message
        val message = if (note.isFavorite) "Added to favorites" else "Removed from favorites"
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }



    private fun searchNote(query: String?){
        val searchQuery = "%$query%"
        notesViewModel.searchNotes(searchQuery).observe(this){ list ->
            noteAdapter.differ.submitList(list as List<Note?>?)
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null){
            searchNote(newText)
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        homeBinding = null
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.home_menu, menu)

        val menuSearch = menu.findItem(R.id.searchMenu).actionView as SearchView
        menuSearch.isSubmitButtonEnabled = false
        menuSearch.setOnQueryTextListener(this)

        // Change cursor color programmatically
        try {
            val field = SearchView::class.java.getDeclaredField("mSearchSrcTextView")
            field.isAccessible = true
            val searchEditText = field.get(menuSearch) as EditText

            // Set the cursor color
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.text_cursor_color)
            searchEditText.setTextCursorDrawable(drawable)

        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }
}