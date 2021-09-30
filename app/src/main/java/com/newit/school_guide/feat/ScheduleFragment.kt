package com.newit.school_guide.feat

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.newit.school_guide.R
import com.newit.school_guide.core.base.BaseFragment
import com.newit.school_guide.core.common.Logger
import com.newit.school_guide.core.common.Utils
import com.newit.school_guide.databinding.FragmentScheduleBinding
import com.newit.school_guide.feat.model.EventModel
import com.soumaschool.souma.core.api.Status
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.annotations.AfterPermissionGranted
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class ScheduleFragment : BaseFragment(), EasyPermissions.PermissionCallbacks {
    private val viewModel: HomeViewModel by viewModels()
    lateinit var adapter: ScheduleAdapter
    var typeRequestPermission = 1 // 1:read calendar ,2: icRight requestPermission

//    val CALENDAR_QUERY_COLUMNS = arrayOf(
//            CalendarContract.Calendars._ID,
//            CalendarContract.Calendars.NAME,
//            CalendarContract.Calendars.VISIBLE,
//            CalendarContract.Calendars.ACCOUNT_TYPE,
//            CalendarContract.Calendars.OWNER_ACCOUNT,
//            CalendarContract.Calendars.IS_PRIMARY,
//            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
//    )

    var calendarIds = ArrayList<String>()

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    override fun getLayoutId(): Int {
        return R.layout.fragment_schedule
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun viewReadyToUse() {
        binding.toolbar.tvTitle.text = getString(R.string.schedule_title)
        binding.toolbar.icRight.visibility = View.VISIBLE
        binding.toolbar.icRight.setImageResource(R.drawable.ic_add)
        binding.toolbar.icLeft.setImageResource(R.drawable.ic_home)
        binding.toolbar.icLeft.setOnClickListener {
            requireActivity().finish()
        }
        binding.toolbar.icRight.setOnClickListener {
            typeRequestPermission = 2
            requiresCalendarPermission()
        }
        binding.rcvList.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = ScheduleAdapter(ArrayList()) {
            it.timeStart?.let { dateStart ->
                var startMillis = dateStart.time
                if (it.allDay == 0) {
                    startMillis -= TimeUnit.MINUTES.toMillis(30)
                }
                val builder: Uri.Builder = CalendarContract.CONTENT_URI.buildUpon()
                    .appendPath("time")
                ContentUris.appendId(builder, startMillis)
                val intent = Intent(Intent.ACTION_VIEW)
                    .setData(builder.build())
                startActivity(intent)
            }
        }
        binding.rcvList.adapter = adapter

    }

    private fun gotoAddEvent() {
//        val startMillis: Long = Calendar.getInstance().run {
//            set(this.get(Calendar.YEAR), this.get(Calendar.MONTH), this.get(Calendar.DAY_OF_MONTH))
//            timeInMillis
//        }
//        val endMillis: Long = Calendar.getInstance().run {
//            set(this.get(Calendar.YEAR), this.get(Calendar.MONTH), this.get(Calendar.DAY_OF_MONTH))
//            timeInMillis
//        }
        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
//                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
//                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
//                .putExtra(CalendarContract.Events.TITLE, "Yoga")
//                .putExtra(CalendarContract.Events.DESCRIPTION, "Group class")
            .putExtra(CalendarContract.Events.CALENDAR_ID, calendarIds.get(0).toLong())
//            .putExtra(CalendarContract.Events.EVENT_LOCATION, "The gym")
//            .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
//            .putExtra(Intent.EXTRA_EMAIL, "rowan@example.com,trevor@example.com")
        startActivity(intent)
    }

    @AfterPermissionGranted(HomeFragment.REQUEST_CODE_READ_WRITE_CALENDAR_PERMISSION)
    private fun requiresCalendarPermission() {
        if (EasyPermissions.hasPermissions(
                context,
                Manifest.permission.WRITE_CALENDAR,
                Manifest.permission.READ_CALENDAR
            )
        ) {
            // Already have permission, do the thing
            initCalendar() { calendarId ->
                viewModel.schedules.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                    when (it.status) {
                        Status.SUCCESS -> {
                            hideLoading()
                            it.data?.let { data ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    Utils.createEventWithSchedules(
                                        calendarId,
                                        Utils.removeDuplicateData(data.data)
                                    )
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

            if (typeRequestPermission == 1) {
                getEvent()
            } else {
                gotoAddEvent()
                typeRequestPermission = 1
            }
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.permission_read_write_calendar_rationale_message),
                HomeFragment.REQUEST_CODE_READ_WRITE_CALENDAR_PERMISSION,
                Manifest.permission.WRITE_CALENDAR,
                Manifest.permission.READ_CALENDAR
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
//        if (typeRequestPermission == 1) {
//            getEvent()
//        } else {
//            gotoAddEvent()
//        }
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

    private fun getEvent() {
        val selection = CalendarContract.Calendars.ACCOUNT_TYPE +
                " =? AND " +
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME +
                " =?"
        val selArgs = arrayOf(
            CalendarContract.ACCOUNT_TYPE_LOCAL, getString(R.string.calendar_name_job)
        )
        val contentResolver: ContentResolver = requireContext().getContentResolver()
        val cursor: Cursor? = contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            CALENDAR_QUERY_COLUMNS, selection, selArgs, null
        )

        calendarIds.clear()
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
        for (id in calendarIds) {
            var eventCursor = contentResolver.query(
                builder.build(),
                arrayOf("_id", "title", "begin", "end", "allDay"),
                "calendar_id=" + id,
                null,
                "startDay ASC, startMinute ASC"
            )
            println("eventCursor count=" + eventCursor!!.count)

            if (eventCursor.count > 0) {
                while (eventCursor?.moveToNext() == true) {
                    var id = eventCursor.getString(0)
                    var title = eventCursor.getString(1)
                    var begin = Date(eventCursor.getLong(2))
                    var end = Date(eventCursor.getLong(3))
                    var allDay = !eventCursor.getString(4).equals("0")
                    Logger.d("event = " + "title:$title")
                    if (title == null) {
                        title = ""
                    }
                    data.add(EventModel(id.toLong(), title, ScheduleAdapter.TYPE_EVENT, begin, end))
                }
            }

        }

        if (data.size > 0) {
            data.add(
                0,
                EventModel(0, Utils.dateToYYYYDDJp(data[0].timeStart), ScheduleAdapter.TYPE_SESSION)
            )
        }

//        for (i in 0 until data.size) {
//            var item = data[i]
//            if (item.type == ScheduleAdapter.TYPE_EVENT) {
//                var dateItem = Utils.dateToString2(item.timeStart)
//                if (i + 1 < data.size) {
//                    if (dateItem != Utils.dateToString2(data[i + 1].timeStart)) {
//                        data.add(i + 1, EventModel(0,Utils.dateToYYYYDDJp(data[i + 1].timeStart), ScheduleAdapter.TYPE_SESSION))
//                    }
//                }
//            }
//            Logger.d("i:" + i)
//        }

        var i = 0
        while (i < data.size) {
            var item = data[i]
            if (item.type == ScheduleAdapter.TYPE_EVENT) {
                var dateItem = Utils.dateToString2(item.timeStart)
                if (i + 1 < data.size) {
                    if (dateItem != Utils.dateToString2(data[i + 1].timeStart)) {
                        data.add(
                            i + 1,
                            EventModel(
                                0,
                                Utils.dateToYYYYDDJp(data[i + 1].timeStart),
                                ScheduleAdapter.TYPE_SESSION
                            )
                        )
                    }
                }
            }
            i++
            Logger.d("i:" + i)
        }
        adapter.setData(data)
    }

    override fun onResume() {
        super.onResume()
        Logger.d("onResume ScheduleFragment")
        requiresCalendarPermission()
    }

}