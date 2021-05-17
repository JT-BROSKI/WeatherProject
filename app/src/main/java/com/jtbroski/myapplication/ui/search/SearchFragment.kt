package com.jtbroski.myapplication.ui.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.jtbroski.myapplication.R
import com.jtbroski.myapplication.Utils
import com.jtbroski.myapplication.databinding.FragmentSearchBinding


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

        // Toolbar Back Arrow
        binding.btnBackArrow.setOnClickListener {
            inputMethodManger.hideSoftInputFromWindow(view?.windowToken, 0)
            navController.popBackStack()
        }
        binding.btnBackArrow.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundColor(
                    if (Utils.preferenceDbHelper.darkThemeFlag) ContextCompat.getColor(
                        requireActivity(),
                        R.color.black
                    )
                    else ContextCompat.getColor(requireActivity(), R.color.purple_700)
                )
                MotionEvent.ACTION_UP -> v.setBackgroundColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.transparent
                    )
                )
            }
            false
        }

        // Search TextBox
        binding.searchTxtView.requestFocus()
        binding.searchTxtView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.updateFilterCursor(s)
            }

            override fun afterTextChanged(s: Editable) {}
        })
        binding.searchTxtView.setOnEditorActionListener { textView, actionId, _ ->
            viewModel.onEnterPressed(textView.text.toString(), actionId)
            false
        }

        inputMethodManger =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManger.toggleSoftInput(
            InputMethodManager.SHOW_IMPLICIT,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )

        return binding.root
    }

    override fun onDestroyView() {
        viewModel.searchFilterAdapter.closeCursor()
        super.onDestroyView()
    }
}