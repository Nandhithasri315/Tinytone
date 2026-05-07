package com.example.tinytone

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

class SoundManager(context: Context) {
    private val soundPool: SoundPool
    private var clickSoundId = 0
    private var correctSoundId = 0
    private var incorrectSoundId = 0
    private var isLoaded = false

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
            
        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build()
            
        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) isLoaded = true
        }
        
        // Dynamically load sound resources to prevent compile errors if files don't exist
        val res = context.resources
        val pkg = context.packageName
        
        val clickResId = res.getIdentifier("click", "raw", pkg)
        val correctResId = res.getIdentifier("correct", "raw", pkg)
        val incorrectResId = res.getIdentifier("incorrect", "raw", pkg)
        
        if (clickResId != 0) clickSoundId = soundPool.load(context, clickResId, 1)
        if (correctResId != 0) correctSoundId = soundPool.load(context, correctResId, 1)
        if (incorrectResId != 0) incorrectSoundId = soundPool.load(context, incorrectResId, 1)
    }
    
    fun playClick() {
        if (isLoaded && clickSoundId != 0) {
            soundPool.play(clickSoundId, 1f, 1f, 1, 0, 1f)
        }
    }
    
    fun playCorrect() {
        if (isLoaded && correctSoundId != 0) {
            soundPool.play(correctSoundId, 1f, 1f, 1, 0, 1f)
        }
    }
    
    fun playIncorrect() {
        if (isLoaded && incorrectSoundId != 0) {
            soundPool.play(incorrectSoundId, 1f, 1f, 1, 0, 1f)
        }
    }
    
    fun release() {
        soundPool.release()
    }
}
