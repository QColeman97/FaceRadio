package com.android.quinnmc.faceradio

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_radio.*

class RadioFragment : Fragment() {

    var currFaceId: Int? = null
    var currSong: String? = null
    var currArtist: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        currFaceId = arguments?.getInt("face")
        currSong = arguments?.getString("track")
        currArtist = arguments?.getString("artist")

        return inflater.inflate(R.layout.fragment_radio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

    companion object {

        fun newInstance(faceGraphicId: Int, track: String, artist: String) : RadioFragment {
            val args = Bundle()
            args.putInt("face", faceGraphicId)
            args.putString("track", track)
            args.putString("artist", artist)
            val fragment = RadioFragment()
            fragment.arguments = args
            return fragment
        }
    }
}