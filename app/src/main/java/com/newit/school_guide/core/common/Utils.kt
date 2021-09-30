package com.newit.school_guide.core.common

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.format.DateUtils
import android.util.DisplayMetrics
import android.util.Log
import androidx.core.content.ContextCompat
import com.newit.school_guide.R
import com.newit.school_guide.core.api.ErrorCode
import com.newit.school_guide.core.base.BaseApplication
import com.newit.school_guide.feat.ScheduleAdapter
import com.newit.school_guide.feat.model.EventModel
import com.newit.school_guide.feat.model.Schedule
import retrofit2.HttpException
import java.io.*
import java.net.URISyntaxException
import java.net.UnknownHostException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


object Utils {

    val CALENDAR_QUERY_COLUMNS = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.NAME,
        CalendarContract.Calendars.VISIBLE,
        CalendarContract.Calendars.ACCOUNT_TYPE,
        CalendarContract.Calendars.OWNER_ACCOUNT,
        CalendarContract.Calendars.IS_PRIMARY,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
    )

    fun getErrorCode(e : Exception): Int {
        if(e is UnknownHostException){
            return ErrorCode.ERROR_NETWORK
        }
        if(e is HttpException){
            return e.code()
        }
        return ErrorCode.ERROR_UNDEFINE
    }

    fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * (context.getResources()
            .getDisplayMetrics().densityDpi  / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun convertPixelsToDp(px: Float, context: Context): Float {
        return px / (context.getResources()
            .getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun getTemporaryCameraFile(): File {
        val storageDir = File(getAppExternalDataDirectoryFile(), "Camera")
        storageDir.mkdirs()
        val file = File(storageDir, "CAMERA_" + System.currentTimeMillis() + ".jpg")
        try {
            file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file
    }

    fun getAppExternalDataDirectoryFile(): File {
        val dataDirectoryFile = File(getAppExternalDataDirectoryPath())
        dataDirectoryFile.mkdirs()

        return dataDirectoryFile
    }

    fun getAppExternalDataDirectoryPath(): String {
        val sb = StringBuilder()
        sb.append(Environment.getExternalStorageDirectory())
            .append(File.separator)
            .append("Android")
            .append(File.separator)
            .append("data")
            .append(File.separator)
            .append(BaseApplication.getInstance()?.getPackageName())
            .append(File.separator)

        return sb.toString()
    }

    fun getLastUsedCameraFile(): File? {
        val dataDir = File(getAppExternalDataDirectoryFile(), "Camera")
        val files = dataDir.listFiles()
        val filteredFiles = ArrayList<File>()
        for (file in files) {
            if (file.name.startsWith("CAMERA_")) {
                filteredFiles.add(file)
            }
        }

        Collections.sort(filteredFiles)
        return if (!filteredFiles.isEmpty()) {
            filteredFiles[filteredFiles.size - 1]
        } else {
            null
        }
    }

    fun rotate(filePath: String, uri: Uri, context: Context) {
        var cameraBitmap: Bitmap? = null
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = false
        bmOptions.inPurgeable = true
        bmOptions.inBitmap = cameraBitmap
        bmOptions.inMutable = true

        cameraBitmap = BitmapFactory.decodeFile(filePath, bmOptions)
        // Your image file path
        val bos = ByteArrayOutputStream()
        cameraBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, bos)


        var exif: ExifInterface? = null
        try {
            if (Build.VERSION.SDK_INT > 23)
                exif = ExifInterface(inputStream!!)
            else
                exif = ExifInterface(filePath)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val rotation = exif!!.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL).toFloat()
        println(rotation)

        val rotationInDegrees = exifToDegrees(rotation)
        println(rotationInDegrees)

        val matrix = Matrix()
        matrix.postRotate(rotationInDegrees)

        val rotatedBitmap = Bitmap.createBitmap(cameraBitmap, 0, 0, cameraBitmap.width, cameraBitmap.height, matrix, true)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(filePath)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        try {
            fos!!.write(bos.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }

        cameraBitmap.recycle()
        System.gc()
        try {
            fos!!.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        try {
            fos!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun exifToDegrees(exifOrientation: Float): Float {
        val deviceModel = Build.MODEL
        val reqString = (Build.MANUFACTURER
                + " " + Build.MODEL + " " + Build.VERSION.RELEASE
                + " " + Build.VERSION_CODES::class.java.fields[Build.VERSION.SDK_INT].name)
        return if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90.toFloat()) {
            90f
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180.toFloat()) {
            180f
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270.toFloat()) {
            270f
        } else {
            if (reqString.contains("Sony")) {
                90f
            } else
                0f
        }

    }

    @SuppressLint("NewApi")
    @Throws(URISyntaxException::class)
    fun getPath(context: Context, uri: Uri): String? {
        var uri = uri
        val needToCheckUri = Build.VERSION.SDK_INT >= 19
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(context.applicationContext, uri)) {
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                } else {
                    val files = ContextCompat.getExternalFilesDirs(context, null)
                    for (file in files) {
                        if (file.absolutePath.contains(type)) {
                            return file.parentFile.parentFile.parentFile.parentFile.toString() + "/" + split[1]
                        }
                    }
                }
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                uri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                if ("image" == type) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                selection = "_id=?"
                selectionArgs = arrayOf(split[1])
            }
        }
        if ("content".equals(uri.scheme!!, ignoreCase = true)) {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            var cursor: Cursor? = null
            try {
                cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
                assert(cursor != null)
                val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index)
                }
            } catch (e: Exception) {
            }

        } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    fun stringToTime(stringTime : String): Long{
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'").parse(stringTime)
        return date.time
    }
    fun convertStringToTime(stringTime : String): String{
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'").parse(stringTime)
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        return dateFormat.format(date)
    }

    fun stringToDate(stringTime : String): Date{
        val date = SimpleDateFormat("yyyy/MM/dd HH:mm").parse(stringTime)
        return date
    }

    fun stringToTime2(stringTime : String): Long{
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(stringTime)
        return date.time
    }

    fun convertStringToTime2(stringTime : String?): String?{
        if(stringTime == null) return null
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'").parse(stringTime)
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
        return dateFormat.format(date)
    }

    fun convertStringToTime3(stringTime : String?): String?{
        if(stringTime == null) return null
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(stringTime)
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
        return dateFormat.format(date)
    }

    fun dateToString(date : Date?):String{
        if(date == null)return ""
        val fomat = SimpleDateFormat("yyyy-MM-dd HH:mm")
        return fomat.format(date)
    }

    fun strDateToStringJP(s : String):String{
        var date = SimpleDateFormat("yyyy/MM/dd HH:mm").parse(s)
        return dateToStringDateJP2(date)
    }

    fun dateToString3(date : Date?):String{
        if(date == null)return ""
        val fomat = SimpleDateFormat("yyyy/MM/dd")
        return fomat.format(date)
    }

    fun dateToStringDate(date : Date?):String{
        if(date == null)return ""
        val fomat = SimpleDateFormat("yyyy-MM-dd")
        return fomat.format(date)
    }

    fun dateToStringDateJP(date : Date?):String{
        if(date == null)return ""
        val fomat = SimpleDateFormat("yyyy年MM月dd日")
        return fomat.format(date)
    }

    fun dateToStringDateJP2(date : Date?):String{
        if(date == null)return ""
        val fomat = SimpleDateFormat("yyyy年MM月dd日 HH:mm")
        return fomat.format(date)
    }

    fun dateToStringHour(date : Date?):String{
        if(date == null)return ""
        val fomat = SimpleDateFormat("HH:mm")
        return fomat.format(date)
    }

    fun dateToString2(date : Date?):String{
        if(date == null)return ""
        val fomat = SimpleDateFormat("yyyy-MM")
        return fomat.format(date)
    }

    fun dateToYYYYDDJp(date : Date?):String{
        if(date == null)return ""
        val fomat = SimpleDateFormat("yyyy年MM月")
        return fomat.format(date)
    }

    fun getRootDirPath(context: Context): String? {
        return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            val file = ContextCompat.getExternalFilesDirs(
                context.applicationContext,
                null
            )[0]
            file.absolutePath
        } else {
            context.applicationContext.filesDir.absolutePath
        }
    }

    fun createCalendarWithName(ctx: Context, name: String?, accountName: String): Uri? {
        var target = Uri.parse(CalendarContract.Calendars.CONTENT_URI.toString())
        target =
            target.buildUpon().appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
                .appendQueryParameter(
                    CalendarContract.Calendars.ACCOUNT_TYPE,
                    CalendarContract.ACCOUNT_TYPE_LOCAL
                )
                .build()
        val values = ContentValues()
        values.put(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
        values.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
        values.put(CalendarContract.Calendars.NAME, name)
        values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, name)
        if(name.equals(ctx.getString(R.string.calendar_name_job))){
            values.put(CalendarContract.Calendars.CALENDAR_COLOR, 0xf5e342)
        }else{
            values.put(CalendarContract.Calendars.CALENDAR_COLOR, 0x00FF00)
        }
        values.put(
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
            CalendarContract.Calendars.CAL_ACCESS_OWNER
        )
        values.put(CalendarContract.Calendars.OWNER_ACCOUNT, accountName)
        values.put(CalendarContract.Calendars.VISIBLE, 1)
        values.put(CalendarContract.Calendars.SYNC_EVENTS, 1)
        values.put(
            CalendarContract.Calendars.CALENDAR_TIME_ZONE,
            Calendar.getInstance().timeZone.id
        )
        values.put(CalendarContract.Calendars.CAN_PARTIALLY_UPDATE, 1)
//        values.put(
//            Calendars.CAL_SYNC1,
//            "https://www.google.com/calendar/feeds/$accountName/private/full"
//        )
//        values.put(
//            Calendars.CAL_SYNC2,
//            "https://www.google.com/calendar/feeds/default/allcalendars/full/$accountName"
//        )
//        values.put(
//            Calendars.CAL_SYNC3,
//            "https://www.google.com/calendar/feeds/default/allcalendars/full/$accountName"
//        )
//        values.put(Calendars.CAL_SYNC4, 1)
//        values.put(Calendars.CAL_SYNC5, 0)
//        values.put(Calendars.CAL_SYNC8, System.currentTimeMillis())
        return ctx.getContentResolver().insert(target, values)
    }

    suspend fun createEventWithSchedules(calendarId: String, data: ArrayList<Schedule>){
        var eventExist = getEventJissen()
        for (schedule in data) {
            var isExist = false
            for (event in eventExist){
                if(event.title.equals(schedule.title)){
                    if(event.allDay == 1 && schedule.fullday == 1){
                        if(Utils.dateToString3(event.timeStart).equals(schedule.calendar)){
                            isExist = true
                            break
                        }
                    }else if(event.allDay == 0 && schedule.fullday == 0){
                        if(Utils.dateToString3(event.timeStart).equals(schedule.calendar)
                            && Utils.dateToStringHour(event.timeStart).equals(schedule.stime)
                            && Utils.dateToStringHour(event.timeEnd).equals(schedule.etime)){
                            isExist = true
                            break
                        }
                    }
                }
            }
            if(!isExist){
                initEvent(calendarId.toLong(), schedule)
            }
        }
        CommonSharedPreferences.getInstance()
            .putBoolean(Constants.INIT_SCHEDULE, true)
    }

    fun removeDuplicateData(data: ArrayList<Schedule>) : ArrayList<Schedule>{
        var result = ArrayList<Schedule>()
        for (item in data){
            var isExist = false
            for (event in result){
                if(event.title.equals(item.title) && event.fullday == item.fullday
                    && event.stime == item.stime && event.etime == item.etime
                    && event.calendar == item.calendar){
                    isExist = true
                    break
                }
            }
            if(!isExist){
                result.add(item)
            }
        }
        return result
    }

    fun getEventJissen() : ArrayList<EventModel>{
        val selection = CalendarContract.Calendars.ACCOUNT_TYPE +
                " =? AND " +
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME +
                " =?"
        val selArgs = arrayOf(
            CalendarContract.ACCOUNT_TYPE_LOCAL, BaseApplication.getInstance()?.getString(R.string.calendar_name_jissen)
        )
        val contentResolver: ContentResolver = BaseApplication.getInstance()!!.getContentResolver()
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

        val uri: Uri? = BaseApplication.getInstance()?.contentResolver?.insert(CalendarContract.Events.CONTENT_URI, values)

        val eventID: Long = uri?.lastPathSegment?.toLong() ?: 0
        Logger.d("eventID=" + eventID)
    }

    fun deleteEventWithSchedules(calendarId: String, data: ArrayList<Schedule>?){
        if(data == null) return
        if(data.isEmpty()) return
        var eventExist = getEventJissen()
        for (schedule in data) {
            var isExist = false
            for (event in eventExist){
                if(event.title.equals(schedule.title)){
                    if(event.allDay == 1 && schedule.fullday == 1){
                        if(Utils.dateToString3(event.timeStart).equals(schedule.calendar)){
                            isExist = true
                            var deleteUri = ContentUris.withAppendedId(
                                CalendarContract.Events.CONTENT_URI, event.id
                            )
//                            requireContext().getContentResolver().delete(deleteUri, null, null)
                            BaseApplication.getInstance()?.getContentResolver()?.delete(
                                Uri.parse("content://com.android.calendar/events"),
                                "title=? and dtstart=? and dtend=? and allDay=?",
                                arrayOf(event.title,event.timeStart?.time.toString(),event.timeEnd?.time.toString(),event.allDay.toString())
                            )
                            break
                        }
                    }else if(event.allDay == 0 && schedule.fullday == 0){
                        if(Utils.dateToString3(event.timeStart).equals(schedule.calendar)
                            && Utils.dateToStringHour(event.timeStart).equals(schedule.stime)
                            && Utils.dateToStringHour(event.timeEnd).equals(schedule.etime)){
                            isExist = true
                            var deleteUri = ContentUris.withAppendedId(
                                CalendarContract.Events.CONTENT_URI, event.id
                            )
//                            requireContext().getContentResolver().delete(deleteUri, null, null)
                            ContactsContract.CommonDataKinds.Event.START_DATE
                            BaseApplication.getInstance()?.getContentResolver()?.delete(
                                Uri.parse("content://com.android.calendar/events"),
                                "title=? and dtstart=? and dtend=? and allDay=?",
                                arrayOf(event.title,event.timeStart?.time.toString(),event.timeEnd?.time.toString(),event.allDay.toString())
                            )
                            break
                        }
                    }
                }
            }
        }
    }

    fun deleteAllEvent(){
        var eventExist = getEventJissen()
        for (event in eventExist){
            var deleteUri = ContentUris.withAppendedId(
                CalendarContract.Events.CONTENT_URI, event.id
            )
//            val rows = requireContext().contentResolver.delete(deleteUri, null, null)

            val rows2 = BaseApplication.getInstance()?.getContentResolver()?.delete(
                Uri.parse("content://com.android.calendar/events"),
                "title=?",
                arrayOf(event.title)
            )
//            Log.i("deleteAllEvent", "Rows deleted: $rows2"  )
        }
    }

    fun deleteJissenCalendar(){
        val selection = CalendarContract.Calendars.ACCOUNT_TYPE +
                " =? AND " +
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME +
                " =?"
        val selArgs = arrayOf(
            CalendarContract.ACCOUNT_TYPE_LOCAL, BaseApplication.getInstance()?.getString(R.string.calendar_name_jissen)
        )
        val contentResolver: ContentResolver = BaseApplication.getInstance()!!.getContentResolver()
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
            calendarIds.add(_id)
        }
        for (calendarId in calendarIds){
            val uri = ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, calendarId.toLong())
            contentResolver.delete(uri, null, null) // delete calendar
        }
    }

}