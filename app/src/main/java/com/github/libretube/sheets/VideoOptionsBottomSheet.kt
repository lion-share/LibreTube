package com.github.libretube.sheets

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import com.github.libretube.Globals
import com.github.libretube.R
import com.github.libretube.constants.IntentData
import com.github.libretube.constants.PLAYER_NOTIFICATION_ID
import com.github.libretube.dialogs.AddToPlaylistDialog
import com.github.libretube.dialogs.DownloadDialog
import com.github.libretube.dialogs.ShareDialog
import com.github.libretube.util.BackgroundHelper
import com.github.libretube.util.PreferenceHelper
import com.github.libretube.views.BottomSheet

/**
 * Dialog with different options for a selected video.
 *
 * Needs the [videoId] to load the content from the right video.
 */
class VideoOptionsBottomSheet(
    private val videoId: String
) : BottomSheet() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // List that stores the different menu options. In the future could be add more options here.
        val optionsList = mutableListOf(
            context?.getString(R.string.playOnBackground)!!,
            context?.getString(R.string.addToPlaylist)!!,
            context?.getString(R.string.download)!!,
            context?.getString(R.string.share)!!
        )

        // remove the add to playlist option if not logged in
        if (PreferenceHelper.getToken() == "") {
            optionsList.remove(
                context?.getString(R.string.addToPlaylist)

            )
        }
        /**
         * Check whether the player is running by observing the notification
         */
        try {
            val notificationManager =
                context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notificationManager.activeNotifications.forEach {
                    if (it.id == PLAYER_NOTIFICATION_ID) {
                        optionsList += context?.getString(R.string.add_to_queue)!!
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setSimpleItems(optionsList) { which ->
            when (optionsList[which]) {
                // Start the background mode
                context?.getString(R.string.playOnBackground) -> {
                    BackgroundHelper.playOnBackground(requireContext(), videoId)
                }
                // Add Video to Playlist Dialog
                context?.getString(R.string.addToPlaylist) -> {
                    val token = PreferenceHelper.getToken()
                    if (token != "") {
                        val newFragment = AddToPlaylistDialog()
                        val bundle = Bundle()
                        bundle.putString(IntentData.videoId, videoId)
                        newFragment.arguments = bundle
                        newFragment.show(
                            parentFragmentManager,
                            AddToPlaylistDialog::class.java.name
                        )
                    } else {
                        Toast.makeText(context, R.string.login_first, Toast.LENGTH_SHORT).show()
                    }
                }
                context?.getString(R.string.download) -> {
                    val downloadDialog = DownloadDialog(videoId)
                    downloadDialog.show(parentFragmentManager, DownloadDialog::class.java.name)
                }
                context?.getString(R.string.share) -> {
                    val shareDialog = ShareDialog(videoId, false)
                    // using parentFragmentManager is important here
                    shareDialog.show(parentFragmentManager, ShareDialog::class.java.name)
                }
                context?.getString(R.string.add_to_queue) -> {
                    Globals.playingQueue += videoId
                }
            }
        }

        super.onCreate(savedInstanceState)
    }
}