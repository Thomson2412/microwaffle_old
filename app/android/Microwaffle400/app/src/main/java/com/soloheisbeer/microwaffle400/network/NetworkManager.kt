package com.soloheisbeer.microwaffle400.network

import android.app.Activity
import android.content.Context
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

interface StatusUpdateInterface {
    fun onStatusUpdate(status: JSONObject)
}

interface ConnectionUpdateInterface {
    fun connectedToMicrowave()
    fun disconnectedToMicrowave()
}

object NetworkManager {

    //private const val microURL = "http://192.168.178.146:3000"
    private const val microURL = "http://192.168.178.115:3000"
    private lateinit var socket: Socket
    private var statusUpdateCallbacks = ArrayList<StatusUpdateInterface>()
    private var connectionUpdateCallbacks = ArrayList<ConnectionUpdateInterface>()

    var isConnected = false

    init {
        try {
            socket = IO.socket(microURL)
            socket.on("connect", OnConnected)
            socket.on("disconnect", OnDisconnected)
            socket.on("statusUpdate", OnStatusUpdate)
        }
        catch (e: URISyntaxException) {
        }
    }

    object OnConnected : Emitter.Listener {
        override fun call(vararg args: Any?) {
            isConnected = true
            for (cuc in connectionUpdateCallbacks) {
                cuc.connectedToMicrowave()
            }
        }
    }

    object OnDisconnected : Emitter.Listener {
        override fun call(vararg args: Any?) {
            isConnected = false
            for (cuc in connectionUpdateCallbacks) {
                cuc.disconnectedToMicrowave()
            }
        }
    }

    object OnStatusUpdate : Emitter.Listener {
        override fun call(vararg args: Any?) {
            val data = args[0] as JSONObject
            for (suc in statusUpdateCallbacks) {
                suc.onStatusUpdate(data)
            }
        }
    }

    fun connectToMicrowave(){
        if(!socket.connected())
            socket.connect()
    }

    fun startMicrowave(timeInSeconds: Int){
        socket.emit("start", timeInSeconds)
    }

    fun pauseMicrowave(){
        socket.emit("pause")
    }

    fun stopMicrowave(){
        socket.emit("stop")
    }

    fun sendStatusUpdateRequest(){
        socket.emit("getStatus")
    }

    fun cleanUp(){
        socket.disconnect()
        socket.off("statusUpdate", OnStatusUpdate)
    }

    fun addStatusUpdateCallback(suc: StatusUpdateInterface){
        statusUpdateCallbacks.add(suc)
    }

    fun removeStatusUpdateCallback(suc: StatusUpdateInterface){
        statusUpdateCallbacks.remove(suc)
    }

    fun addConnectionUpdateCallback(cuc: ConnectionUpdateInterface){
        connectionUpdateCallbacks.add(cuc)
    }

    fun removeConnectionUpdateCallback(cuc: ConnectionUpdateInterface){
        connectionUpdateCallbacks.remove(cuc)
    }
}