package digital.ollis.android.photogallery

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.ImageView

private const val TAG = "PhotoGalleryFragment"

class PhotoGalleryFragment : Fragment() {

    private lateinit var photoRecyclerView: RecyclerView
    private var items = emptyList<GalleryItem>()
    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoHolder>

    interface Callbacks {
        fun onPhotoSelected(item: GalleryItem)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setHasOptionsMenu(true)
        updateItems()

//        requireContext().also {
//            val intent = PollService.newIntent(it)
//            it.startService(intent)
//        }

        val responseHandler = Handler()
        thumbnailDownloader = ThumbnailDownloader<PhotoHolder>(
                responseHandler
        ){ photoHolder, bitmap ->
            photoHolder.bindDrawable(BitmapDrawable(resources, bitmap), GalleryItem())
        }.apply {
            start()
            looper
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)

        photoRecyclerView = view.findViewById(R.id.photo_recycler_view) as RecyclerView
        photoRecyclerView.layoutManager = GridLayoutManager(context, 3)

        setupAdapter()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        thumbnailDownloader.clearQueue()
    }

    override fun onDestroy() {
        super.onDestroy()
        thumbnailDownloader.quit()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.fragment_photo_gallery, menu)

        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView

        searchView.apply {
            setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    QueryPreferences.setStoredQuery(context, query ?: "")
                    updateItems()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }
            })

            setOnClickListener {
                searchView.setQuery(QueryPreferences.getStoredQuery(requireContext()), false)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_item_clear -> {
                QueryPreferences.setStoredQuery(requireContext(), "")
                updateItems()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateItems() {
        FetchItemsTask(QueryPreferences.getStoredQuery(requireContext())).execute()
    }

    private fun setupAdapter() {
        if (isAdded) {
            photoRecyclerView.adapter = PhotoAdapter(items)
        }
    }

    private class PhotoHolder(itemView: ImageView) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val itemImageView: ImageView = itemView
        private lateinit var galleryItem: GalleryItem

        fun bindDrawable(drawable: Drawable, item: GalleryItem) {
            galleryItem = item
            itemImageView.setImageDrawable(drawable)
        }

        override fun onClick(v: View?) {

        }

    }

    private inner class PhotoAdapter(private val galleryItems: List<GalleryItem>) : RecyclerView.Adapter<PhotoHolder>() {
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): PhotoHolder {
            val view = layoutInflater.inflate(R.layout.list_item_gallery,
                    p0, false) as ImageView
            return PhotoHolder(view)
        }

        override fun getItemCount(): Int = galleryItems.size

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            val placeholder = ContextCompat.getDrawable(requireContext(), R.drawable.placeholder)
            holder.bindDrawable(placeholder!!, galleryItems[position])
            thumbnailDownloader.queueThumbnail(holder, galleryItems[position].url)
        }
    }

    private inner class FetchItemsTask(private val query: String = "") : AsyncTask<Unit, Unit, List<GalleryItem>>() {

        override fun doInBackground(vararg params: Unit?): List<GalleryItem> {
            if (query.isNotEmpty())
                return FlickrFetchr().searchPhotos(query)

            return FlickrFetchr().fetchRecentPhotos()
        }

        override fun onPostExecute(result: List<GalleryItem>) {
            items = result
            setupAdapter()
        }
    }

    companion object {
        fun newInstance() = PhotoGalleryFragment()
    }
}
