package com.newit.school_guide.feat

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import com.newit.school_guide.R
import com.newit.school_guide.core.base.BaseActivity
import com.newit.school_guide.core.base.BaseApplication
import com.newit.school_guide.core.common.Logger
import com.newit.school_guide.databinding.ActivitySplashBinding
import com.newit.school_guide.feat.book.BookshelfViewModel


class SplashActivity : BaseActivity() {
    private lateinit var navController: NavController
    lateinit var viewModel: SplashViewModel
    private lateinit var bookshelfViewModel: BookshelfViewModel

    lateinit var app: BaseApplication
    var isNotify = false

    private lateinit var binding: ActivitySplashBinding

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                importBooks()
            }
        }

    private var permissionAsked: Boolean = false

    override fun setupView() {
        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    override fun initial() {
        viewModel = ViewModelProvider(this).get(SplashViewModel::class.java)
        bookshelfViewModel = ViewModelProvider(this).get(BookshelfViewModel::class.java)
        BaseApplication.getInstance()?.pushId = intent.getStringExtra("id")
        Logger.d("push_id: " + BaseApplication.getInstance()?.pushId)
        isNotify = intent?.getBooleanExtra("isNotify", false) ?: false
        viewModel.isNotify = isNotify
        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return

        // Set up Action Bar
        navController = host.navController
        app = BaseApplication.getInstance()!!

        importBooks()

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Logger.w("Fetching FCM registration token failed : " + task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            Logger.d("fcm_token :" + token)
        })

        if (isNotify) {
            navController.navigate(SplashFragmentDirections.actionSplashFragmentToHomeFragment())
        }
    }

    override fun getLayout(): Int {
        return R.layout.activity_splash
    }

    fun writeSamplesInstalled() {
        val pref =
            getSharedPreferences("pref", Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean("samplesInstalled", true)
        editor.commit()
    }

    override fun onDestroy() {
        hideLoading()
        super.onDestroy()
    }

    private fun importBooks() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            bookshelfViewModel.copySamplesFromAssetsToStorage()
        } else requestStoragePermission()

    }

    private fun requestStoragePermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//            Snackbar.make(
//                findViewById(R.id.root),
//                R.string.permission_external_new_explanation,
//                Snackbar.LENGTH_LONG
//            )
//                .setAction(R.string.permission_retry) {
//                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                }
//                .show()
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            // FIXME this is an ugly hack for when user has said don't ask again
            if (permissionAsked) {
                Snackbar.make(
                    findViewById(R.id.root),
                    R.string.permission_external_new_explanation,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.action_settings) {
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            addCategory(Intent.CATEGORY_DEFAULT)
                            data = Uri.parse("package:${binding.root.context?.packageName}")
                        }.run(::startActivity)
                    }
                    .setActionTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.snackbar_text_color
                        )
                    )
                    .show()
            } else {
                permissionAsked = true
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }
}