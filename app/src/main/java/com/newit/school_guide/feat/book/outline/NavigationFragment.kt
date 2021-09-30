/*
 * Copyright 2021 Readium Foundation. All rights reserved.
 * Use of this source code is governed by the BSD-style license
 * available in the top-level LICENSE file of the project.
 */

package com.newit.school_guide.feat.book.outline

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.newit.school_guide.R
import com.newit.school_guide.databinding.FragmentListviewBinding
import com.newit.school_guide.databinding.ItemRecycleNavigationBinding
import com.newit.school_guide.feat.book.reader.ReaderViewModel
import com.newit.school_guide.feat.book.utils.extensions.outlineTitle
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.indexOfFirstWithHref
import org.readium.r2.shared.publication.toLocator

/*
* Fragment to show navigation links (Table of Contents, Page lists & Landmarks)
*/
class NavigationFragment : Fragment() {

    private lateinit var publication: Publication
    private lateinit var links: List<Link>
    private lateinit var navAdapter: NavigationAdapter

    private var _binding: FragmentListviewBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel : ReaderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ViewModelProvider(requireActivity()).get(ReaderViewModel::class.java).let {
            publication = it.publication
            viewModel = it
        }

        links = requireNotNull(requireArguments().getParcelableArrayList(LINKS_ARG))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navAdapter = NavigationAdapter(currentResourceIndex = viewModel.resourceIndex,publication = publication,onLinkSelected = { link -> onLinkSelected(link) })

        val flatLinks = mutableListOf<Pair<Int, Link>>()

        for (link in links) {
            val children = childrenOf(Pair(0, link))
            // Append parent.
            flatLinks.add(Pair(0, link))
            // Append children, and their children... recursive.
            flatLinks.addAll(children)
        }

        binding.listView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = navAdapter
        }
        navAdapter.submitList(flatLinks)
        if(viewModel.resourceIndex - 5 >= 0){
            binding.listView.scrollToPosition(viewModel.resourceIndex - 5)
        }else{
            binding.listView.scrollToPosition(0)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun onLinkSelected(link: Link) {
        val locator = link.toLocator().let {
            // progression is mandatory in some contexts
            if (it.locations.fragments.isEmpty())
                it.copyWithLocations(progression = 0.0)
            else
                it
        }

        setFragmentResult(
            OutlineContract.REQUEST_KEY,
            OutlineContract.createResult(locator)
        )
    }

    companion object {

        private const val LINKS_ARG = "links"

        fun newInstance(links: List<Link>) =
            NavigationFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(LINKS_ARG, if (links is ArrayList<Link>) links else ArrayList(links))
                }
            }
    }
}

class NavigationAdapter(private val currentResourceIndex :Int?,private val publication: Publication,private val onLinkSelected: (Link) -> Unit) :
        ListAdapter<Pair<Int, Link>, NavigationAdapter.ViewHolder>(NavigationDiff()) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemRecycleNavigationBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(val binding: ItemRecycleNavigationBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Pair<Int, Link>) {
            val resource = publication.readingOrder.indexOfFirstWithHref(item.second.href)!!
            if(resource == currentResourceIndex){
                binding.navigationTextView.setTextColor(ContextCompat.getColor(binding.root.context,R.color.blue))
            }else{
                binding.navigationTextView.setTextColor(ContextCompat.getColor(binding.root.context,R.color.black))
            }
            binding.navigationTextView.text = item.second.outlineTitle
            binding.indentation.layoutParams = LinearLayout.LayoutParams(item.first * 50, ViewGroup.LayoutParams.MATCH_PARENT)
            binding.root.setOnClickListener {
                onLinkSelected(item.second)
            }
        }
    }
}

private class NavigationDiff : DiffUtil.ItemCallback<Pair<Int, Link>>() {

    override fun areItemsTheSame(
            oldItem: Pair<Int, Link>,
            newItem: Pair<Int, Link>
    ): Boolean {
        return oldItem.first == newItem.first
                && oldItem.second == newItem.second
    }

    override fun areContentsTheSame(
            oldItem: Pair<Int, Link>,
            newItem: Pair<Int, Link>
    ): Boolean {
        return oldItem.first == newItem.first
                && oldItem.second == newItem.second
    }
}

fun childrenOf(parent: Pair<Int, Link>): MutableList<Pair<Int, Link>> {
    val indentation = parent.first + 1
    val children = mutableListOf<Pair<Int, Link>>()
    for (link in parent.second.children) {
        children.add(Pair(indentation, link))
        children.addAll(childrenOf(Pair(indentation, link)))
    }
    return children
}
