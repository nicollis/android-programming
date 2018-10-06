package digital.ollis.android.photogallery

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class PhotoGalleryActivity : SingleFragmentActivity(), PhotoGalleryFragment.Callbacks {

    override fun createFragment() = PhotoGalleryFragment.newInstance()

    
}
