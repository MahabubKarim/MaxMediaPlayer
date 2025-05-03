package com.mmk.maxmediaplayer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.mmk.maxmediaplayer.R
import com.mmk.maxmediaplayer.domain.model.Track
import com.mmk.maxmediaplayer.ui.screen.player.PlaybackState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject lateinit var player: ExoPlayer
    @Inject lateinit var notificationAdapter: NotificationAdapter
    var mediaSession: MediaSession? = null
    private var notificationManager: PlayerNotificationManager? = null

    // State flows
    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition

    private val _playbackDuration = MutableStateFlow(0L)
    val playbackDuration: StateFlow<Long> = _playbackDuration

    private val _bufferedPosition = MutableStateFlow(0L)
    val bufferedPosition: StateFlow<Long> = _bufferedPosition

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState

    override fun onCreate() {
        super.onCreate()
        initializeMediaSession()
        initializeNotificationManager()
        createNotificationChannel()
        setupPlayerListeners()
    }

    private fun setupPlayerListeners() {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                _isPlaying.value = player.isPlaying
                _playbackState.value = when (playbackState) {
                    Player.STATE_IDLE -> PlaybackState.Idle
                    Player.STATE_BUFFERING -> PlaybackState.Loading
                    Player.STATE_READY -> PlaybackState.Ready
                    Player.STATE_ENDED -> PlaybackState.Ended
                    else -> PlaybackState.Idle
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                _playbackPosition.value = player.currentPosition
            }

            override fun onEvents(player: Player, events: Player.Events) {
                _playbackPosition.value = player.currentPosition
                _playbackDuration.value = player.duration
                _bufferedPosition.value = player.bufferedPosition
            }
        })
    }

    fun play(track: Track) {
        player.setMediaItem(createMediaItem(track))
        player.prepare()
        player.play()
        _currentTrack.value = track
    }

    fun pause() {
        player.pause()
    }

    fun resume() {
        player.play()
    }

    fun stop() {
        player.stop()
        _currentTrack.value = null
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    fun updateCurrentTrack(track: Track) {
        _currentTrack.value = track
    }

    private fun createMediaItem(track: Track): MediaItem {
        return MediaItem.Builder()
            .setUri(track.audioUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.artist)
                    //.setArtworkUri(track.albumArtUrl?.toUri())
                    .build()
            )
            .build()
    }

    /*override fun onCreate() {
        super.onCreate()
        initializeMediaSession()
        initializeNotificationManager()
        createNotificationChannel()
    }*/

    private fun initializeMediaSession() {
        mediaSession = MediaSession.Builder(this, player).build()
    }

    @OptIn(UnstableApi::class)
    private fun initializeNotificationManager() {
        notificationManager = PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID
        ).apply {
            setMediaDescriptionAdapter(notificationAdapter)
            setSmallIconResourceId(R.drawable.ic_music_note)
            setNotificationListener(NotificationListener())
        }.build().apply {
            setPlayer(player)
            setPriority(NotificationCompat.PRIORITY_LOW)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Audio playback controls" }

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    @OptIn(UnstableApi::class)
    override fun onDestroy() {
        notificationManager?.setPlayer(null)
        mediaSession?.release()
        mediaSession = null
        player.release()
        super.onDestroy()
    }

    @UnstableApi
    private inner class NotificationListener : PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            startForeground(notificationId, notification)
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 101
        private const val NOTIFICATION_CHANNEL_ID = "playback_channel"
    }
}