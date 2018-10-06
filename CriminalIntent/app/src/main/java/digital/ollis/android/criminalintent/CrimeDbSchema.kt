package digital.ollis.android.criminalintent

object CrimeTable {
    const val NAME = "crimes"

    object Cols {
        const val UUID = "uuid"
        const val TITLE = "title"
        const val DATE = "date"
        const val SOLVED = "solved"
        const val SUSPECT = "suspect"
    }
}

fun Boolean.toInt() = if (this) 1 else 0