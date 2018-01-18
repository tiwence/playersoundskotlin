package tiwence.fr.playersoundskotlin.module

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Created by Tiwence on 09/01/2018.
 */

@Module
class NetModule {

    var mBaseUrl: String? = ""

    constructor(baseUrl: String) {
        mBaseUrl = baseUrl
    }

    @Provides
    @Singleton
    fun providesSharedPreferences(application: Application?) : SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(application)
    }

    @Provides
    @Singleton
    fun providesHttpCache(application: Application?) : Cache {
        val cacheSize: Long = 10 * 1024 * 1024;
        val cache = Cache(
                application?.cacheDir,
                cacheSize
        )
        return cache
    }

    @Provides
    @Singleton
    fun providesGson() : Gson {
        val gsonBuilder: GsonBuilder = GsonBuilder()
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        return gsonBuilder.create()
    }

    @Provides
    @Singleton
    fun providesOkHttpClient(cache: Cache): OkHttpClient {
        val okHttpClient: OkHttpClient = OkHttpClient.Builder().cache(cache).build()
        return okHttpClient
    }

    @Provides
    @Singleton
    fun providesRetrofit(gson: Gson, okHttpClient: OkHttpClient) : Retrofit {
        val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(mBaseUrl)
                .client(okHttpClient)
                .build()
        return retrofit
    }

}