/*
 * Copyright 2021 Readium Foundation. All rights reserved.
 * Use of this source code is governed by the BSD-style license
 * available in the top-level LICENSE file of the project.
 */

package com.newit.school_guide.feat.book.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.newit.school_guide.R
import com.newit.school_guide.databinding.FragmentSearchBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.readium.r2.shared.publication.Locator
import com.newit.school_guide.feat.book.reader.ReaderViewModel
import org.readium.r2.shared.Search

class SearchFragment : Fragment(R.layout.fragment_search) {

    private val viewModel: ReaderViewModel by activityViewModels()

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewScope = viewLifecycleOwner.lifecycleScope

        val adapter =  SearchResultAdapter(object : SearchResultAdapter.Listener {
            override fun onItemClicked(v: View, locator: Locator) {
                val result = Bundle().apply {
                    putParcelable(SearchFragment::class.java.name, locator)
                }
                setFragmentResult(SearchFragment::class.java.name, result)
            }
        })

        viewModel.searchResult
            .onEach {
                adapter.submitData(it)
            }
            .launchIn(viewScope)

        binding.searchListView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(activity)
        }
        adapter.addLoadStateListener {
            if(viewModel.currentTextSearch.isNotEmpty()){
                binding.tvNodata.isVisible = adapter.itemCount == 0
            }else{
                binding.tvNodata.isVisible = false
            }
        }
//        binding.tvNodata.isVisible = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
