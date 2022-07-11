package com.zjgsu.yzwtodocalendar

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.CalendarView.INVISIBLE
import com.haibin.calendarview.CalendarView.OnCalendarSelectListener
import com.zjgsu.yzwtodocalendar.Canendars.yzwCustomMonthView
import com.zjgsu.yzwtodocalendar.Canendars.yzwTodoMonthView
import com.zjgsu.yzwtodocalendar.databinding.ActivityMainBinding
import com.zjgsu.yzwtodocalendar.todoServices.yzwNormalNotifyService
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.set


class yzwMainActivity : AppCompatActivity() {

    companion object{
        var eventAlarmMap:MutableMap<todoEvent,AlarmManager> = HashMap()
    }

    lateinit var binding: ActivityMainBinding
    var todoEventList = ArrayList<todoEvent>()
    var filterTodoEventList = ArrayList<todoEvent>()
    var todoEventMap:MutableMap<Date,List<todoEvent>> = HashMap()
    var monthViewID = 1

    var selectYear:Int = 0
    var selectMonth:Int = 0
    var selectDay:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectYear = binding.mCalendarView.curYear
        selectMonth = binding.mCalendarView.curMonth
        selectDay = binding.mCalendarView.curDay
        // 日历滚动到下一个月
        binding.monthNext.setOnClickListener {
            binding.mCalendarView.scrollToNext()
        }
        // 日历滚动今日所在的月份
        binding.monthCurrent.setOnClickListener {
            binding.mCalendarView.scrollToCurrent()
        }
        // 日历滚动到上一个月
        binding.monthPre.setOnClickListener {
            binding.mCalendarView.scrollToPre()
        }
        // 添加日程
        binding.fabAddEvent.setOnClickListener {
            val intent = Intent(this, yzwAddEventActivity::class.java)
            startActivity(intent)
        }
        // 添加日程
        binding.buttonGoTodo.setOnClickListener {
            val intent = Intent(this, yzwTodoListActivity::class.java)
            startActivity(intent)
        }
        // 实现日历日程进度条隐藏
        binding.buttonChangeCal.setOnClickListener {
            val customMonthViewID = 0
            val todoMonthViewID = 1
            if (monthViewID == customMonthViewID){
                monthViewID = todoMonthViewID
                binding.mCalendarView.setMonthView(yzwTodoMonthView::class.java)
                binding.buttonChangeCal.setText("隐藏进度")
            }else if (monthViewID == todoMonthViewID){
                monthViewID = customMonthViewID
                binding.mCalendarView.setMonthView(yzwCustomMonthView::class.java)
                binding.buttonChangeCal.setText("显示进度")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        todoEventList.clear()
        todoEventMap.clear()
        filterTodoEventList.clear()
        // 从数据库读取数据到 todoEventMap todoEventList
        initTodoEventList()
        flushCalendar()
        binding.mCalendarView.scrollToCurrent()
        filterTodoEventList = todoEventList.filter {
            it.year == selectYear &&
            it.month == selectMonth-1 &&
            it.day == selectDay
        } as ArrayList<todoEvent>
        val adapter = todoEventAdapter(this, R.layout.list_item, filterTodoEventList)
        if (filterTodoEventList.size == 0) binding.tipInfo.visibility = VISIBLE
        else binding.tipInfo.visibility = INVISIBLE
        binding.listEvent.adapter = adapter
    }

    private fun getSchemeCalendar(
        year: Int,
        month: Int,
        day: Int,
        color: Int,
        text: String
    ): Any {
        val calendar = Calendar()
        calendar.year = year
        calendar.month = month
        calendar.day = day
        calendar.schemeColor = color //如果单独标记颜色、则会使用这个颜色
        calendar.scheme = text
        return calendar
    }

    inner class todoEventAdapter(activity: Activity, val resourceId: Int, data: List<todoEvent>) :
        ArrayAdapter<todoEvent>(activity, resourceId, data) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = LayoutInflater.from(context).inflate(resourceId, parent, false)
            val nameTextView: TextView = view.findViewById(R.id.textName)
            val dateTextView: TextView = view.findViewById(R.id.textDate)
            val iconImageView: ImageView = view.findViewById(R.id.iconIsFinish)
            val arrayItem = getItem(position) // 获取当前项的Fruit实例
            if (arrayItem != null) {
                nameTextView.setText(arrayItem.name)
                dateTextView.setText(arrayItem.getDateAndTime())
                if (arrayItem.isFinished)  iconImageView.setImageResource(R.drawable.finished) else iconImageView.setImageResource(R.drawable.unfinished2)
            }

            iconImageView.setOnClickListener {
                if (arrayItem != null) {
                    arrayItem.isFinished = !arrayItem.isFinished
                    updateEventList(arrayItem)
                    if (arrayItem.isFinished) {
                        iconImageView.setImageResource(R.drawable.finished)
                        flushCalendar()
                    }else {
                        iconImageView.setImageResource(R.drawable.unfinished2)
                        flushCalendar()
                    }
                }else{
                    Toast.makeText(context, "错误，没有选择TODO", Toast.LENGTH_LONG).show()
                }
            }
            return view
        }
    }

    fun updateEventList(arrayItem:todoEvent){
        val dbHelper = todoEventDBHelper(this, "TodoEventDB.db", 1)
        val db = dbHelper.writableDatabase
        val value = ContentValues().apply {
            //组装数据
            put("name", arrayItem.name)
            put("isFinished",arrayItem.isFinished)
            put("year", arrayItem.year)
            put("month", arrayItem.month)
            put("day",arrayItem.day)
            put("cate",arrayItem.cate)
            put("persons",arrayItem.persons)
            put("location",arrayItem.location)
            put("notification",arrayItem.notification)
            put("hourOfDay",arrayItem.hourOfDay)
            put("minute",arrayItem.minute)
        }
        db.update("TodoEventTable",value,"id = ?", arrayOf(arrayItem.id.toString()))
    }

    @SuppressLint("Range")
    fun initTodoEventList(){
        // 查询数据库
        val dbHelper = todoEventDBHelper(this, "TodoEventDB.db", 1)
        val db = dbHelper.writableDatabase
        val cursor = db.query("TodoEventTable", null, null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                // 遍历Cursor对象,取出数据并打印
                val id = cursor.getInt(cursor.getColumnIndex("id"))
                val name = cursor.getString(cursor.getColumnIndex("name"))
                val isFinished = cursor.getInt(cursor.getColumnIndex("isFinished"))
                val year = cursor.getInt(cursor.getColumnIndex("year"))
                val month = cursor.getInt(cursor.getColumnIndex("month"))
                val day = cursor.getInt(cursor.getColumnIndex("day"))
                val cate = cursor.getString(cursor.getColumnIndex("cate"))
                val persons = cursor.getString(cursor.getColumnIndex("persons"))
                val location = cursor.getString(cursor.getColumnIndex("location"))
                val notification = cursor.getString(cursor.getColumnIndex("notification"))
                val hourOfDay = cursor.getInt(cursor.getColumnIndex("hourOfDay"))
                val minute = cursor.getInt(cursor.getColumnIndex("minute"))
                val eventObj = todoEvent(
                    id = id,
                    name = name,
                    isFinished = isFinished != 0,
                    year = year,
                    month = month,
                    day = day,
                    cate = cate,
                    persons = persons,
                    location = location,
                    notification = notification,
                    hourOfDay = hourOfDay,
                    minute = minute,
                )

                // 开始创建alarmManager 实现定时通知
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val testIntent = Intent(this, yzwNormalNotifyService::class.java)
                // 打包通知信息
                val bundle = Bundle()
                bundle.putString("name",eventObj.name)
                bundle.putString("persons",eventObj.persons)
                bundle.putString("location",eventObj.location)
                bundle.putString("notification",eventObj.notification)
                testIntent.putExtras(bundle)
                Log.d("eventObj.notification",eventObj.notification)
                // 获取PendingIntent
                val pi = PendingIntent.getService(this, 0, testIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                // 设置定时提醒
                var timeNow = LocalDateTime.now()
                var timeEvent = LocalDateTime.of(eventObj.year,eventObj.month+1,eventObj.day,eventObj.hourOfDay,eventObj.minute)
                Log.d("timeEvent",timeEvent.toString())
                Log.d("timeNow",timeNow.toString())
                if (timeNow.isBefore(timeEvent)) {
                    val triggerAtMillis = timeEvent.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
                }
                todoEventList.add(eventObj)
                eventAlarmMap.put(eventObj,alarmManager)

                val tmpDate = Date(year,month,day)
                if (todoEventMap.containsKey(tmpDate)){
                    todoEventMap[tmpDate]
                }
                if (todoEventMap.containsKey(tmpDate)){
                    val tmpList = todoEventMap.get(tmpDate)!! as ArrayList<todoEvent>
                    tmpList.add(eventObj)
                    todoEventMap.put(tmpDate, tmpList)
                } else{
                    val tmpList = ArrayList<todoEvent>()
                    tmpList.add(eventObj)
                    todoEventMap.put(tmpDate, tmpList)
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    fun flushList(year: Int,month: Int,day: Int){
        filterTodoEventList.clear()
        filterTodoEventList = todoEventList.filter {
            it.year == year &&
            it.month == month-1 &&
            it.day == day
        } as ArrayList<todoEvent>

        val adapter = todoEventAdapter(this, R.layout.list_item, filterTodoEventList)
        // 若没有日程提示点击右下角新建
        if (filterTodoEventList.size == 0) binding.tipInfo.visibility = VISIBLE
        else binding.tipInfo.visibility = INVISIBLE

        binding.listEvent.adapter = adapter
        binding.listEvent.setOnItemClickListener { parent, view, position, id ->
            val item = filterTodoEventList[position]
            Log.d("testMaintodoEventList",item.toString())
            val intent = Intent(this, yzwEditEventActivity::class.java)
            val bundle = Bundle()
            bundle.putInt("id",item.id)
            bundle.putString("name",item.name)
            bundle.putBoolean("isFinished",item.isFinished)
            bundle.putInt("year",item.year)
            bundle.putInt("month",item.month)
            bundle.putInt("day",item.day)
            bundle.putString("cate",item.cate)
            bundle.putString("persons",item.persons)
            bundle.putString("location",item.location)
            bundle.putString("notification",item.notification)
            bundle.putInt("hourOfDay",item.hourOfDay)
            bundle.putInt("minute",item.minute)
            intent.putExtras(bundle)
            startActivity(intent)
        }
        binding.monthCurrent.text = year.toString() + "/" + month.toString()
    }

    fun flushCalendar(){
        // 根据 todoEventMap 分析任务完成进度
        val todoMap = getSchemeMap()
        // 设置日历
        binding.mCalendarView.setSchemeDate(todoMap)
        // 日历点击时间监听
        binding.mCalendarView.setOnCalendarSelectListener(object : OnCalendarSelectListener {
            override fun onCalendarOutOfRange(calendar: Calendar) {}
            override fun onCalendarSelect(calendar: Calendar, isClick: Boolean) {
                //日历选择监听
                flushList(calendar.year,calendar.month,calendar.day)
                setSelected(calendar.year,calendar.month,calendar.day)
            }
        })
    }
    fun setSelected(year: Int,month: Int,day: Int){
        selectYear = year
        selectMonth = month
        selectDay = day
    }
    fun getSchemeMap(): MutableMap<String, Calendar> {
        val map: MutableMap<String, Calendar> = HashMap()
        for (each in todoEventList){
            val year = each.year
            val month = each.month
            val day = each.day
            val process = getProcess(Date(year,month,day))
            val color = getSuitableColor(process)
            map[getSchemeCalendar(year, month+1, day, color, process.toString()).toString()] =
                getSchemeCalendar(year, month+1, day, color, process.toString()) as Calendar
        }
        return map
    }

    fun getSuitableColor(process: Int):Int{
        return if (process > 80) -0xec5310
        else if  (process > 60) -0x43ec10
        else if  (process > 40) -0x5533bc
        else if  (process > 20) -0xec5310
        else -0x196ec8
    }

    fun getProcess(date:Date):Int{
        val list = todoEventMap[date] as ArrayList<todoEvent>
        val predicate: (todoEvent) -> Boolean = { it.isFinished }
        val finishedNum = list.count(predicate)
        return finishedNum*100/list.size
    }
}


