package com.zjgsu.yzwtodocalendar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.Toast
import com.zjgsu.yzwtodocalendar.databinding.ActivityEditEventBinding

class yzwEditEventActivity : AppCompatActivity() {
    private var year:Int = 0
    private var month:Int = 0
    private var day:Int = 0
    private var hourOfDay:Int = 0
    private var minute:Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityEditEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bundle = this.intent.extras
        val id = bundle!!.getInt("id")
        year = bundle.getInt("year")
        month = bundle.getInt("month")
        day = bundle.getInt("day")
        Log.d("EditActivityDay", day.toString())
        Log.d("EditActivitybundle", bundle.toString())
        hourOfDay = bundle.getInt("hourOfDay")
        minute = bundle.getInt("minute")
        val name = bundle.getString("name").toString()
        val isFinished = bundle.getInt("isFinished") != 0
        val cate = bundle.getString("cate").toString()
        val persons = bundle.getString("persons").toString()
        val location = bundle.getString("location").toString()
        val notification = bundle.getString("notification").toString()
        binding.inputNameUpdate.setText(name)
        binding.inputCateUpdate.setText(cate)
        binding.inputPersonUpdate.setText(persons)
        binding.inputLocationUpdate.setText(location)
        if (notification == "通知提醒"){
            binding.radioButtonNoticeUpdate.isChecked = true
        }else{
            binding.radioButtonAlarmUpdate.isChecked = true
        }
        binding.textChoosenDate.text = year.toString() + "年" + (month+1).toString() + "月" + day.toString() + "日"
        binding.textChoosenTime.text = hourOfDay.toString() + "时" + minute.toString() + "分"

        val dbHelper = todoEventDBHelper(this, "TodoEventDB.db", 1)
        val db = dbHelper.writableDatabase
        var radioSelected = findViewById<RadioButton>(binding.radioGroupUpdate.checkedRadioButtonId)

        binding.updateDB.setOnClickListener {
            val value = ContentValues().apply {
                //组装数据
                put("name",binding.inputNameUpdate.text.toString())
                put("isFinished",false)
                put("year",year)
                put("month",month)
                put("day",day)
                put("cate",binding.inputCateUpdate.text.toString())
                put("persons",binding.inputPersonUpdate.text.toString())
                put("location",binding.inputLocationUpdate.text.toString())
                put("notification",radioSelected.text.toString())
                put("hourOfDay",hourOfDay)
                put("minute",minute)
            }
            db.update("TodoEventTable",value,"id = ?", arrayOf(id.toString()))
            Toast.makeText(this, "修改成功", Toast.LENGTH_LONG).show()
            finish()
        }
        binding.deleteDB.setOnClickListener {
            db.delete("TodoEventTable","id = ?", arrayOf(id.toString()))
            Toast.makeText(this, "删除成功", Toast.LENGTH_LONG).show()
            finish()
        }
        // 选择日期
        binding.buttonAddDateUpdate.setOnClickListener {
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                this.year = year
                this.month = monthOfYear
                this.day = dayOfMonth
                binding.textChoosenDate.setText(year.toString() + "年" + (monthOfYear+1) + "月"  + dayOfMonth + "日")
            }, year, month, day)
            dpd.show()
        }
        // 选择时间
        binding.buttonAddTimeUpdate.setOnClickListener {
            val tpd = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute->
                this.hourOfDay = hourOfDay
                this.minute = minute
                binding.textChoosenTime.setText(hourOfDay.toString() + "时" + minute + "分")
            },hourOfDay, minute, true)
            tpd.show()
        }
    }
}