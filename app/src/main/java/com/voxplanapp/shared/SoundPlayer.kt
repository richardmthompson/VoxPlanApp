package com.voxplanapp.shared

import android.content.Context
import android.media.MediaPlayer
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.RawResourceDataSource

class SoundPlayer (private val context: Context) {
    private var player: ExoPlayer? = null

    init {
        player = ExoPlayer.Builder(context).build()
    }

    fun playSound(soundResourceId: Int) {
        val mediaItem = MediaItem.fromUri("android.resource://${context.packageName}/$soundResourceId")
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.playWhenReady = true
        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    // Optionally handle completion
                }
            }
        })
    }

    fun release() {
        player?.release()
        player = null
    }
}
