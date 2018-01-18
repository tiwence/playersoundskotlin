package tiwence.fr.playersoundskotlin.service

import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import tiwence.fr.playersoundskotlin.model.Feed
import tiwence.fr.playersoundskotlin.model.ItunesResult

/**
 * Created by Tiwence on 10/01/2018.
 */

interface RestAPIService {

    @GET
    fun getItunesTopCharts(@Url url: String): Observable<ItunesResult>

    @GET("search")
    fun getItunesSearchQuery(@Query("country") country: String, @Query("media") media: String?, @Query("term") term: String?): Observable<Feed>

    @GET("{country}/lookup")
    fun getItunesLookup(@Path("country") country: String, @Query("id") id: String?): Observable<Feed>
}