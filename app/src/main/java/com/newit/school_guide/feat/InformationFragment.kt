package com.newit.school_guide.feat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.newit.school_guide.MainActivity
import com.newit.school_guide.R
import com.newit.school_guide.core.api.Resource
import com.newit.school_guide.core.base.BaseApplication
import com.newit.school_guide.core.base.BaseFragment
import com.newit.school_guide.databinding.FragmentInformationBinding
import com.newit.school_guide.feat.model.MessageEvent
import com.soumaschool.souma.core.api.Status
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.onRefresh


class InformationFragment : BaseFragment() {
    lateinit var adapter: NotifyAdapter

    private val viewModel: InfomationViewModel by viewModels()

    @Volatile
    private var firstLoading = false

    private var _binding: FragmentInformationBinding? = null
    private val binding get() = _binding!!

    override fun getLayoutId(): Int {
        return R.layout.fragment_information
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInformationBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun viewReadyToUse() {
        binding.toolbar.icLeft.setImageResource(R.drawable.ic_home)
        binding.toolbar.icLeft.visibility = View.VISIBLE
        binding.toolbar.icLeft.setOnClickListener {
            requireActivity().finish()
        }
        binding.toolbar.tvTitle.text = getString(R.string.notification_title)

        adapter = NotifyAdapter(ArrayList()) {
            val bundle = bundleOf("url" to it.url,"title_infomation" to it.title,"id_infomation" to it.id,"unread" to it.unread)
            findNavController().navigate(R.id.webviewFragment, bundle)
        }
        binding.rcvList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.rcvList.adapter = adapter
        binding.swipeRefresh.onRefresh {
            viewModel.resetData()
            adapter.notifyDataSetChanged()
            viewModel.getInfomations(false)
        }

        viewModel.infomations.observe(viewLifecycleOwner, Observer {
            binding.swipeRefresh.isRefreshing = false
//            (activity as? MainActivity)?.removeBadge()
//            BaseApplication.getInstance()?.isNeedShowNumberNotify = false
            when (it.status) {
                Status.SUCCESS -> {
                    hideLoading()
                    it.data?.let { data ->
                        adapter.setData(data)
                        if(data.size > 0){
                            binding.rcvList.visibility = View.VISIBLE
                            binding.tvNoData.visibility = View.GONE
                        }else{
                            binding.rcvList.visibility = View.GONE
                            binding.tvNoData.visibility = View.VISIBLE
                        }
                        var info = data.find { info -> info.id.toString().equals(BaseApplication.getInstance()?.pushId) }
                        info?.let {
                            val bundle = bundleOf("url" to it.url,"title_infomation" to it.title,"id_infomation" to it.id,"unread" to it.unread)
                            findNavController().navigate(R.id.webviewFragment, bundle)
                            BaseApplication.getInstance()?.pushId = null
                        }
                    }
                    (activity as MainActivity).displayBadge(BaseApplication.getInstance()?.countUnread)
                }
                Status.LOADING -> {
                    showLoading()
                }
                Status.ERROR -> {
                    hideLoading()
                    handleError(it.errorCode)
                    (activity as MainActivity).removeBadge()
                }
            }
            firstLoading = false
        })
        if(viewModel.infomations.value == null){
            firstLoading = true
            viewModel.getInfomations()
            if(BaseApplication.getInstance()?.infoList != null){
                viewModel.infomations.postValue(Resource.success(BaseApplication.getInstance()?.infoList))
            }else{
                viewModel.resetData()
                viewModel.getInfomations()
            }
        }else{
            if(BaseApplication.getInstance()?.isReaded == true){
                viewModel.resetData()
                viewModel.getInfomations()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent?) {
        viewModel.resetData()
        viewModel.getInfomations()
    }

    override fun onResume() {
        super.onResume()
//        if(!firstLoading){
//            viewModel.resetData()
//            viewModel.getInfomations()
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this);
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

}