package com.newit.school_guide.core.base

import android.annotation.SuppressLint
import android.app.Application
import com.newit.school_guide.BuildConfig
import com.newit.school_guide.BuildConfig.DEBUG
import com.newit.school_guide.core.common.Logger
import com.newit.school_guide.feat.ApplicationLifecycleHandler
import com.newit.school_guide.feat.model.Info
import com.skytree.epub.BookInformation
import com.skytree.epub.SkyKeyManager
import org.readium.r2.streamer.server.Server
import timber.log.Timber
import java.io.IOException
import java.net.ServerSocket
import java.util.*

class BaseApplication :  Application() {
    var type : String?= null
    var pushId :String?=null

    var message = "We are the world."
    var sortType = 0
    val applicationName: String
        get() {
            val stringId = this.applicationInfo.labelRes
            return this.getString(stringId)
        }

    var infoList : ArrayList<Info>? = null
    var countUnread :Int?= 0
    var isReaded = false

    companion object {
        private var applicationInstance: BaseApplication? = null
        fun getInstance(): BaseApplication? =
            applicationInstance

        @SuppressLint("StaticFieldLeak")
        lateinit var server: Server
            private set

        lateinit var R2DIRECTORY: String
            private set

        var isServerStarted = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        applicationInstance = this
        val handler = ApplicationLifecycleHandler()
        registerActivityLifecycleCallbacks(handler)
        registerComponentCallbacks(handler)

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        val s = ServerSocket(if (DEBUG) 8080 else 0)
        s.close()
        server = Server(s.localPort, applicationContext)
        startServer()
        R2DIRECTORY = r2Directory
    }

    private val r2Directory: String
        get() {
//            val properties = Properties()
//            val inputStream = applicationContext.assets.open("configs/config.properties")
//            properties.load(inputStream)
//            val useExternalFileDir =
//                properties.getProperty("useExternalFileDir", "false")!!.toBoolean()
//            return if (useExternalFileDir) {
//                applicationContext.getExternalFilesDir(null)?.path + "/"
//            } else {
//                applicationContext.filesDir?.path + "/"
//            }
            return applicationContext.filesDir?.path + "/"
        }

    private fun startServer() {
        if (!server.isAlive) {
            try {
                server.start()
            } catch (e: IOException) {
                // do nothing
                if (DEBUG) Timber.e(e)
            }
            if (server.isAlive) {
//                // Add your own resources here
//                server.loadCustomResource(assets.open("scripts/test.js"), "test.js")
//                server.loadCustomResource(assets.open("styles/test.css"), "test.css")
//                server.loadCustomFont(assets.open("fonts/test.otf"), applicationContext, "test.otf")

                isServerStarted = true
            }
        }
    }

}