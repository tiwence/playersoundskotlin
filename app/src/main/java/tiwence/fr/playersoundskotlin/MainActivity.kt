package tiwence.fr.playersoundskotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Retrofit
import tiwence.fr.playersoundskotlin.adapter.SongAdapter
import tiwence.fr.playersoundskotlin.model.ItunesResult
import tiwence.fr.playersoundskotlin.util.Constants
import tiwence.fr.playersoundskotlin.service.RestAPIService
import javax.inject.Inject
import tiwence.fr.playersoundskotlin.model.Song
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var mItunesAPIService: RestAPIService

    @Inject
    lateinit var mRetrofit: Retrofit

    private lateinit var searchDisposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        (application as MyApplication).getNetComponent()?.inject(this);

        supportActionBar!!.hide()

        //songsListView = findViewById(R.id.songsListView)
        songsListView.layoutManager = LinearLayoutManager(this)

        mItunesAPIService =  mRetrofit.create<RestAPIService>(RestAPIService::class.java)

        //Observable used to perform search query
        //I know there is a better way with RxBinding like this way : RxSearchview.queryQuanges...
        val searchObservable = floatingSearchViewObservable()
        searchDisposable = searchObservable
                .debounce(300, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.newThread())
                .switchMap { query ->
                    mItunesAPIService.getItunesSearchQuery(Constants.COUNTRY, "music", query)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { feed ->
                    if (feed.resultCount == 0) {
                        loadItunesChart()
                    } else {
                        songsListView.adapter = SongAdapter(feed.results, this@MainActivity, floatingSearchView.query)
                        (songsListView.adapter as SongAdapter).getPositionClicks().subscribe { position ->
                            launchMediaPlayer(position, feed.results)
                        }
                    }
                }

        //loading itunes charts
        loadItunesChart()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!searchDisposable.isDisposed)
            searchDisposable.dispose()
    }

    private fun loadItunesChart() {
        var itunesChart: Observable<ItunesResult> = mItunesAPIService.getItunesTopCharts(Constants.mItunesChartBaseURL)
        itunesChart.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { ituneResult ->
                    songsListView.adapter = SongAdapter(ituneResult.feed.results, this@MainActivity, null)
                    (songsListView.adapter as SongAdapter).getPositionClicks().subscribe { position ->
                        launchMediaPlayer(position, ituneResult.feed.results)
                    }
                }
    }

    private fun launchMediaPlayer(position: Int?, results: ArrayList<Song>?) {
        val intent = Intent(this@MainActivity, MediaPlayerActivity::class.java)
        intent.putExtra(Constants.SONGS, results)
        intent.putExtra(Constants.POSITION, position)
        startActivity(intent)
    }

    /**
     * Observable used to emit new query from the FloatingSearchView
     */
    private fun floatingSearchViewObservable(): Observable<String> {
        val subject = BehaviorSubject.create<String>()

        floatingSearchView.setOnQueryChangeListener { oldQuery, newQuery ->
            subject.onNext(newQuery)
        }

        return subject
    }

}
