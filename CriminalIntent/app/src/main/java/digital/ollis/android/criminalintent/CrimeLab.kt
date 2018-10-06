package digital.ollis.android.criminalintent

import android.content.ContentValues
import android.content.Context
import java.io.File
import java.util.*

class CrimeLab private constructor(context: Context) {

    private val database = CrimeBaseHalper(context).writableDatabase

    fun getCrimes(): List<Crime> {
        val crimes = mutableListOf<Crime>()
        val cursor = queryCrimes(null, null)
        cursor.use {
            it.moveToFirst()
            while (!it.isAfterLast) {
                crimes.add(it.getCrime())
                it.moveToNext()
            }
        }

        return crimes
    }

    fun getCrime(id: UUID): Crime? {
        val cursor = queryCrimes("${CrimeTable.Cols.UUID} = ?", arrayOf(id.toString()))

        cursor.use {
            if (it.count == 0) {
                return null
            }

            cursor.moveToFirst()
            return cursor.getCrime()
        }
    }

    fun getPhotoFile(context: Context, crime: Crime): File {
        val fileDir = context.applicationContext.filesDir
        return File(fileDir, crime.photoFileName)
    }

    private fun getContentValues(crime: Crime) =
            ContentValues().apply {
                put(CrimeTable.Cols.UUID, crime.id.toString())
                put(CrimeTable.Cols.TITLE, crime.title)
                put(CrimeTable.Cols.DATE, crime.date.time)
                put(CrimeTable.Cols.SOLVED, crime.isSolved.toInt())
                put(CrimeTable.Cols.SUSPECT, crime.suspect)
            }

    fun addCrime(crime: Crime) {
        database.insert(CrimeTable.NAME, null, getContentValues(crime))
    }

    fun updateCrime(crime: Crime) {
        val uuidString = crime.id.toString()
        val values = getContentValues(crime)

        database.update(CrimeTable.NAME, values, "${CrimeTable.Cols.UUID} = ?", arrayOf(uuidString))
    }

    fun queryCrimes(whereClause: String?, whereArgs: Array<String>?): CrimeCursorWrapper {
        val cursor = database.query(
                CrimeTable.NAME,
                null, // Columns
                whereClause,
                whereArgs,
                null, // Group by
                null, // having
                null // order by
        )

        return CrimeCursorWrapper(cursor)
    }

    companion object {
        private var INSTANCE: CrimeLab? = null

        fun initialize(context: Context) { INSTANCE = CrimeLab(context) }

        fun get(): CrimeLab =
                INSTANCE ?: throw IllegalStateException("CrimeLab must be initialized")
    }
}