package com.newit.school_guide.feat

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.ContentResolver
import android.content.Context
import android.content.res.Configuration
import android.database.Cursor
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import androidx.navigation.fragment.findNavController
import com.newit.school_guide.R
import com.newit.school_guide.core.api.Resource
import com.newit.school_guide.core.api.network
import com.newit.school_guide.core.base.BaseApplication
import com.newit.school_guide.core.common.CommonSharedPreferences
import com.newit.school_guide.core.common.Constants
import com.newit.school_guide.core.common.Utils
import com.newit.school_guide.feat.model.MessageEvent
import com.vmadalin.easypermissions.EasyPermissions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class ApplicationLifecycleHandler : Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {
    val CALENDAR_QUERY_COLUMNS = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.NAME,
        CalendarContract.Calendars.VISIBLE,
        CalendarContract.Calendars.ACCOUNT_TYPE,
        CalendarContract.Calendars.OWNER_ACCOUNT,
        CalendarContract.Calendars.IS_PRIMARY,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
    )

    private val TAG = ApplicationLifecycleHandler::class.java.simpleName
    private var isInBackground = false
    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
    }

    override fun onActivityStarted(p0: Activity) {
    }

    override fun onActivityResumed(p0: Activity) {
        if(isInBackground){
            Log.d(TAG, "app went to foreground");
            isInBackground = false;
            refreshEvent()
            EventBus.getDefault().post(MessageEvent())
        }
    }

    override fun onActivityPaused(p0: Activity) {
    }

    override fun onActivityStopped(p0: Activity) {
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityDestroyed(p0: Activity) {
    }

    override fun onConfigurationChanged(p0: Configuration) {
    }

    override fun onLowMemory() {
    }

    override fun onTrimMemory(i: Int) {
        if(i == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN){
            Log.d(TAG, "app went to background");
            isInBackground = true
        }
    }

    @Synchronized fun refreshEvent(){
        if(EasyPermissions.hasPermissions(BaseApplication.getInstance(),
                Manifest.permission.WRITE_CALENDAR,
                Manifest.permission.READ_CALENDAR
            )){
            initCalendar(true) {calendarId ->
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val resPost = network().getSchedule()
                        if (resPost.statusCode == 1) {
                            CoroutineScope(Dispatchers.IO).launch {
                                if(resPost.dataDelete.isNotEmpty()){
                                    Utils.deleteAllEvent()
                                }
                                Utils.createEventWithSchedules(calendarId,Utils.removeDuplicateData(resPost.data))
                            }

//                            Utils.deleteEventWithSchedules(calendarId,resPost.dataDelete)
                        }
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }

    fun initCalendar(isNeedRefresh: Boolean = false, getSchedule: (calendarId: String) -> Unit) {
        var context = BaseApplication.getInstance()
        if(context == null) return
        val selection = CalendarContract.Calendars.ACCOUNT_TYPE +
                " =? AND " +
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME +
                " =?"
        val selArgs = arrayOf(
            CalendarContract.ACCOUNT_TYPE_LOCAL, context.getString(R.string.calendar_name_jissen)
        )
        val selArgs2 = arrayOf(
            CalendarContract.ACCOUNT_TYPE_LOCAL, context.getString(R.string.calendar_name_job)
        )
        val contentResolver: ContentResolver = context.getContentResolver()
        val cursor: Cursor? = contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            CALENDAR_QUERY_COLUMNS,
            selection,
            selArgs,
            null
        )
        val cursor2: Cursor? = contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            CALENDAR_QUERY_COLUMNS,
            selection,
            selArgs2,
            null
        )
        var calendarIds = ArrayList<String>()
        while (cursor?.moveToNext() == true) {
            var _id = cursor.getString(0)
            var name = cursor.getString(1)
            var selected = !cursor.getString(2).equals("0")
            var type = cursor.getString(3)
            var accountName = cursor.getString(4)
            var isPrimary = cursor.getString(5)
            var displayName = cursor.getString(6)
            Log.d(
                "initCalendar",
                "Found calendar " + displayName + "   id = " + _id + "  type = " + type + " isPrimary" + isPrimary
            )
            calendarIds.add(_id)
        }
        while (cursor2?.moveToNext() == true) {
            var _id = cursor2.getString(0)
            var name = cursor2.getString(1)
            var selected = !cursor2.getString(2).equals("0")
            var type = cursor2.getString(3)
            var accountName = cursor2.getString(4)
            var isPrimary = cursor2.getString(5)
            var displayName = cursor2.getString(6)
            Log.d(
                "initCalendar",
                "Found calendar " + displayName + "   id = " + _id + "  type = " + type + " isPrimary" + isPrimary
            )
            calendarIds.add(_id)
        }

        if (!CommonSharedPreferences.getInstance().getBoolean(Constants.INIT_SCHEDULE)) {
            if (calendarIds.size == 0) {
                Utils.createCalendarWithName(
                    context,
                    context.getString(R.string.calendar_name_jissen),
                    context.getString(R.string.app_name)
                )
                Utils.createCalendarWithName(
                    context,
                    context.getString(R.string.calendar_name_job),
                    context.getString(R.string.app_name)
                )
                initSchedule(context,getSchedule)
            } else {
                initSchedule(context,getSchedule)
            }
        }else{
            if(isNeedRefresh){
                initSchedule(context,getSchedule)
            }
        }
    }

    fun initSchedule(context: Context,getSchedule: (calendarId: String) -> Unit){
        val selection = CalendarContract.Calendars.ACCOUNT_TYPE +
                " =? AND " +
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME +
                " =?"
        val selArgs = arrayOf(
            CalendarContract.ACCOUNT_TYPE_LOCAL, context.getString(R.string.calendar_name_jissen)
        )
        val contentResolver: ContentResolver = context.getContentResolver()
        val cursor: Cursor? = contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            CALENDAR_QUERY_COLUMNS,
            selection,
            selArgs,
            null
        )
        if (cursor?.moveToNext() == true) {
            var _id = cursor.getString(0)
            getSchedule(_id)
        }
    }
}
