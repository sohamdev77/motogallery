package com.motogallery.app.media.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.motogallery.app.data.preferences.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VideoPlayerState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val isBuffering: Boolean = false,
    val isEnded: Boolean = false,
    val error: String? = null
)

class VideoPlayerManager(
    private val context: Context,
    private val preferences: AppPreferences
) {

    private var exoPlayer: ExoPlayer? = null

    private val _playerState = MutableStateFlow(VideoPlayerState())
    val playerState: StateFlow<VideoPlayerState> = _playerState.asStateFlow()

    private var currentUri: Uri? = null

    fun getOrCreatePlayer(): ExoPlayer {
        return exoPlayer ?: ExoPlayer.Builder(context).build().also { player ->
            exoPlayer = player
            player.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            _playerState.value = _playerState.value.copy(isBuffering = true, isEnded = false)
                        }
                        Player.STATE_READY -> {
                            _playerState.value = _playerState.value.copy(
                                isBuffering = false,
                                durationMs = player.duration.coerceAtLeast(0L),
                                isEnded = false,
                                error = null
                            )
                        }
                        Player.STATE_ENDED -> {
                            _playerState.value = _playerState.value.copy(
                                isPlaying = false,
                                isEnded = true,
                                isBuffering = false
                            )
                        }
                        Player.STATE_IDLE -> {
                            _playerState.value = _playerState.value.copy(isBuffering = false)
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    _playerState.value = _playerState.value.copy(
                        isPlaying = false,
                        isBuffering = false,
                        error = "Playback error: ${error.localizedMessage ?: "Unsupported format"}"
                    )
                }
            })
        }
    }

    fun prepare(uri: Uri) {
        val player = getOrCreatePlayer()
        currentUri = uri

        val savedPosition = preferences.getVideoPosition(uri.toString())

        val exoItem = ExoMediaItem.fromUri(uri)
        player.setMediaItem(exoItem)
        player.prepare()

        if (savedPosition > 0L) {
            player.seekTo(savedPosition)
        }

        player.playWhenReady = true
    }

    fun play() {
        exoPlayer?.play()
    }

    fun pause() {
        exoPlayer?.pause()
        saveCurrentPosition()
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        _playerState.value = _playerState.value.copy(currentPositionMs = positionMs)
    }

    fun seekForward(deltaMs: Long = 10000L) {
        val player = exoPlayer ?: return
        val target = (player.currentPosition + deltaMs).coerceAtMost(player.duration)
        seekTo(target)
    }

    fun seekBackward(deltaMs: Long = 10000L) {
        val player = exoPlayer ?: return
        val target = (player.currentPosition - deltaMs).coerceAtLeast(0L)
        seekTo(target)
    }

    fun setPlaybackSpeed(speed: Float) {
        val player = exoPlayer ?: return
        player.playbackParameters = PlaybackParameters(speed)
        _playerState.value = _playerState.value.copy(playbackSpeed = speed)
    }

    fun updateProgress() {
        val player = exoPlayer ?: return
        _playerState.value = _playerState.value.copy(
            currentPositionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = player.duration.coerceAtLeast(0L)
        )
    }

    private fun saveCurrentPosition() {
        val player = exoPlayer ?: return
        val uri = currentUri ?: return
        if (player.currentPosition > 1000L && player.currentPosition < player.duration - 2000L) {
            preferences.saveVideoPosition(uri.toString(), player.currentPosition)
        } else {
            preferences.saveVideoPosition(uri.toString(), 0L)
        }
    }

    fun release() {
        saveCurrentPosition()
        exoPlayer?.release()
        exoPlayer = null
    }
}
