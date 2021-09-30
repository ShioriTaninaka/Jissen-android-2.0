package com.newit.school_guide

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.*
import android.view.MenuItem
import android.view.View
import android.view.ViewConfiguration
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.newit.school_guide.core.base.BaseActivity
import com.newit.school_guide.core.base.BaseNavFragment
import com.newit.school_guide.core.common.Constants
import com.newit.school_guide.core.extension.removeBadgeCustom
import com.newit.school_guide.core.extension.showBadge
import com.newit.school_guide.databinding.ActivityMainBinding
import com.newit.school_guide.feat.book.reader.ReaderContract
import com.newit.school_guide.feat.book.reader.ReaderViewModel
import kotlinx.coroutines.Dispatchers
import org.readium.r2.shared.publication.Publication
import java.util.*
import kotlin.coroutines.CoroutineContext


class MainActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemReselectedListener, ViewPager.OnPageChangeListener,
    BaseNavFragment.IDestinationChangeListener {

     val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

     lateinit var preferences: SharedPreferences
     lateinit var publicationPath: String
     lateinit var publicationFileName: String
     lateinit var publication: Publication
     lateinit var publicationIdentifier: String
     var bookId: Long = -1
    lateinit var baseUrl: String

     var allowToggleActionBar = true

    private lateinit var readViewModel: ReaderViewModel
    private lateinit var modelFactory: ReaderViewModel.Factory

    private lateinit var binding: ActivityMainBinding

    // overall back stack of containers
    private val backStack = Stack<Int>()

    // list of base destination containers
    private var fragments = listOf(
        BaseNavFragment.newInstance(
            R.layout.content_infomation_base,
            R.id.nav_host_information,
            this
        ),
        BaseNavFragment.newInstance(R.layout.content_schedule_base, R.id.nav_host_schedule, this),
        BaseNavFragment.newInstance(R.layout.content_handbook_base, R.id.nav_host_handbook, this),
        BaseNavFragment.newInstance(R.layout.content_setting_base, R.id.nav_host_setting, this)
    )

    // map of navigation_id to container index
    private var indexToPage = mapOf(
        0 to R.id.tab_information,
        1 to R.id.tab_my_schedule,
        2 to R.id.tab_handbook, 3 to R.id.tab_setting
    )

    var currentIndex = 0
    var badges = 0
    var btNavigationView : BottomNavigationView?=null

    var mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            /* Toast.makeText(context, "Message is: "+ intent.getStringExtra("message"), Toast.LENGTH_LONG)
                .show();*/
            val action = intent.action
            when (action) {
                "msg" -> {
//                    displayBadge()
                }
            }
        }
    }

    override fun setupView() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return modelFactory
    }

    override fun initial() {
//        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        preferences = getSharedPreferences("org.readium.r2.settings", Context.MODE_PRIVATE)

        val inputData = ReaderContract.parseIntent(this)
        baseUrl = inputData.baseUrl.toString()
        modelFactory = ReaderViewModel.Factory(applicationContext, inputData)

        ViewModelProvider(this).get(ReaderViewModel::class.java).let { model ->
            model.channel.receive(this) {
//                handleReaderFragmentEvent(it)
            }
            readViewModel = model
        }

        // setup main view pager
        binding.mainPager.addOnPageChangeListener(this)
        binding.mainPager.adapter = ViewPagerAdapter()
//        binding.mainPager.post(this::checkDeepLink)
        binding.mainPager.offscreenPageLimit = fragments.size

        // set bottom nav
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this)
        binding.bottomNavigation.setOnNavigationItemReselectedListener(this)
        btNavigationView = binding.bottomNavigation

        // initialize backStack with elements
        if (backStack.empty()) backStack.push(0)

        currentIndex = intent.getIntExtra(Constants.INDEX_TAB_SELECTED, 0)
        badges = intent.getIntExtra(Constants.BADGES, 0)
        binding.mainPager.currentItem = currentIndex

        val filter = IntentFilter("msg")
        registerReceiver(mBroadcastReceiver, filter)

    }

    fun displayBadge(){
        var countBadge = getCountBadge()
        if(countBadge > 0){
            binding.bottomNavigation.showBadge(countBadge.toString(), R.id.tab_information)
        }else{
            removeBadge()
        }
    }

    fun displayBadge(count : Int?){
        if(count == null){
            removeBadge()
            return
        }
        if(count > 0){
            binding.bottomNavigation.showBadge(count.toString(), R.id.tab_information)
        }else{
            removeBadge()
        }
    }

    fun removeBadge(){
        btNavigationView?.removeBadgeCustom(R.id.tab_information)
    }

