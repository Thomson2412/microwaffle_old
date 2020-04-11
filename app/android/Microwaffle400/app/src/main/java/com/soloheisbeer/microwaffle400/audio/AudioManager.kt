package com.soloheisbeer.microwaffle400.audio

import android.content.Context
import android.media.MediaPlayer

class AudioManager (val context: Context) {

    var sounds = mutableMapOf<String, MediaPlayer>()

    fun init() {
        val fields = arrayListOf(
            "alarm",
            "boot",
            "down",
            "loop",
            "up"
        )
        for (field in fields) {
            val mp = MediaPlayer.create(context, context.resources.getIdentifier(field, "raw", context.packageName))
            sounds[field] = mp
        }
    }

    fun play(name: String, loop: Boolean = false, volume: Float = 1.0f){
        val sound = sounds[name] ?: return
        sound.isLooping = loop
        sound.setVolume(volume, volume)

        stop(name)
        sound.start()
    }

    fun stop(name: String){
        val sound = sounds[name] ?: return
            sound.stop()
            sound.prepare()
    }

    fun cleanUp() {
        for(mp in sounds.values){
            mp.reset()
            mp.release()
        }
    }
}