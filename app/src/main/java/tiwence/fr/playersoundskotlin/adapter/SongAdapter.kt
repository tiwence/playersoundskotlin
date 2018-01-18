package tiwence.fr.playersoundskotlin.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.RxView
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.list_song_header.view.*
import kotlinx.android.synthetic.main.list_song_item.view.*
import tiwence.fr.playersoundskotlin.R
import tiwence.fr.playersoundskotlin.model.Song

/**
 * Created by Tiwence on 10/01/2018.
 */
class SongAdapter(private val songs: ArrayList<Song>?, private val context: Context, private val query: String?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_HEADER: Int = 0
    private val TYPE_ITEM: Int = 1

    override fun getItemCount(): Int {
        return songs!!.size + 1 //According to the header view...
    }

    private val onClickSubject = PublishSubject.create<Int>()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.itemView.headerTextView.text = if (query.isNullOrEmpty()) context.getText(R.string.top) else context.getString(R.string.search_results, query)
        } else if (holder is SongViewHolder && position < songs!!.size) {
            holder.bindSongs(songs[position], context)
            RxView.clicks(holder.itemView)
                    .map { aVoid ->
                        position
                    }
                    .subscribe(onClickSubject)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_HEADER) {
            val inflatedView = LayoutInflater.from(context).inflate(R.layout.list_song_header, parent, false)
            return HeaderViewHolder(inflatedView)
        } else {
            val inflatedView = LayoutInflater.from(context).inflate(R.layout.list_song_item, parent, false)
            return SongViewHolder(inflatedView)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0)
            return TYPE_HEADER
        return TYPE_ITEM
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        super.onDetachedFromRecyclerView(recyclerView)
        onClickSubject.onComplete()
    }

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindSongs(song: Song, context: Context) {
            itemView.songNameTextView.text = song.name ?: song.trackName
            itemView.artistNameTextView.text = song.artistName
            Picasso.with(context).load(song.artworkUrl100).into(itemView.artworkImageView)
        }
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) { }

    fun getPositionClicks(): Observable<Int> {
        return onClickSubject
    }

}