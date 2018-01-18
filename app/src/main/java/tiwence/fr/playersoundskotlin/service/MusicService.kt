package tiwence.fr.playersoundskotlin.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.util.Pair
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import retrofit2.Retrofit
import rx.Subscription
import tiwence.fr.playersoundskotlin.MyApplication
import tiwence.fr.playersoundskotlin.model.Feed
import tiwence.fr.playersoundskotlin.model.Song
import tiwence.fr.playersoundskotlin.util.Constants
import java.util.ArrayList
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/**
 * Created by Tiwence on 17/12/2017.
 */

/**
 * Service used to handle all MediaPlayer behaviours asynchronously
 */
class MusicService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private lateinit var mMediaPlayer: MediaPlayer

    private var mSongs: ArrayList<Song>? = null
    private var mSongPos: Int = 0

    private var mContext: Context? = null

    @Inject
    lateinit var mRetrofit: Retrofit

    private lateinit var mItunesAPIService: RestAPIService

    private var searchDisposable: Disposable? = null
    private lateinit var ticksDisposable: Disposable

    private val musicBind = MusicBinder()

    //Observable used to update song informations in the MediaPlayerActivity
    private val onMusicChangeSubject = PublishSubject.create<Int>()
    private val onMusicPreparedSubject = PublishSubject.create<Int>()
    private val onMusicTicksSubject = BehaviorSubject.create<Int>()


    override fun onBind(intent: Intent): IBinder? {
        return musicBind
    }

    override fun onUnbind(intent: Intent): Boolean {
        mMediaPlayer.stop()
        mMediaPlayer.release()

        onMusicChangeSubject.onComplete()
        onMusicPreparedSubject.onComplete()
        onMusicTicksSubject.onComplete()

        if (searchDisposable != null && !searchDisposable!!.isDisposed)
            searchDisposable!!.dispose()
        if (!ticksDisposable.isDisposed)
            ticksDisposable.dispose()


        return false
    }

    override fun onCreate() {
        super.onCreate()

        (application as MyApplication).getNetComponent()?.inject(this)

        mItunesAPIService =  mRetrofit.create<RestAPIService>(RestAPIService::class.java)

        mSongPos = 0
        mMediaPlayer = MediaPlayer()

        val ticksObservable = createMediaPlayerTicksObservable();
        ticksDisposable = ticksObservable.subscribe { ticks ->
            onMusicTicksSubject.onNext(ticks)
        }

        initMusicPlayer()
    }

    fun initMusicPlayer() {
        mMediaPlayer.setWakeMode(applicationContext,
                PowerManager.PARTIAL_WAKE_LOCK)
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mMediaPlayer.setOnPreparedListener(this)
        mMediaPlayer.setOnCompletionListener(this)
        mMediaPlayer.setOnErrorListener(this)
    }

    /**
     * Method used to play selected song according to his streaming URL or get his streaming URL if it's not already set
     */
    fun playSong() {
        val playedSong = this.mSongs!![mSongPos]
        if (playedSong.previewUrl == null || "" == playedSong.previewUrl!!.trim()) {
            val lookup: Observable<Feed> = mItunesAPIService.getItunesLookup(Constants.COUNTRY, playedSong.id)
            searchDisposable = lookup.subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { feed ->
                        if (feed.resultCount!! > 0) {
                            playedSong.previewUrl = feed.results!!.get(0).previewUrl
                        }
                        playSongFinally(playedSong)
                    }
        } else {
            playSongFinally(playedSong)
        }
    }

    /**
     * Method used to play selected song according to his streaming URL
     */
    private fun playSongFinally(songToPlay: Song) {
        try {
            mMediaPlayer.reset()
            mMediaPlayer.setDataSource(songToPlay.previewUrl)
            mMediaPlayer.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun playNext() {
        this.mSongPos++
        if (mSongPos == mSongs!!.size)
            mSongPos = 0
        playSong()
        onMusicChangeSubject.onNext(mSongPos)
    }

    fun playPrevious() {
        this.mSongPos--
        if (mSongPos < 0)
            mSongPos = mSongs!!.size - 1
        playSong()
        onMusicChangeSubject.onNext(mSongPos)
    }

    override fun onError(mediaPlayer: MediaPlayer, i: Int, i1: Int): Boolean {
        return true
    }

    override fun onCompletion(mediaPlayer: MediaPlayer) {
        playNext()
    }

    override fun onPrepared(mediaPlayer: MediaPlayer) {
        mediaPlayer.start()
        onMusicPreparedSubject.onNext(mSongPos)
    }

    fun getmMediaPlayer(): MediaPlayer {
        return this.mMediaPlayer
    }

    fun getMusicPositionObservable(): Observable<Int> {
        return onMusicChangeSubject
    }

    fun getMusicPreparedObservable(): Observable<Int> {
        return onMusicPreparedSubject
    }

    fun getMusicTicksObservable(): Observable<Int> {
        return onMusicTicksSubject
    }

    fun setSongs(songs: ArrayList<Song>?) {
        this.mSongs = songs
    }

    fun setSongIndex(index: Int) {
        this.mSongPos = index
    }

    fun setmContext(mContext: Context) {
        this.mContext = mContext
    }

    inner class MusicBinder : Binder() {
        val service: MusicService
            get() = this@MusicService
    }

    private fun createMediaPlayerTicksObservable(): Observable<Int> {
        return Observable.interval(16, TimeUnit.MILLISECONDS)
                .map {  y -> mMediaPlayer.currentPosition }
                .onErrorReturn { y -> 0 }
    }
}
