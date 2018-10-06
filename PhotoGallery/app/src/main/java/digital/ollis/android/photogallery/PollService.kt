package digital.ollis.android.photogallery

import android.app.IntentService
import android.content.Context
import android.content.Intent

private const val TAG = "PollService"

class PollService : IntentService(TAG) {

    override fun onHandleIntent(intent: Intent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, PollService::class.java)
    }
}