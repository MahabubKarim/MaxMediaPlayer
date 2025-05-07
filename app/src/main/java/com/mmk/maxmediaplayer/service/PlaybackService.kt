package com.mmk.maxmediaplayer.service

import android.app.Notification
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject
    lateinit var player: ExoPlayer
    @Inject
    lateinit var notificationAdapter: NotificationAdapter
    lateinit var mediaSession: MediaSession
    private var notificationManager: PlayerNotificationManager? = null
    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: MutableStateFlow<Track?> get() = _currentTrack
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> get() = _isPlaying
    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> get() = _playbackPosition
    private val _playbackDuration = MutableStateFlow(0L)
    val playbackDuration: StateFlow<Long> get() = _playbackDuration
    private val _bufferedPosition = MutableStateFlow(0L)
    val bufferedPosition: StateFlow<Long> get() = _bufferedPosition

    private var playlist: List<Track> = emptyList()

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    override fun onDestroy() {
        notificationManager?.setPlayer(null)
        mediaSession.release()
        player.release()
        super.onDestroy()
    }

    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSession.Builder(this, player)
            .setId("playback_media_session")
            .build()
        initNotification()
        setupPlayerListeners()
    }

    private fun initNotification() {
        notificationManager = PlayerNotificationManager.Builder(
            this, NOTIFICATION_ID, NOTIFICATION_CHANNEL_ID
        ).apply {
            setMediaDescriptionAdapter(notificationAdapter)
            setSmallIconResourceId(R.drawable.ic_music_note)
            setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationPosted(
                    notificationId: Int,
                    notification: Notification,
                    ongoing: Boolean
                ) {
                    if (ongoing) startForeground(notificationId, notification)
                }
                override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                    stopSelf()
                }
            })
        }.build().apply {
            setPlayer(player)
            setMediaSessionToken(mediaSession.sessionCompatToken)
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setUseNextAction(true)
            setUsePreviousAction(true)
            setUsePlayPauseActions(true)
        }
    }

    private fun setupPlayerListeners() {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                _isPlaying.value = player.isPlaying
            }

            override fun onEvents(player: Player, events: Player.Events) {
                _playbackPosition.value = player.currentPosition
                _playbackDuration.value = player.duration
                _bufferedPosition.value = player.bufferedPosition
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = player.currentMediaItemIndex
                _currentTrack.value = playlist.getOrNull(index)
            }
        })
    }

    private fun createMediaItem(track: Track): MediaItem {
        return MediaItem.Builder()
            .setUri(track.audioUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.artist)
                    .setArtworkUri(track.imageUrl.toUri())
                    .setExtras(Bundle().apply {
                        putString("track_id", track.id)
                    })
                    .build()
            )
            .build()
    }

    fun play(track: Track) {
        playlist = listOf(track)
        player.setMediaItem(createMediaItem(track))
        player.prepare()
        player.play()
        _currentTrack.value = track
        _isPlaying.value = true
        _playbackPosition.value = 0L
        _playbackDuration.value = track.duration
    }

    fun playPlaylist(tracks: List<Track>, startIndex: Int) {
        if (tracks.isEmpty() || startIndex !in tracks.indices) return

        playlist = tracks
        val mediaItems = tracks.map { createMediaItem(it) }
        player.setMediaItems(mediaItems, startIndex, 0L)
        player.prepare()
        player.play()

        val selectedTrack = tracks[startIndex]
        _currentTrack.value = selectedTrack
        _isPlaying.value = true
        _playbackPosition.value = 0L
        _playbackDuration.value = selectedTrack.duration
    }

    fun pause() {
        player.pause()
        _isPlaying.value = false
    }

    fun resume() {
        player.play()
    }

    fun stop() {
        player.stop()
        _currentTrack.value = null
        _isPlaying.value = false
        stopSelf()
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    /**
     * Skip to the next track in the playlist, if there is one.
     */
    fun skipNext() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
            // update our currentTrack flow
            val nextIndex = player.currentMediaItemIndex
            _currentTrack.value = playlist.getOrNull(nextIndex)
        }
    }

    /**
     * Skip to the previous track in the playlist, or restart current if at zero.
     */
    fun skipPrevious() {
        if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
            val prevIndex = player.currentMediaItemIndex
            _currentTrack.value = playlist.getOrNull(prevIndex)
        } else {
            // if we're already at the first item, just restart it
            player.seekTo(0)
            _currentTrack.value = playlist.firstOrNull()
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 101
        private const val NOTIFICATION_CHANNEL_ID = "playback_channel"
    }
}
