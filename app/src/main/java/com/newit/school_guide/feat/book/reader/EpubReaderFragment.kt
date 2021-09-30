/*
 * Copyright 2021 Readium Foundation. All rights reserved.
 * Use of this source code is governed by the BSD-style license
 * available in the top-level LICENSE file of the project.
 */

package com.newit.school_guide.feat.book.reader

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.newit.school_guide.MainActivity
import com.newit.school_guide.R
import com.newit.school_guide.core.common.Logger
import com.newit.school_guide.feat.book.outline.OutlineContract
import com.newit.school_guide.feat.book.outline.OutlineFragment
import com.newit.school_guide.feat.book.search.SearchFragment
import com.newit.school_guide.feat.book.tts.ScreenReaderContract
import com.newit.school_guide.feat.book.tts.ScreenReaderFragment
import org.jetbrains.anko.backgroundColor
import org.readium.r2.navigator.Navigator
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.shared.APPEARANCE_REF
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.indexOfFirstWithHref
import java.net.URL

class EpubReaderFragment : VisualReaderFragment(), EpubNavigatorFragment.Listener {

    override lateinit var model: ReaderViewModel
    override lateinit var navigator: Navigator
    private lateinit var publication: Publication
    lateinit var navigatorFragment: EpubNavigatorFragment

    private lateinit var menuScreenReader: MenuItem
    private lateinit var menuSearch: MenuItem
    lateinit var menuSearchView: SearchView

    private var isScreenReaderVisible = false
    private var isSearchViewIconified = true
    private var isShowMenu = false

