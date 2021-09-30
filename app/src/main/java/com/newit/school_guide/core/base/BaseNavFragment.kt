package com.newit.school_guide.core.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.newit.school_guide.core.common.Constants.rootDestinations

class BaseNavFragment : Fragment() {

    private val defaultInt = -1
    private var layoutRes: Int = -1
//    private var toolbarId: Int = -1
    private var navHostId: Int = -1
//    private val appBarConfig = AppBarConfiguration(rootDestinations,null)
    private val appBarConfig = AppBarConfiguration(rootDestinations)
    private var iDestinationChangeListener : IDestinationChangeListener?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            layoutRes = it.getInt(KEY_LAYOUT)
//            toolbarId = it.getInt(KEY_TOOLBAR)
            navHostId = it.getInt(KEY_NAV_HOST)

        } ?: return
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return if (layoutRes == defaultInt) null
        else inflater.inflate(layoutRes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = requireActivity().findNavController(navHostId)
        navController.addOnDestinationChangedListener{_, destination, _ ->
            iDestinationChangeListener?.onDestinationChangeListener(destination.id)
        }
    }

    fun onBackPressed(): Boolean {
        if(activity == null) return true
        return requireActivity()
            .findNavController(navHostId)
            .navigateUp(appBarConfig)
    }


    fun popToRoot() {
        val navController = requireActivity().findNavController(navHostId)
        navController.popBackStack(navController.graph.startDestination, false)
    }

    fun handleDeepLink(intent: Intent) = requireActivity().findNavController(navHostId).handleDeepLink(intent)


    companion object {

        private const val KEY_LAYOUT = "layout_key"
        private const val KEY_TOOLBAR = "toolbar_key"
        private const val KEY_NAV_HOST = "nav_host_key"

        fun newInstance(layoutRes: Int, navHostId: Int,callback : IDestinationChangeListener?=null) = BaseNavFragment().apply {
            arguments = Bundle().apply {
                putInt(KEY_LAYOUT, layoutRes)
//                putInt(KEY_TOOLBAR, toolbarId)
                putInt(KEY_NAV_HOST, navHostId)
            }
            iDestinationChangeListener = callback
        }
    }

    interface IDestinationChangeListener{
        fun onDestinationChangeListener(destinationId: Int)
    }
}