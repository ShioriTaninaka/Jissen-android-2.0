package com.newit.school_guide.core.base

import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.newit.school_guide.R
import com.newit.school_guide.core.api.ErrorCode
import com.newit.school_guide.core.common.*
import com.newit.school_guide.core.extension.replaceFragment
import com.newit.school_guide.feat.ScheduleAdapter
import com.newit.school_guide.feat.model.EventModel
import com.newit.school_guide.feat.model.Schedule
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


open abstract class BaseFragment : Fragment() {

    abstract fun getLayoutId(): Int

    abstract fun viewReadyToUse()


    val CALENDAR_QUERY_COLUMNS = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.NAME,
        CalendarContract.Calendars.VISIBLE,
        CalendarContract.Calendars.ACCOUNT_TYPE,
        CalendarContract.Calendars.OWNER_ACCOUNT,
        CalendarContract.Calendars.IS_PRIMARY,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(getLayoutId(), container, false)
//        return if (this.view != null) this.view else inflater.inflate(
//            getLayoutId(),
//            container,
//            false
//        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewReadyToUse()
    }

    protected fun popBack() {
        if (activity is BaseActivity) {
            (activity as BaseActivity).backToPrev()
        }
    }

    protected fun replaceFrag(fragment: Fragment, rootId: Int, isBackStack: Boolean = false) {
        if (activity is BaseActivity) {
            (activity as BaseActivity).replaceFragment(fragment, rootId, isBackStack)
        }
    }

    fun showLoading() {
        (activity as BaseActivity).showLoading()
    }

    fun hideLoading() {
        (activity as? BaseActivity)?.hideLoading()
    }

    fun showActivity(t: Class<*>?, bundle: Bundle? = null) {
        val intent = Intent(context, t)
        intent.putExtra("extra", bundle)
        startActivity(intent)
    }

    fun handleError(errorCode: Int?, fragment: BaseFragment? = null) {
        if (errorCode == null) {
            Toast.makeText(context, "error", Toast.LENGTH_SHORT).show()
        } else {
            when (errorCode) {
                ErrorCode.ERROR_NETWORK -> DialogUtils.showAlert1Btn(
                    context = context, message = getString(
                        R.string.err_network
                    )
                )
                else -> DialogUtils.showAlert1Btn(
                    context = context,
                    message = getString(R.string.err_undefine)
                )
            }
        }
    }

    fun openWebview(url: String, title: String? = "") {
        val bundle = bundleOf("url" to url, "title" to title)
        findNavController().navigate(R.id.webviewFragment, bundle)
    }

    fun initCalendar(isNeedRefresh: Boolean = false, getSchedule: (calendarId: String) -> Unit) {
        val selection = CalendarContract.Calendars.ACCOUNT_TYPE +
                " =? AND " +
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME +
                " =?"
        val selArgs = arrayOf(
            CalendarContract.ACCOUNT_TYPE_LOCAL, getString(R.string.calendar_name_jissen)
        )
        val selArgs2 = arrayOf(
            CalendarContract.ACCOUNT_TYPE_LOCAL, getString(R.string.calendar_name_job)
        )
        val contentResolver: ContentResolver = requireContext().getContentResolver()
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
                    requireContext(),
                    getString(R.string.calendar_name_jissen),
                    getString(R.string.app_name)
                )
                Utils.createCalendarWithName(
                    requireContext(),
                    getString(R.string.calendar_name_job),
                    getString(R.string.app_name)
                )
                initSchedule(getSchedule)
            } else {
//                for (id in calendarIds) {
//                    val uri =
//                        ContentUris.withAppendedId(
//                            CalendarContract.Calendars.CONTENT_URI,
//                            id.toLong()
//                        )
//                    contentResolver.delete(uri, null, null) // delete calendar
//                }
//                createCalendarWithName(
//                    requireContext(),
//                    getString(R.string.calendar_name_jissen),
//                    getString(R.string.app_name)
//                )
//                createCalendarWithName(
//                    requireContext(),
//                    getString(R.string.calendar_name_job),
//                    getString(R.string.app_name)
//                )
                initSchedule(getSchedule)
            }
        }else{
            if(isNeedRefresh){
                initSchedule(getSchedule)
            }
        }
    }

    fun initSchedule(getSchedule: (calendarId: String) -> Unit){
        val selection = CalendarContract.Calendars.ACCOUNT_TYPE +
                " =? AND " +
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME +
                " =?"
        val selArgs = arrayOf(
            CalendarContract.ACCOUNT_TYPE_LOCAL, getString(R.string.calendar_name_jissen)
        )
        val contentResolver: ContentResolver = requireContext().getContentResolver()
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

    fun initEvent(calID: Long, schedule: Schedule) {
        var timeStart = schedule.calendar
        if(!schedule.stime.isNullOrEmpty()){
            timeStart += " " + schedule.stime
        }else{
            timeStart += " " + "00:00"
        }
        var dateStart = Utils.stringToDate(timeStart)
        var calendarStart = Calendar.getInstance().apply {
            time = dateStart
        }
        var timeEnd = schedule.calendar
        if(!schedule.etime.isNullOrEmpty()){
            timeEnd += " " + schedule.etime
        }else{
            timeEnd += " " + "00:00"
        }
        var dateEnd = Utils.stringToDate(timeEnd)

        var startMillis: Long = Calendar.getInstance().run {
            time = dateStart
            timeInMillis
        }

        var endMillis: Long = Calendar.getInstance().run {
            time = dateEnd
            timeInMillis
        }
        if(schedule.fullday == 1){
            startMillis = Calendar.getInstance().run {
                set(
                    calendarStart.get(Calendar.YEAR),
                    calendarStart.get(Calendar.MONTH),
                    calendarStart.get(Calendar.DAY_OF_MONTH),
                )
                timeInMillis
            }
            endMillis = startMillis + TimeUnit.DAYS.toMillis(1)
        }

        var tz = TimeZone.getDefault()
//        var tz = TimeZone.getTimeZone("GMT")

        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startMillis)
            put(CalendarContract.Events.DTEND, endMillis)
            put(CalendarContract.Events.TITLE, schedule.title)
//            put(CalendarContract.Events.DESCRIPTION, "school")
            put(CalendarContract.Events.CALENDAR_ID, calID)
//            put(CalendarContract.Events.EVENT_COLOR, 0xffff0000)
//            put(CalendarContract.Events.EVENT_COLOR, Color.parseColor("#FF0000")) // not work
            put(CalendarContract.Events.ALL_DAY, schedule.fullday) // event all_day
//            put(CalendarContract.Events.DURATION, "PT1H") //event duration 1h
            put(CalendarContract.Events.EVENT_TIMEZONE, tz.id)
        }

        val uri: Uri? =
            activity?.contentResolver?.insert(CalendarContract.Events.CONTENT_URI, values)

        val eventID: Long = uri?.lastPathSegment?.toLong() ?: 0
        Logger.d("eventID=" + eventID)
    }



    fun getEventJissen() : ArrayList<EventModel>{
        val selection = CalendarContract.Calendars.ACCOUNT_TYPE +
                " =? AND " +
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME +
                " =?"
        val selArgs = arrayOf(
            CalendarContract.ACCOUNT_TYPE_LOCAL, getString(R.string.calendar_name_jissen)
        )
        val contentResolver: ContentResolver = requireContext().getContentResolver()
        val cursor: Cursor? = contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            CALENDAR_QUERY_COLUMNS, selection, selArgs, null
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

        var builder: Uri.Builder =
            Uri.parse("content://com.android.calendar/instances/when").buildUpon()
        val now = Date().time
        ContentUris.appendId(builder, now - DateUtils.DAY_IN_MILLIS * 365 * 4)
        ContentUris.appendId(builder, now + DateUtils.DAY_IN_MILLIS * 365 * 4)

        var data = ArrayList<EventModel>()
        for (id in calendarIds){
            var eventCursor = contentResolver.query(
                builder.build(),
                arrayOf("_id", "title", "begin", "end", "allDay"),
                "calendar_id=" + id,
                null,
                "startDay ASC, startMinute ASC"
            )
            if(eventCursor != null){
                if (eventCursor?.count > 0) {
                    while (eventCursor?.moveToNext() == true) {
                        var id = eventCursor.getString(0)
                        var title = eventCursor.getString(1)
                        var begin = Date(eventCursor.getLong(2))
                        var end = Date(eventCursor.getLong(3))
//                        var allDay = !eventCursor.getString(4).equals("0")
                        var allDay = eventCursor.getInt(4)
                        Logger.d("event = " + "title:$title")
                        if(title == null) {
                            title = ""
                        }
                        data.add(
                            EventModel(
                                id.toLong(),
                                title,
                                ScheduleAdapter.TYPE_EVENT,
                                begin,
                                end,
                                allDay
                            )
                        )
                    }
                }
            }
        }
        return data
    }
}