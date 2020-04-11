package com.soloheisbeer.microwaffle400.utils

import android.content.Context
import com.soloheisbeer.microwaffle400.R

object MicroUtils {
    fun secondsToTimeString(context: Context, tis: Int): String{
        val min  = tis / 60
        val sec = tis % 60
        return context.getString(R.string.timer, min, sec)
    }
}