package tiwence.fr.playersoundskotlin.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Tiwence on 09/01/2018.
 */

class Feed : Serializable {
    var title: String? = null;
    @SerializedName("resultCount")
    var resultCount: Int? = null;
    var results: ArrayList<Song>? = null;
}
