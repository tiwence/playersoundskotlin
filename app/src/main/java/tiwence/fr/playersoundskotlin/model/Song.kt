package tiwence.fr.playersoundskotlin.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Tiwence on 09/01/2018.
 */

class Song : Serializable {
    var id: String? = null
    @SerializedName("artistName")
    var artistName: String? = null
    var name: String? = null
    @SerializedName("trackName")
    var trackName: String? = null
    @SerializedName("artistId")
    var artistId: String? = null
    @SerializedName("artistUrl")
    var artistUrl: String? = null
    @SerializedName("artworkUrl100")
    var artworkUrl100: String? = null
    @SerializedName("previewUrl")
    var previewUrl: String? = null
    @SerializedName("collectionName")
    var collectionName: String? = null
    var url: String? = null
    @SerializedName("trackTimeMillis")
    var trackTimeMillis: Long? = null
}