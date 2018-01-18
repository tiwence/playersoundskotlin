package tiwence.fr.playersoundskotlin.util

import java.util.*

/**
 * Created by Tiwence on 10/01/2018.
 */

class Constants {
    companion object {
        val COUNTRY = Locale.getDefault().country
        val apiBaseUrl: String = "https://itunes.apple.com"
        val mItunesChartBaseURL = "https://rss.itunes.apple.com/api/v1/$COUNTRY/itunes-music/top-songs/all/100/explicit.json"
        val SONGS: String = "songs"
        val POSITION: String = "position"

    }
}