    private val activity: MainActivity
        get() = requireActivity() as MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            isScreenReaderVisible = savedInstanceState.getBoolean(IS_SCREEN_READER_VISIBLE_KEY)
            isSearchViewIconified = savedInstanceState.getBoolean(IS_SEARCH_VIEW_ICONIFIED)
        }

        ViewModelProvider(requireActivity()).get(ReaderViewModel::class.java).let {
            model = it
            publication = it.publication
        }

        val baseUrl = checkNotNull(activity.baseUrl)

        childFragmentManager.fragmentFactory =
            EpubNavigatorFragment.createFactory(publication, baseUrl, model.initialLocation, this)

        childFragmentManager.setFragmentResultListener(
            SearchFragment::class.java.name,
            this,
            FragmentResultListener { _, result ->
//                menuSearch.collapseActionView()

                isSearchViewIconified = true
                childFragmentManager.popBackStack()
//                binding.icLeft.setImageResource(R.drawable.ic_home)
//                binding.groupNormal.visibility = View.VISIBLE
//                binding.edtSearch.clearFocus()
//                binding.edtSearch.visibility = View.GONE

                resetToolbarFromSearch()

                result.getParcelable<Locator>(SearchFragment::class.java.name)?.let {
                    navigatorFragment.go(it)
                    model.checkBookmarkPage(it)
                }
            }
        )

        childFragmentManager.setFragmentResultListener(
            ScreenReaderContract.REQUEST_KEY,
            this,
            FragmentResultListener { _, result ->
                val locator = ScreenReaderContract.parseResult(result).locator
                if (locator.href != navigator.currentLocator.value.href) {
                    navigator.go(locator)
                    model.checkBookmarkPage(locator)
                }
            }
        )

        childFragmentManager.setFragmentResultListener(
            OutlineContract.REQUEST_KEY,
            this,
            FragmentResultListener { _, result ->
                val locator = OutlineContract.parseResult(result).destination
                closeOutlineFragment(locator)
            }
        )

        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        val navigatorFragmentTag = getString(R.string.epub_navigator_tag)

        if (savedInstanceState == null) {
            childFragmentManager.commitNow {
                add(
                    R.id.fragment_reader_container,
                    EpubNavigatorFragment::class.java,
                    Bundle(),
                    navigatorFragmentTag
                )
            }
        }
        navigator = childFragmentManager.findFragmentByTag(navigatorFragmentTag) as Navigator
        navigatorFragment = navigator as EpubNavigatorFragment
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // This is a hack to draw the right background color on top and bottom blank spaces
        navigatorFragment.lifecycleScope.launchWhenStarted {
            val appearancePref = activity.preferences.getInt(APPEARANCE_REF, 0)
            val backgroundsColors = mutableListOf("#ffffff", "#faf4e8", "#000000")
            navigatorFragment.resourcePager.setBackgroundColor(Color.parseColor(backgroundsColors[appearancePref]))
        }
        binding.tvTitle.text = getString(R.string.tab_handbook)

        binding.icSearch.setOnClickListener {
            if (isSearchViewIconified) { // It is not a state restoration.
                showSearchFragment()
                isSearchViewIconified = false

                binding.edtSearch.visibility = View.VISIBLE
                binding.icClearSearch.visibility = View.GONE
                binding.groupNormal.visibility = View.GONE
                binding.edtSearch.requestFocus()
                binding.tvRight.text = getString(R.string.cancel)
                binding.tvRight.visibility = View.VISIBLE
                binding.icLeft.visibility = View.GONE
                binding.tvRight.backgroundColor = ContextCompat.getColor(requireContext(),R.color.transparent)


                (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(
                    InputMethodManager.SHOW_FORCED,
                    InputMethodManager.HIDE_IMPLICIT_ONLY
                )
            }
        }
        binding.icMenu.setOnClickListener {
            showOutlineFragment()
        }

        binding.edtSearch.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                binding.icClearSearch.isVisible = s.toString().isNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
            }
        })

        binding.edtSearch.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                model.search(textView.text.toString().trim())
                binding.edtSearch.clearFocus()
                (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(
                    InputMethodManager.HIDE_IMPLICIT_ONLY,
                    0
                )
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        binding.icClearSearch.setOnClickListener {
            model.search("")
            binding.edtSearch.setText("")
        }

        binding.tvRight.setOnClickListener {
            if (isShowMenu) {
                isShowMenu = false
                childFragmentManager.popBackStack()
                resetToolbarFromOutline()
                model.checkBookmarkPage(navigator.currentLocator.value)
                return@setOnClickListener
            }
            if (!isSearchViewIconified) {
                model.cancelSearch()
                binding.edtSearch.setText("")

                isSearchViewIconified = true
                childFragmentManager.popBackStack()
                resetToolbarFromSearch()
                return@setOnClickListener
            }
        }

        binding.icLeft.setOnClickListener {
//            if (isShowMenu) {
//                isShowMenu = false
//                childFragmentManager.popBackStack()
//                resetToolbarFromOutline()
//                return@setOnClickListener
//            }
//            if (!isSearchViewIconified) {
//                model.cancelSearch()
//                binding.edtSearch.setText("")
//
//                isSearchViewIconified = true
//                childFragmentManager.popBackStack()
//                binding.edtSearch.visibility = View.GONE
//                binding.groupNormal.visibility = View.VISIBLE
//                binding.icLeft.setImageResource(R.drawable.ic_home)
//                return@setOnClickListener
//            }
            requireActivity().finish()
        }
        model.checkBookmarkPage(navigator.currentLocator.value)
        binding.icBookmark.setOnClickListener {
            model.changeBookmark(navigator.currentLocator.value)
        }

        navigator.currentLocator.asLiveData().observe(viewLifecycleOwner, Observer { locator ->
            locator ?: return@Observer
            model.checkBookmarkPage(locator)
        })

        model.checkBookmark.observe(viewLifecycleOwner, Observer { isBookmarked ->
            if (isBookmarked) {
                binding.icBookmark.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.red
                    ), android.graphics.PorterDuff.Mode.MULTIPLY
                )
                Logger.d("abc")
            } else {
                binding.icBookmark.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    ), android.graphics.PorterDuff.Mode.MULTIPLY
                )
                Logger.d("abc")
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)
        menuInflater.inflate(R.menu.menu_epub, menu)

        menuScreenReader = menu.findItem(R.id.screen_reader)
        menuSearch = menu.findItem(R.id.search)
        menuSearchView = menuSearch.actionView as SearchView

        connectSearch()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_SCREEN_READER_VISIBLE_KEY, isScreenReaderVisible)
        outState.putBoolean(IS_SEARCH_VIEW_ICONIFIED, isSearchViewIconified)
    }

    private fun connectSearch() {
        menuSearch.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {

            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                if (isSearchViewIconified) { // It is not a state restoration.
                    showSearchFragment()
                }

                isSearchViewIconified = false
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                isSearchViewIconified = true
                childFragmentManager.popBackStack()
                menuSearchView.clearFocus()

                return true
            }
        })

        menuSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                model.search(query)
                menuSearchView.clearFocus()

                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                return false
            }
        })

        menuSearchView.findViewById<ImageView>(R.id.search_close_btn).setOnClickListener {
            menuSearchView.requestFocus()
            model.cancelSearch()
            menuSearchView.setQuery("", false)

            (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (super.onOptionsItemSelected(item)) {
            return true
        }

        return when (item.itemId) {
            R.id.settings -> {
//               activity.userSettings.userSettingsPopUp().showAsDropDown(
//                   requireActivity().findViewById(R.id.settings),
//                   0,
//                   0,
//                   Gravity.END
//               )
                // TODO: 05/07/2021  activity.userSettings.userSettingsPopUp()
                true
            }
            R.id.search -> {
                super.onOptionsItemSelected(item)
            }

            android.R.id.home -> {
                menuSearch.collapseActionView()
                true
            }

            R.id.screen_reader -> {
                if (isScreenReaderVisible) {
                    closeScreenReaderFragment()
                } else {
                    showScreenReaderFragment()
                }
                true
            }
            else -> false
        }
    }

    override fun onTap(point: PointF): Boolean {
//        requireActivity().toggleSystemUi()

        // TODO: 05/07/2021 : toggleSystemUI
        return true
    }

//    override fun onPageChanged(pageIndex: Int, totalPages: Int, locator: Locator) {
//        model.checkBookmarkPage(locator)
//        Logger.d("onPageChanged")
//    }

    private fun showSearchFragment() {
        childFragmentManager.commit {
            childFragmentManager.findFragmentByTag(SEARCH_FRAGMENT_TAG)?.let { remove(it) }
            add(
                R.id.fragment_reader_container,
                SearchFragment::class.java,
                Bundle(),
                SEARCH_FRAGMENT_TAG
            )
            hide(navigatorFragment)
            addToBackStack(SEARCH_FRAGMENT_TAG)
        }
    }

    private fun showScreenReaderFragment() {
//        menuScreenReader.title = resources.getString(R.string.epubactivity_read_aloud_stop)
        isScreenReaderVisible = true
        val arguments = ScreenReaderContract.createArguments(navigator.currentLocator.value)
        childFragmentManager.commit {
            add(R.id.fragment_reader_container, ScreenReaderFragment::class.java, arguments)
            hide(navigatorFragment)
            addToBackStack(null)
        }
    }

    private fun closeScreenReaderFragment() {
//        menuScreenReader.title = resources.getString(R.string.epubactivity_read_aloud_start)
        isScreenReaderVisible = false
        childFragmentManager.popBackStack()
    }

    companion object {

        private const val BASE_URL_ARG = "baseUrl"
        private const val BOOK_ID_ARG = "bookId"

        private const val SEARCH_FRAGMENT_TAG = "search"

        private const val IS_SCREEN_READER_VISIBLE_KEY = "isScreenReaderVisible"

        private const val IS_SEARCH_VIEW_ICONIFIED = "isSearchViewIconified"

        fun newInstance(baseUrl: URL, bookId: Long): EpubReaderFragment {
            return EpubReaderFragment().apply {
                arguments = Bundle().apply {
                    putString(BASE_URL_ARG, baseUrl.toString())
                    putLong(BOOK_ID_ARG, bookId)
                }
            }
        }
    }

    private fun handleReaderFragmentEvent(event: ReaderViewModel.Event) {
        when (event) {
            is ReaderViewModel.Event.OpenOutlineRequested -> showOutlineFragment()
        }
    }

    private fun showOutlineFragment() {
        isShowMenu = true
        model.resourceIndex = publication.readingOrder.indexOfFirstWithHref(navigator.currentLocator.value.href)!!
        childFragmentManager.commit {
            add(R.id.fragment_reader_container, OutlineFragment::class.java, Bundle(), "outline")
            hide(navigatorFragment)
            addToBackStack(null)
        }
        setupToolbarForOutlineFragment()
    }

    private fun closeOutlineFragment(locator: Locator) {
        this.go(locator, true)
        childFragmentManager.popBackStack()
        isShowMenu = false
        resetToolbarFromOutline()
        model.checkBookmarkPage(locator)
    }

    private fun setupToolbarForOutlineFragment() {
        binding.icMenu.visibility = View.GONE
        binding.icSearch.visibility = View.GONE
        binding.icBookmark.visibility = View.GONE
        binding.icLeft.visibility = View.INVISIBLE
        binding.tvRight.visibility = View.VISIBLE
        binding.tvTitle.gravity = Gravity.START
    }

    private fun resetToolbarFromOutline() {
        binding.icMenu.visibility = View.VISIBLE
        binding.icSearch.visibility = View.VISIBLE
        binding.icBookmark.visibility = View.VISIBLE
        binding.icLeft.setImageResource(R.drawable.ic_home)
        binding.icLeft.visibility = View.VISIBLE
        binding.tvRight.visibility = View.GONE
        binding.tvTitle.gravity = Gravity.CENTER
    }

    private fun resetToolbarFromSearch(){
        binding.edtSearch.visibility = View.GONE
        binding.icClearSearch.visibility = View.GONE
        binding.groupNormal.visibility = View.VISIBLE
        binding.icLeft.visibility = View.VISIBLE
        binding.tvRight.visibility = View.GONE
        binding.tvRight.text = getString(R.string.resume)
        binding.tvRight.backgroundColor = ContextCompat.getColor(requireContext(),R.color.gray)
    }
}