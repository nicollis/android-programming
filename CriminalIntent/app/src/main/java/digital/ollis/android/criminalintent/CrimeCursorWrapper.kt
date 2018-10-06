package digital.ollis.android.criminalintent

import android.database.Cursor
import android.database.CursorWrapper
import java.util.*

class CrimeCursorWrapper(cursor: Cursor) : CursorWrapper(cursor) {
    fun getCrime(): Crime {
        val uuidString = getString(getColumnIndex(CrimeTable.Cols.UUID))

        return Crime(UUID.fromString(uuidString)).apply {
            title = getString(getColumnIndex(CrimeTable.Cols.TITLE))
            date = Date(getLong(getColumnIndex(CrimeTable.Cols.DATE)))
            isSolved = getInt(getColumnIndex(CrimeTable.Cols.SOLVED)) != 0
            suspect = getString(getColumnIndex(CrimeTable.Cols.SUSPECT))
        }
    }
}