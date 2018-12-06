package com.android.quinnmc.faceradio

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(val uuid: String, val username: String, val profileImageUrl: String,
           val happyPlaylists: MutableList<String>, val passivePlaylists: MutableList<String>,
           val sleepyPlaylists: MutableList<String>): Parcelable {

    constructor() : this("", "", "",
        arrayListOf<String>(), arrayListOf<String>(), arrayListOf<String>())

//    private val happyURIs: ArrayList<String>? = null
//    private val passiveURIs: ArrayList<String>? = null
//    private val sleepyURIs: ArrayList<String>? = null
}