package digital.ollis.android.photogallery

import android.net.Uri
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "FlickrFetchr"
private const val API_KEY = ""
private const val FETCH_RECENTS_METHOD = "flickr.photos.getRecent"
private const val SEARCH_METHOD = "flickr.photos.search"

private val BASE_ENDPOINT_URI: Uri = Uri.parse("https://api.flickr.com/services/rest/")
        .buildUpon()
        .appendQueryParameter("api_key", API_KEY)
        .appendQueryParameter("format", "json")
        .appendQueryParameter("nojsoncallback", "1")
        .appendQueryParameter("extras", "url_s")
        .build()

private val FETCH_RECENTS_URL: String =
        BASE_ENDPOINT_URI.buildUpon()
                .appendQueryParameter("method", FETCH_RECENTS_METHOD)
                .build()
                .toString()

class FlickrFetchr {

    @Throws(IOException::class)
    fun getUrlBytes(urlSpec: String): ByteArray {
        val url = URL(urlSpec)
        val connection = url.openConnection() as HttpURLConnection
        try {
            val input = connection.inputStream
            if (connection.responseCode != HttpURLConnection.HTTP_OK)
                throw IOException("${connection.responseMessage}: with $urlSpec")

            return input.readBytes()
        } finally {
            connection.disconnect()
        }
    }

    @Throws(IOException::class)
    fun getUrlString(urlSpec: String) = String(getUrlBytes(urlSpec))

    fun fetchRecentPhotos(): List<GalleryItem> = fetchItems(FETCH_RECENTS_URL)

    fun searchPhotos(query: String): List<GalleryItem> =
            fetchItems(
                    BASE_ENDPOINT_URI.buildUpon()
                            .appendQueryParameter("method", SEARCH_METHOD)
                            .appendQueryParameter("text", query)
                            .build()
                            .toString()
            )

    fun fetchItems(url: String): List<GalleryItem> {
        val items = mutableListOf<GalleryItem>()

        try {
            val jsonString = JSONObject(getUrlString(url))
            Log.i(TAG, "Received JSON: $jsonString")
            parseItems(items, jsonString)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to fethc items", e)
        } catch (e: JSONException) {
            Log.e(TAG, "Failed to parse JSON", e)
        }

        return items
    }

    @Throws(IOException::class, JSONException::class)
    private fun parseItems(items: MutableList<GalleryItem>, jsonBody: JSONObject) {
        val photosJsonObject = jsonBody.getJSONObject("photos")
        val photoJsonArray = photosJsonObject.getJSONArray("photo")

        for (it in 0 until photoJsonArray.length()) {
            val photoJsonObject = photoJsonArray.getJSONObject(it)
            if (!photoJsonObject.has("url_s"))
                continue

            items.add(GalleryItem(
                    title = photoJsonObject.getString("title"),
                    id = photoJsonObject.getString("id"),
                    url = photoJsonObject.getString("url_s")
            ))
        }
    }
}
