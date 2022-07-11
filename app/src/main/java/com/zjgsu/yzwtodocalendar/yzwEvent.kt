package com.zjgsu.yzwtodocalendar

class todoEvent(val id:Int,
                val name:String,        //名字 name:String/text
                var isFinished:Boolean,  //是否完成 integer 1:完成 0:未完成
                val year:Int,                //日期
                val month:Int,
                val day:Int,
                val cate:String,      //分类
                val persons:String,      //参与人
                val location:String,     //活动地点
                val notification:String, //提醒方式   "通知提醒","响铃提醒"
                val hourOfDay:Int,          //提醒时间
                val minute:Int,
                ){
    fun getDate():String{
        return year.toString() + "年" + (month+1).toString() + "月" + day.toString() + "日"
    }
    fun getTime():String{
        return hourOfDay.toString() + "时" + minute.toString() + "分"
    }
    fun getDateAndTime():String{
        return year.toString() + "-" + (month+1).toString() + "-" + day.toString()+" "+"$hourOfDay:$minute"
    }

}


// TODO: 创建日程，设置提醒方式