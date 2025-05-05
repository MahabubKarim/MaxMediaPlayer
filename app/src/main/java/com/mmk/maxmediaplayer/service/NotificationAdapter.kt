package com.mmk.maxmediaplayer.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerNotificationManager
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.mmk.maxmediaplayer.MainActivity
import com.mmk.maxmediaplayer.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@UnstableApi
class NotificationAdapter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageLoader: ImageLoader
) : PlayerNotificationManager.MediaDescriptionAdapter {

    override fun getCurrentContentTitle(player: Player): CharSequence {
        return player.mediaMetadata.title?.toString() ?: context.getString(R.string.unknown_track)
    }

    override fun getCurrentContentText(player: Player): CharSequence {
        return player.mediaMetadata.artist?.toString() ?: context.getString(R.string.unknown_artist)
    }

    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? {
        player.mediaMetadata.artworkUri?.let { uri ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = ImageRequest.Builder(context)
                        .data(uri)
                        .allowHardware(false)
                        .error(R.drawable.ic_music_note)
                        .fallback(R.drawable.ic_music_note)
                        .build()

                    when (val result = imageLoader.execute(request)) {
                        is SuccessResult -> callback.onBitmap(result.drawable.toBitmap())
                        else -> callback.onBitmap(getDefaultBitmap())
                    }
                } catch (e: Exception) {
                    callback.onBitmap(getDefaultBitmap())
                }
            }
        }
        return null
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        return PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getDefaultBitmap(): Bitmap {
        return context.getDrawable(R.drawable.ic_music_note)!!.toBitmap()
    }
}