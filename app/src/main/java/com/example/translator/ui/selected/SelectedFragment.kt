package com.example.translator.ui.selected

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.translator.R
import com.example.translator.data.SortOrder
import com.example.translator.databinding.FragmentSelectedBinding
import com.example.translator.domain.models.Translation
import com.example.translator.ui.util.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectedFragment : Fragment(R.layout.fragment_selected), SelectedTranslationAdapter.OnItemClickListener {

    private lateinit var binding: FragmentSelectedBinding
    private val viewModel: SelectedViewModel by viewModels()
    private lateinit var searchView: SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentSelectedBinding.bind(view)
        val selectedTranslationAdapter = SelectedTranslationAdapter(this)
        binding.apply {
            recyclerSelectedTranslations.apply {
                adapter = selectedTranslationAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                addItemDecoration(
                    DividerItemDecoration(
                        requireContext(),
                        1
                    )
                )
            }

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val translation = selectedTranslationAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.deleteTranslation(translation)
                }
            }).attachToRecyclerView(recyclerSelectedTranslations)
        }

        viewModel.translations.observe(viewLifecycleOwner) {
            selectedTranslationAdapter.submitList(it)
        }

        val menuHost: MenuHost = requireActivity()
            menuHost.addMenuProvider(object: MenuProvider {

                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.selected_menu, menu)

                    val searchItem = menu.findItem(R.id.action_search)
                    searchView = searchItem.actionView as SearchView
                    searchView.queryHint = context!!.resources.getString(R.string.search_query_hint)

                    val pendingQuery = viewModel.searchQuery.value
                    if (!pendingQuery.isNullOrEmpty()) {
                        searchItem.expandActionView()
                        searchView.setQuery(pendingQuery, false)
                    }

                    searchView.onQueryTextChanged {
                        viewModel.searchQuery.value = it
                    }

                    val magImage = searchView.findViewById<View>(androidx.appcompat.R.id.search_mag_icon) as ImageView
                    magImage.layoutParams = LinearLayout.LayoutParams(0, 0)
                }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when(menuItem.itemId)
                {
                    R.id.action_search -> {
                        true
                    }
                    R.id.action_sort_by_name -> {
                        viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                        true
                    }
                    R.id.action_sort_by_date_created -> {
                        viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                        true
                    }
                    R.id.action_selected_clear -> {
                        viewModel.deleteAllSelected()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onItemClick(translation: Translation) {
        return
    }

    override fun onStop() {
        searchView.setOnQueryTextListener(null)
        super.onStop()
    }
}