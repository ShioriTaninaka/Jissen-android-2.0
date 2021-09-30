package com.newit.school_guide.feat

import android.Manifest.permission.READ_CALENDAR
import android.Manifest.permission.WRITE_CALENDAR
import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.newit.school_guide.MainActivity
import com.newit.school_guide.R
import com.newit.school_guide.core.base.BaseApplication
import com.newit.school_guide.core.base.BaseFragment
import com.newit.school_guide.core.common.CommonSharedPreferences
import com.newit.school_guide.core.common.Constants
import com.newit.school_guide.core.common.Logger
import com.newit.school_guide.core.common.Utils
import com.newit.school_guide.core.extension.setSafeOnClickListener
import com.newit.school_guide.databinding.FragmentHomeBinding
import com.newit.school_guide.feat.book.BookshelfViewModel
import com.newit.school_guide.feat.model.MessageEvent
import com.soumaschool.souma.core.api.Status
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.annotations.AfterPermissionGranted
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import com.yarolegovich.discretescrollview.DSVOrientation
import com.yarolegovich.discretescrollview.DiscreteScrollView.OnItemChangedListener
import com.yarolegovich.discretescrollview.InfiniteScrollAdapter
import com.yarolegovich.discretescrollview.transform.ScaleTransformer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.intentFor
import org.json.JSONObject
import org.readium.r2.shared.extensions.putPublication
import org.readium.r2.shared.publication.Locator
import kotlin.collections.ArrayList


