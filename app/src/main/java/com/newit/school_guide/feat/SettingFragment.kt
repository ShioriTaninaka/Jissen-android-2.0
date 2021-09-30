package com.newit.school_guide.feat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.newit.school_guide.R
import com.newit.school_guide.core.base.BaseFragment
import com.newit.school_guide.core.common.CommonSharedPreferences
import com.newit.school_guide.core.common.Constants
import com.newit.school_guide.databinding.FragmentSettingBinding
import com.soumaschool.souma.core.api.Status


class SettingFragment : BaseFragment() {

    private val viewModel: SettingViewModel by viewModels()

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    override fun getLayoutId(): Int {
        return R.layout.fragment_setting
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun viewReadyToUse() {
        binding.toolbar.tvTitle.text = getString(R.string.setting_title)
        binding.toolbar.icLeft.setImageResource(R.drawable.ic_home)
        binding.toolbar.icLeft.visibility = View.VISIBLE
        binding.toolbar.icLeft.setOnClickListener {
            requireActivity().finish()
        }
        binding.switchNotify.isChecked = CommonSharedPreferences.getInstance().getBoolean(
            Constants.ENABLE_NOTIFICATION,
            true
        )
        viewModel.settings.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    hideLoading()
                    CommonSharedPreferences.getInstance().putBoolean(
                        Constants.ENABLE_NOTIFICATION,
                        !CommonSharedPreferences.getInstance().getBoolean(
                            Constants.ENABLE_NOTIFICATION,
                            true
                        )
                    )
                }
                Status.LOADING -> {
                    showLoading()
                }
                Status.ERROR -> {
                    hideLoading()
                    handleError(it.errorCode)
                }
            }
        })
        binding.switchNotify.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                viewModel.notifySetting(1)
            } else {
                viewModel.notifySetting(2)
            }
        })

        binding.llPolicy.setOnClickListener {
            val bundle = bundleOf("isPolicy" to true, "title" to getString(R.string.setting_policy))
            findNavController().navigate(R.id.webviewFragment, bundle)
        }
        binding.llContactUs.setOnClickListener {
//            openWebview(
//                "https://www.jissen.ac.jp/contact/index.html",
//                getString(R.string.setting_contact_us)
//            )
            val url = "https://www.jissen.ac.jp/contact/index.html"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
    }
}