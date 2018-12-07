package com.android.quinnmc.faceradio

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_radio.*

class RadioFragment : Fragment() {

    interface RadioFragmentListener{
        fun onPlayPause()
        fun onSkipNext()
        fun onSkipPrev()
    }

    var currFaceId: Int? = null
    var currSong: String? = null
    var currArtist: String? = null
    var currEmotion: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        currFaceId = arguments?.getInt("face")
        currSong = arguments?.getString("track")
        currArtist = arguments?.getString("artist")
        currEmotion = arguments?.getString("emo")

        return inflater.inflate(R.layout.fragment_radio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        play_pause_btn.setOnClickListener {
            (activity as RadioActivity).onPlayPause()
        }
        next_song_btn.setOnClickListener {
            (activity as RadioActivity).onSkipNext()
        }
        prev_song_btn.setOnClickListener {
            (activity as RadioActivity).onSkipPrev()
        }

        if (currFaceId != null) {
            expression_graphic.setImageResource(currFaceId!!)
        }
        if (currSong != null && currArtist != null) {
            current_track_title.text = "Now Playing"
            current_track_label.text = currSong
            by_title.text = "by"
            current_artist_label.text = currArtist
        }
    }

    override fun onStart() {
        super.onStart()
        share_button.setOnClickListener {
            toNewPreMessage()
        }
    }

    fun toNewPreMessage() {
        val newMsgIntent = Intent(activity, NewMessageActivity::class.java)
        val preset = ("I'm " + currEmotion + " listening to " + currSong + " by " + currArtist + "!")
        newMsgIntent.putExtra(USER_KEY, preset)
        startActivity(newMsgIntent)
    }

    companion object {

        val USER_KEY = "USER_KEY"

        fun newInstance(faceGraphicId: Int, track: String, artist: String, emo: String) : RadioFragment {
            val args = Bundle()
            args.putInt("face", faceGraphicId)
            args.putString("track", track)
            args.putString("artist", artist)
            args.putString("emo", emo)
           // args.putString("album_cover", alb)
            val fragment = RadioFragment()
            fragment.arguments = args
            return fragment
        }
    }
}