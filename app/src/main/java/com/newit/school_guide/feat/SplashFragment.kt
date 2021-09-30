package com.newit.school_guide.feat

import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.newit.school_guide.R
import com.newit.school_guide.core.base.BaseFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : BaseFragment() {
    lateinit var mainViewModel: SplashViewModel
    override fun getLayoutId(): Int {
        return R.layout.fragment_splash
    }

    override fun viewReadyToUse() {
//        TODO("Not yet implemented")
        mainViewModel = ViewModelProvider(requireActivity()).get(SplashViewModel::class.java)
        if(!mainViewModel.isNotify){
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000)
                findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToHomeFragment())
//            findNavController().navigate(R.id.homeFragment)
            }
        }
    }
}