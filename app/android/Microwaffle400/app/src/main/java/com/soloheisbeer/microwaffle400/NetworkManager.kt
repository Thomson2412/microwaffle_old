package com.soloheisbeer.microwaffle400

import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

object NetworkManager {

    private val microURL = "http://192.168.178.146:3000"
    //private val microURL = "http://192.168.178.10:3000"
    private lateinit var socket: Socket
    private lateinit var uiUpdateInterface: UIUpdateInterface

    fun init(UI: UIUpdateInterface) {
        try {
            uiUpdateInterface = UI
            socket = IO.socket(microURL)
            socket.on("connect", OnConnected)
            socket.on("disconnect", OnDisconnected)
            socket.on("statusUpdate", OnStatusUpdate)
            socket.connect()
        }
        catch (e: URISyntaxException) {
        }
    }

    object OnConnected : Emitter.Listener {
        override fun call(vararg args: Any?) {
            uiUpdateInterface.connectedToMicrowave()
        }
    }

    object OnDisconnected : Emitter.Listener {
        override fun call(vararg args: Any?) {
            uiUpdateInterface.disconnectedToMicrowave()
        }
    }

    object OnStatusUpdate : Emitter.Listener {
        override fun call(vararg args: Any?) {
            val data = args[0] as JSONObject
            uiUpdateInterface.statusUpdate(data)
        }
    }

    fun startMicrowave(timeInSeconds: Int){
        socket.emit("start", timeInSeconds);
    }

    fun stopMicrowave(){
        socket.emit("stop");
    }

    fun sendStatusUpdateRequest(){
        socket.emit("getStatus");
    }

    fun cleanUp(){
        socket.disconnect()
        socket.off("statusUpdate", OnStatusUpdate)
    }
}