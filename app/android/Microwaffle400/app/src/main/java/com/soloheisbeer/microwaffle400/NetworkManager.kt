package com.soloheisbeer.microwaffle400

import android.os.AsyncTask
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class NetworkManager {

    private val microURL = "http://192.168.178.146"

    fun startMicrowave(sec: Int){
        val url = URL("$microURL/start?seconds=$sec")
        getTask().execute(url)
    }

    fun stopMicrowave(){
        val url = URL("$microURL/stop")
        getTask().execute(url)
    }

    fun getStatus(): String? {
        val url = URL("$microURL/status")
        return getTask().execute(url).get()
    }

    class getTask() : AsyncTask<URL, Void, String?>() {

        override fun doInBackground(vararg params: URL?): String? {
            var result:String? = null
            try{
                result = params[0]?.readText()
            }
            catch (e:Exception){
                e.printStackTrace()
            }

            return result
        }
    }

}