package com.zjgsu.yzwtodocalendar

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.zjgsu.yzwtodocalendar.databinding.ActivityTodoListBinding
import com.zjgsu.yzwtodocalendar.todoServices.yzwNormalNotifyService
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class yzwTodoListActivity : AppCompatActivity() {
    var todoEventList = ArrayList<todoEvent>()
    var titleList = ArrayList<String>()
    var dataMap = HashMap<String, List<todoEvent>>()
    // 前往编辑页面的Intent
    lateinit var goEdit:Intent

    private lateinit var binding: ActivityTodoListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding  = ActivityTodoListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fabAddEvent.setOnClickListener {
            val intent = Intent(this, yzwAddEventActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        todoEventList.clear()
        titleList.clear()
        dataMap.clear()
        goEdit = Intent(this, yzwEditEventActivity::class.java)

        initTodoEventList()
        val adapter = expTodoEventAdapter(this, titleList, dataMap)
        binding.expListEvent.setAdapter(adapter)
    }



    inner class expTodoEventAdapter (
        private val context: Context,
        private val titleList: List<String>,    // 分组标题
        private val dataList: HashMap<String, List<todoEvent>>  // 分组下对应的item
    ) : BaseExpandableListAdapter() {
        override fun getChild(listPosition: Int, expandedListPosition: Int): Any {
            return this.dataList[this.titleList[listPosition]]!![expandedListPosition]
        }
        override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
            return expandedListPosition.toLong()
        }
        override fun getChildView(
            listPosition: Int,
            expandedListPosition: Int,
            isLastChild: Boolean,
            convertView: View?,
            parent: ViewGroup
        ): View {
            var convertView = convertView
            val eventItem = getChild(listPosition, expandedListPosition) as todoEvent
            // 设置item的点击事件
            convertView?.setOnClickListener{
                val bundle = Bundle()
                bundle.putInt("id",eventItem.id)
                bundle.putString("name",eventItem.name)
                bundle.putBoolean("isFinished",eventItem.isFinished)
                bundle.putInt("year",eventItem.year)
                bundle.putInt("month",eventItem.month)
                bundle.putInt("day",eventItem.day)
                bundle.putString("cate",eventItem.cate)
                bundle.putString("persons",eventItem.persons)
                bundle.putString("location",eventItem.location)
                bundle.putString("notification",eventItem.notification)
                bundle.putInt("hourOfDay",eventItem.hourOfDay)
                bundle.putInt("minute",eventItem.minute)
                Log.d("ListActivityminute", eventItem.minute.toString())
                goEdit.putExtras(bundle)
                startActivity(goEdit)
            }
            if (convertView == null) {
                val layoutInflater =
                    this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                convertView = layoutInflater.inflate(R.layout.item_elv_child, null)
            }
            val nameTextview  = convertView!!.findViewById<TextView>(R.id.textName)
            val dateTextview = convertView.findViewById<TextView>(R.id.textDate)
            val iconImageView = convertView.findViewById<ImageView>(R.id.iconIsFinish)
            if (eventItem.isFinished) iconImageView.setImageResource(R.drawable.finished) else iconImageView.setImageResource(R.drawable.unfinished2)
            iconImageView.setOnClickListener {
                if (eventItem != null) {
                    eventItem.isFinished = !eventItem.isFinished
                    updateEventList(eventItem)
                    if (eventItem.isFinished) {
                        iconImageView.setImageResource(R.drawable.finished)
                    }else {
                        iconImageView.setImageResource(R.drawable.unfinished2)
                    }
                }else{
                    Toast.makeText(context, "错误，没有选择TODO", Toast.LENGTH_LONG).show()
                }
            }
            nameTextview.text = eventItem.name
            dateTextview.text = eventItem.getDate()
            return convertView
        }
        override fun getChildrenCount(listPosition: Int): Int {
            return this.dataList[this.titleList[listPosition]]!!.size
        }
        override fun getGroup(listPosition: Int): Any {
            return this.titleList[listPosition]
        }
        override fun getGroupCount(): Int {
            return this.titleList.size
        }
        override fun getGroupId(listPosition: Int): Long {
            return listPosition.toLong()
        }
        override fun getGroupView(
            listPosition: Int,
            isExpanded: Boolean,
            convertView: View?,
            parent: ViewGroup
        ): View {
            var convertView = convertView
            val listTitle = getGroup(listPosition) as String
            if (convertView == null) {
                val layoutInflater =
                    this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                convertView = layoutInflater.inflate(R.layout.item_elv_group, null)
            }
            val listTitleTextView = convertView!!.findViewById<TextView>(R.id.tv_groupName)

            listTitleTextView.setTypeface(null, Typeface.BOLD)
            listTitleTextView.text = listTitle
            return convertView
        }
        override fun hasStableIds(): Boolean {
            return false
        }
        override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
            return true
        }
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

                // 获取PendingIntent
                val pi = PendingIntent.getService(this, 0, testIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                // 设置定时提醒
                var timeNow = LocalDateTime.now()
                var timeEvent = LocalDateTime.of(eventObj.year,eventObj.month+1,eventObj.day,eventObj.hourOfDay,eventObj.minute)
                if (timeNow.isBefore(timeEvent)) {
                    val triggerAtMillis = timeEvent.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
                }

                todoEventList.add(eventObj)
                yzwMainActivity.eventAlarmMap.put(eventObj,alarmManager)
                if (titleList.indexOf(eventObj.cate) == -1) titleList.add(eventObj.cate)
                if (dataMap.containsKey(eventObj.cate)){
                    val tmp = dataMap.get(eventObj.cate)!! as ArrayList<todoEvent>
                    tmp.add(eventObj)
                    dataMap.put(eventObj.cate,tmp)
                } else{
                    val tmp = ArrayList<todoEvent>()
                    tmp.add(eventObj)
                    dataMap.put(eventObj.cate,tmp)
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
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
}