//    fun setUpNavigation(){
//        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
//        NavigationUI.setupWithNavController(bottomNavigation, navHostFragment!!.navController)
//        val navController = navHostFragment.navController
//
//        navController.addOnDestinationChangedListener { _, destination, _ ->
//            when(destination.id){
////                R.id.chatFragment, R.id.accountInfoFragment, R.id.changePassFragment, R.id.pointManagerFragment, R.id.pointHistoryFragment, R.id.settingFragment, R.id.webviewFragment -> showBottomTab(
////                    false
////                )
//                else -> showBottomTab(true)
//            }
//        }
//    }

    private fun showBottomTab(isShow: Boolean) {
        if (isShow) {
            binding.bottomNavigation.visibility = View.VISIBLE
        } else {
            binding.bottomNavigation.visibility = View.GONE
        }
    }

    override fun getLayout(): Int {
        return R.layout.activity_main
    }

    /// BottomNavigationView ItemSelected Implementation
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val position = indexToPage.values.indexOf(item.itemId)
        if (binding.mainPager.currentItem != position) setItem(position)
        return true
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        val position = indexToPage.values.indexOf(item.itemId)
        if(position < 0) return
        val fragment = fragments[position]
        fragment.popToRoot()
    }

    override fun onBackPressed() {
        val fragment = fragments[binding.mainPager.currentItem]
        val hadNestedFragments = fragment.onBackPressed()
        // if no fragments were popped
        if (!hadNestedFragments) {
            if (backStack.size > 1) {
                // remove current position from stack
                backStack.pop()
                // set the next item in stack as current
                binding.mainPager.currentItem = backStack.peek()

            } else super.onBackPressed()
        }
    }

    override fun onPageScrollStateChanged(state: Int) {}

    override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

    override fun onPageSelected(page: Int) {
        val itemId = indexToPage[page] ?: R.id.home
        if (binding.bottomNavigation.selectedItemId != itemId) binding.bottomNavigation.selectedItemId = itemId
    }

    private fun setItem(position: Int) {
        binding.mainPager.currentItem = position
        backStack.push(position)
    }

    private fun checkDeepLink() {
        fragments.forEachIndexed { index, fragment ->
            val hasDeepLink = fragment.handleDeepLink(intent)
            if (hasDeepLink) setItem(index)
        }
    }

    @SuppressLint("WrongConstant")
    inner class ViewPagerAdapter : FragmentPagerAdapter(
        supportFragmentManager,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {

        override fun getItem(position: Int): Fragment = fragments[position]

        override fun getCount(): Int = fragments.size

    }

    override fun onDestinationChangeListener(destinationId: Int) {
        when (destinationId) {
            R.id.webviewFragment -> showBottomTab(false)
            else -> showBottomTab(true)
        }
    }


    override fun onResume() {
        super.onResume()
//        displayBadge()
    }

    override fun onDestroy() {
        unregisterReceiver(mBroadcastReceiver)
        super.onDestroy()
    }

    fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    fun getNavigationBarHeight(): Int {
        val hasMenuKey = ViewConfiguration.get(this).hasPermanentMenuKey()
        val resourceId =
            resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0 && !hasMenuKey) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

    fun getBottomNavigationViewHeight(): Int {
        val hasMenuKey = ViewConfiguration.get(this).hasPermanentMenuKey()
        val resourceId =
            resources.getIdentifier("design_bottom_navigation_height", "dimen", packageName)
        return if (resourceId > 0 && !hasMenuKey) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

    fun getCountBadge() : Int{
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var s = notificationManager.activeNotifications
        return s.size
    }

}