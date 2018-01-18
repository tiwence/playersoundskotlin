package tiwence.fr.playersoundskotlin

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxSeekBar
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.media_player_layout.*
import tiwence.fr.playersoundskotlin.model.Song
import tiwence.fr.playersoundskotlin.service.MusicService
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Tiwence on 15/01/2018.
 */
class MediaPlayerActivity : AppCompatActivity() {

    var mSongs: ArrayList<Song>? = null
    var mPosition: Int? = 0

    lateinit var mMusicService: MusicService

    var mPlayIntent: Intent? = null
    var mMusicBound = false

    private val df = SimpleDateFormat("m:ss")

    private lateinit var mSeekBarSubscription: Disposable

    /**
     * Connection use to bind the MusicService which will handle all MediaPlayer behaviours
     */
    private val musicConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            val binder = service as MusicService.MusicBinder
            mMusicService = binder.service
            mMusicBound = true

            mMusicService.setmContext(this@MediaPlayerActivity)
            mMusicService.setSongs(mSongs)
            mMusicService.setSongIndex(mPosition!!)

            //Observable used to bind all update song informations according to the current song played by the MediaPlayer
            mMusicService.getMusicPositionObservable().subscribeOn(AndroidSchedulers.mainThread()).subscribe { position ->
                mPosition = position
                displaySongInformations()
            }

            //Used to update enable function of the play button when the music stream is prepared properly
            mMusicService.getMusicPreparedObservable().subscribeOn(AndroidSchedulers.mainThread()).subscribe { _ ->
                playPauseButton.isEnabled = true
            }

            //Used to update the media player progression according to user clicks on the seekbar
            mSeekBarSubscription = RxSeekBar.userChanges(mediaPlayerSeekBar).subscribeOn(AndroidSchedulers.mainThread()).subscribe { progress ->
                mMusicService.getmMediaPlayer().seekTo(progress)
            }

            mMusicService.playSong()

            //Used to update seekbar progression according to the current music listening progression
            mMusicService.getMusicTicksObservable().observeOn(AndroidSchedulers.mainThread()).subscribe { progress ->
                val currentTime = df.format(Date(if (progress > 30000) 0 else progress.toLong() ))
                mediaPlayerSeekBar.setProgress(if (progress > 30000) 0 else progress)
                mediaPlayerCurrentTime.text = currentTime
            }

        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mMusicBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.media_player_layout)

        supportActionBar!!.hide()

        mSongs = this.intent.getSerializableExtra("songs") as ArrayList<Song>?
        mPosition = this.intent.getIntExtra("position", 0)

        //Click listeners
        RxView.clicks(playPauseButton).takeUntil(RxView.detaches(playPauseButton)).subscribe { click ->
            if (mMusicService.getmMediaPlayer().isPlaying()) {
                mMusicService.getmMediaPlayer().pause()
                playPauseButton.setBackgroundResource(R.mipmap.ic_mediaplayer_play)
            } else {
                mMusicService.getmMediaPlayer().start()
                playPauseButton.setBackgroundResource(R.mipmap.ic_mediaplayer_pause)
            }
        }

        RxView.clicks(mediaPlayerNextButton).takeUntil(RxView.detaches(mediaPlayerNextButton)).subscribe { click ->
            playPauseButton.setBackgroundResource(R.mipmap.ic_mediaplayer_pause)
            playPauseButton.isEnabled = false
            mMusicService.playNext()
        }

        RxView.clicks(mediaPlayerPrevButton).takeUntil(RxView.detaches(mediaPlayerPrevButton)).subscribe { click ->
            playPauseButton.setBackgroundResource(R.mipmap.ic_mediaplayer_pause)
            playPauseButton.isEnabled = false
            mMusicService.playPrevious()
        }

        displaySongInformations()
    }

    override fun onStart() {
        super.onStart()
        if (mPlayIntent == null) {
            mPlayIntent = Intent(this, MusicService::class.java)
            applicationContext.bindService(mPlayIntent, musicConnection, Context.BIND_AUTO_CREATE)
            startService(mPlayIntent)
        }
    }

    override fun onPause() {
        super.onPause()
        if(!mSeekBarSubscription.isDisposed)
            mSeekBarSubscription.dispose()
        applicationContext.unbindService(musicConnection)
    }

    override fun onDestroy() {
        applicationContext.stopService(mPlayIntent)
        super.onDestroy()
    }

    fun displaySongInformations() {
        val currentSong = mSongs!![this.mPosition!!]

        mediaPlayerSongName.text = currentSong.trackName ?: currentSong.name
        mediaPlayerArtistName.text = currentSong.artistName
        Picasso.with(this).load(currentSong.artworkUrl100).into(mediaPlayerArtwork)
        mediaPlayerCurrentTime.text = this.getText(R.string.placeholder_time)
        mediaPlayerTotalTime.text = this.getText(R.string.placeholder_totaltime)

        mediaPlayerSeekBar.progress = 0
        mediaPlayerSeekBar.max = 30000

        val totalTime = df.format(Date(mediaPlayerSeekBar.max.toLong()))
        mediaPlayerTotalTime.text = totalTime
    }

}