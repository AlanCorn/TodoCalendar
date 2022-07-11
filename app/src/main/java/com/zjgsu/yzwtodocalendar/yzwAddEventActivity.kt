package com.zjgsu.yzwtodocalendar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.zjgsu.yzwtodocalendar.databinding.ActivityAddEventBinding
import java.util.*


class yzwAddEventActivity : AppCompatActivity() {
    private var year = 2022
    private var month:Int = 1
    private var day:Int = 1
    private var hourOfDay:Int = 0
    private var minute:Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var binding = ActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getNow()
        binding.textChoosenDate.setText(year.toString() + "年" + (month+1) + "月"  + day + "日")
        binding.textChoosenTime.setText(hourOfDay.toString() + "时" + minute + "分")

        // 选择日期
        binding.buttonAddDate.setOnClickListener {
            getNow()
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                this.year = year
                this.month = monthOfYear
                this.day = dayOfMonth
                binding.textChoosenDate.setText(year.toString() + "年" + (monthOfYear+1) + "月"  + dayOfMonth + "日")
            }, year, month, day)
            dpd.show()
        }
        // 选择时间
        binding.buttonAddTime.setOnClickListener {
            getNow()
            val tpd = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute->
                this.hourOfDay = hourOfDay
                this.minute = minute
                binding.textChoosenTime.setText(hourOfDay.toString() + "时" + minute + "分")
            },hourOfDay, minute, true)
            tpd.show()
        }

        binding.insertDB.setOnClickListener {
            val dbHelper = todoEventDBHelper(this, "TodoEventDB.db", 1)
            val db = dbHelper.writableDatabase
            var radioSelected = findViewById<RadioButton>(binding.radioGroup.checkedRadioButtonId)
            if (binding.inputName.text.toString() == "")
                Toast.makeText(this, "日程名不可为空", Toast.LENGTH_LONG).show()
            else if (binding.inputCate.text.toString() == ""){
                Toast.makeText(this, "分类不可为空", Toast.LENGTH_LONG).show()
            }else if (binding.inputPerson.text.toString() == "")
                Toast.makeText(this, "参与人不可为空", Toast.LENGTH_LONG).show()
            else if (binding.inputLocation.text.toString() == "")
                Toast.makeText(this, "地点不可为空", Toast.LENGTH_LONG).show()
            else if (radioSelected.text.toString() == "")
                Toast.makeText(this, "请选择通知类型", Toast.LENGTH_LONG).show()
            else{
                val value = ContentValues().apply {
                    //组装数据
                    put("name",binding.inputName.text.toString())
                    put("isFinished",false)
                    put("year",year)
                    put("month",month)
                    put("day",day)
                    put("cate",binding.inputCate.text.toString())
                    put("persons",binding.inputPerson.text.toString())
                    put("location",binding.inputLocation.text.toString())
                    put("notification",radioSelected.text.toString())
                    put("hourOfDay",hourOfDay)
                    put("minute",minute)
                }
                db.insert("TodoEventTable",null, value)

                Toast.makeText(this, "添加成功", Toast.LENGTH_LONG).show()
                finish()
            }
        }

    }
    fun getNow(): String {
        var tms = Calendar.getInstance()
        year = tms.get(Calendar.YEAR)
        month = tms.get(Calendar.MONTH)
        day = tms.get(Calendar.DAY_OF_MONTH)
        hourOfDay = tms.get(Calendar.HOUR_OF_DAY)
        minute = tms.get(Calendar.MINUTE)
        return tms.get(Calendar.YEAR).toString() + "-" + tms.get(Calendar.MONTH).toString() + "-" + tms.get(Calendar.DAY_OF_MONTH).toString() + " " + tms.get(Calendar.HOUR_OF_DAY).toString() + ":" + tms.get(Calendar.MINUTE).toString()
    }
}