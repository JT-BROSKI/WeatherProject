package com.jtbroski.myapplication.ui.search

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.jtbroski.myapplication.R
import com.jtbroski.myapplication.databinding.FragmentSearchBinding
import com.jtbroski.myapplication.ui.main.MainActivity

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var viewModel: SearchViewModel
    private val navController: NavController by lazy { findNavController() }

    private lateinit var inputMethodManger: InputMethodManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        binding.lifecycleOwner = this

        viewModel = ViewModelProvider(requireActivity()).get(SearchViewModel::class.java)
        binding.searchViewModel = viewModel

        viewModel.refreshHomeFragment.observe(viewLifecycleOwner, { refreshHomeFragment ->
            if (refreshHomeFragment) {
                inputMethodManger.hideSoftInputFromWindow(view?.windowToken, 0)
                navController.popBackStack()
            }
        })
        viewModel.refreshHomeFragmentFromAdapter.observe(
            viewLifecycleOwner,
            { refreshHomeFragment ->
                if (refreshHomeFragment) {
                    inputMethodManger.hideSoftInputFromWindow(view?.windowToken, 0)
                    navController.popBackStack()
                }
            })

        inputMethodManger =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManger.toggleSoftInput(
            InputMethodManager.SHOW_IMPLICIT,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )

        (requireActivity() as MainActivity).invalidateOptionsMenu()

        return binding.root
    }

    override fun onDestroyView() {
        viewModel.searchFilterAdapter.closeCursor()
        super.onDestroyView()
    }
}