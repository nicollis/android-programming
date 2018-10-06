package digital.ollis.android.photogallery

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

class PhotoFragment: Fragment() {

    private lateinit var itemImageView: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.item_gallery, container, false)

        itemImageView = view.findViewById(R.id.item_image_view)

        return view
    }
}