class HomeFragment : BaseFragment(), View.OnClickListener,
    OnItemChangedListener<SlideInfoAdapter.ViewHolder>, Runnable, View.OnTouchListener,
    EasyPermissions.PermissionCallbacks{
    lateinit var infiniteAdapter: InfiniteScrollAdapter<*>
    lateinit var slideInfoAdapter: SlideInfoAdapter

    private var handler = Handler(Looper.getMainLooper())

    var realCurrentIndex = 0
    var timeDelay = 3000L // 3s

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

//    val CALENDAR_QUERY_COLUMNS = arrayOf(
//        CalendarContract.Calendars._ID,
//        CalendarContract.Calendars.NAME,
//        CalendarContract.Calendars.VISIBLE,
//        CalendarContract.Calendars.ACCOUNT_TYPE,
//        CalendarContract.Calendars.OWNER_ACCOUNT,
//        CalendarContract.Calendars.IS_PRIMARY,
//        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
//    )

    companion object {
        public const val REQUEST_CODE_READ_WRITE_CALENDAR_PERMISSION = 2
    }

    private val viewModel: HomeViewModel by viewModels()
    private val activityViewModel: SplashViewModel by activityViewModels()

    private val bookshelfViewModel: BookshelfViewModel by activityViewModels()

    override fun getLayoutId(): Int {
        return R.layout.fragment_home
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun viewReadyToUse() {
        viewModel.checkAndPostToken()
        binding.btnInfo.setSafeOnClickListener {
            openBookAndGotoMain(0)
        }
        binding.btnSchedule.setSafeOnClickListener {
            openBookAndGotoMain(1)
        }
        binding.btnBook.setSafeOnClickListener {
            openBookAndGotoMain(2)
        }
        binding.imvSetting.setSafeOnClickListener {
            openBookAndGotoMain(3)
        }

        binding.itemPicker.setOnClickListener(this)
        binding.itemPicker.setOrientation(DSVOrientation.HORIZONTAL)
        binding.itemPicker.addOnItemChangedListener(this)
        slideInfoAdapter = SlideInfoAdapter(ArrayList())
        slideInfoAdapter.setiClickDescription {
//            openWebview(it.url, it.title)
            val bundle = bundleOf("url" to it.url,"title_infomation" to it.title,"id_infomation" to it.id,"unread" to 1) // have not unread => 1,always call
            findNavController().navigate(R.id.webviewFragment, bundle)
        }
        infiniteAdapter = InfiniteScrollAdapter.wrap(slideInfoAdapter)
        binding.itemPicker.setAdapter(infiniteAdapter)
        binding.itemPicker.setItemTransitionTimeMillis(200)
        binding.itemPicker.setOnTouchListener(this)
        binding.itemPicker.setItemTransformer(
            ScaleTransformer.Builder()
                .setMinScale(1f)
                .build()
        )
        if(!BaseApplication.getInstance()?.pushId.isNullOrEmpty()){
            startActivity(
                intentFor<MainActivity>(
                    Constants.INDEX_TAB_SELECTED to 0,
                    Constants.BADGES to viewModel.badges,
                    Constants.PUSH_ID to BaseApplication.getInstance()?.pushId
                )
            )
        }else{
            requiresCalendarPermission()
        }

//        if (viewModel.topInfos.value == null) {
//            viewModel.getTopInfos()
//        }

        viewModel.topInfos.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    hideLoading()
                    it.data?.let { data ->
                        slideInfoAdapter.setData(data)
                        if (data.size > 1) {
                            startAutoCycle()
                        }
                    }
                }
                Status.LOADING -> {
                    showLoading()
                }
                Status.ERROR -> {
                    //Handle Error
                    hideLoading()
                    handleError(it.errorCode)
                }
            }
        })
    }

    fun startAutoCycle() {
        //clean previous callbacks
        handler.removeCallbacks(this)

        //Run the loop for the first time
        handler.postDelayed(this, timeDelay)
    }

    fun stopAutoCycle() {
        //clean callback
        handler.removeCallbacks(this)
    }

    override fun onClick(v: View?) {
//        when (v?.id) {
//            R.id.btnInfo -> {
//                openBookAndGotoMain(0)
//            }
//            R.id.btnSchedule -> {
//                openBookAndGotoMain(1)
//            }
//            R.id.btnBook -> {
//                openBookAndGotoMain(2)
//            }
//            R.id.imvSetting -> {
//                openBookAndGotoMain(3)
//            }
//        }
    }

    private fun openBookAndGotoMain(tabIndex : Int){
        bookshelfViewModel.openBook(bookshelfViewModel.getBookHref(),requireContext(),callback = { asset, mediaType, publication, remoteAsset, url ->
            var intent = Intent(requireContext(),MainActivity::class.java)
            intent.apply {
                putPublication(publication)
                putExtra("bookId", bookshelfViewModel.getBookId())
                putExtra("publicationPath", asset.file.path)
                putExtra("publicationFileName", asset.file.name)
                putExtra("deleteOnResult", false)
                putExtra("baseUrl", url.toString())
                val strLocator =  CommonSharedPreferences.getInstance().getString("saveProgression")
                if(strLocator.isNotEmpty()){
                    putExtra("locator",  strLocator.let { Locator.fromJSON(
                        JSONObject(it)
                    ) })
                }

                putExtra(Constants.INDEX_TAB_SELECTED,tabIndex)
                putExtra(Constants.BADGES,viewModel.badges)
            }
            startActivity(intent)
        })
    }

    override fun onCurrentItemChanged(
        viewHolder: SlideInfoAdapter.ViewHolder?,
        adapterPosition: Int
    ) {
        realCurrentIndex = infiniteAdapter.getRealPosition(adapterPosition)
    }

    override fun run() {
        try {
            slideToNextPosition()
        } finally {
            // continue the loop
            handler.postDelayed(this, timeDelay)
        }
    }

    override fun onPause() {
        super.onPause()
        stopAutoCycle()
    }

    override fun onResume() {
        super.onResume()
//        startAutoCycle()
        viewModel.getTopInfos()
    }

    fun slideToNextPosition() {
        if (slideInfoAdapter.itemCount > 0) {
            var adapter = binding.itemPicker.adapter
            realCurrentIndex++
            var destination =
                (adapter as InfiniteScrollAdapter<*>).getClosestPosition(realCurrentIndex % slideInfoAdapter.itemCount)
            binding.itemPicker.smoothScrollToPosition(destination)
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_MOVE) {
            stopAutoCycle()
        } else if (event?.action == MotionEvent.ACTION_UP) {
            handler.postDelayed(Runnable { startAutoCycle() }, 500)
        }
        return false
    }

    @AfterPermissionGranted(REQUEST_CODE_READ_WRITE_CALENDAR_PERMISSION)
    private fun requiresCalendarPermission() {
        if (EasyPermissions.hasPermissions(context, WRITE_CALENDAR, READ_CALENDAR)) {
            // Already have permission, do the thing
            initCalendar(true){calendarId ->
                viewModel.schedules.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                    when (it.status) {
                        Status.SUCCESS -> {
                            hideLoading()
                            it.data?.let { data ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    if(data.dataDelete.isNotEmpty()){
                                        Utils.deleteAllEvent()
                                    }
                                    Utils.createEventWithSchedules(calendarId,Utils.removeDuplicateData(data.data))
                                }
                            }
                        }
                        Status.LOADING -> {
                            showLoading()
                        }
                        Status.ERROR -> {
                            //Handle Error
                            hideLoading()
                            handleError(it.errorCode)
                        }
                    }
                })
                viewModel.getSchedules()
            }
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.permission_read_write_calendar_rationale_message),
                REQUEST_CODE_READ_WRITE_CALENDAR_PERMISSION,
                WRITE_CALENDAR,
                READ_CALENDAR
            )
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Logger.d("PermissionsDenied")
        if (EasyPermissions.somePermissionPermanentlyDenied(
                this,
                *perms.map { it }.toTypedArray()
            )
        ) {
            SettingsDialog.Builder(requireContext()).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent?) {
        viewModel.resetData()
        viewModel.getInfomations()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

}