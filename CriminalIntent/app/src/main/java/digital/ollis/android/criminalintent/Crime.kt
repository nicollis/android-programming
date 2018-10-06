package digital.ollis.android.criminalintent

import java.util.*

data class Crime(val id: UUID = UUID.randomUUID(),
                 var title: String = "",
                 var date: Date = Date(),
                 var isSolved: Boolean = false,
                 var suspect: String = "") {
    val photoFileName: String
        get() = "IMG_$id.jpg"
}