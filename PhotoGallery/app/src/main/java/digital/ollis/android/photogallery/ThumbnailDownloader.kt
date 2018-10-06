package digital.ollis.android.photogallery

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "ThumbnailDownloader"
private const val MESSAGE_DOWNLOAD = 0

class ThumbnailDownloader<in T>(
        private val responseHandler: Handler,
        private val onThumbnailDownloaded: (T, Bitmap) -> Unit
) : HandlerThread(TAG) {

    private var hasQuit = false
    private lateinit var requestHandler: Handler
    private val requestMap = ConcurrentHashMap<T, String>()

    @Suppress("UNCHECKED_CAST")
    @SuppressLint("HandlerLeak")
    override fun onLooperPrepared() {
        requestHandler = object : Handler() {
            override fun handleMessage(msg: Message?) {
                if (msg?.what == MESSAGE_DOWNLOAD) {
                    val target = msg?.obj as T
                    handleRequest(target)
                }
            }
        }
    }

    override fun quit(): Boolean {
        hasQuit = true
        return super.quit()
    }

    fun queueThumbnail(target: T, url: String) {
        Log.i(TAG, "GOt URL: $url")
        requestMap[target] = url
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget()
    }

    fun clearQueue() {
        requestHandler.removeMessages(MESSAGE_DOWNLOAD)
        requestMap.clear()
    }

    private fun handleRequest(target: T) {
        try {
            val url = requestMap[target] ?: return

            val bitmapBytes: ByteArray = FlickrFetchr().getUrlBytes(url)
            val bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.size)

            responseHandler.post {
                if (requestMap[target] !== url || hasQuit)
                    return@post

                requestMap.remove(target)
                onThumbnailDownloaded(target, bitmap)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error downloading image", e)
        }
    }
}