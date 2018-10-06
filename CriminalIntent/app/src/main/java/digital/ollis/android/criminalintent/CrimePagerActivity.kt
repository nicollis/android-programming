package digital.ollis.android.criminalintent

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import java.util.*

private const val EXTRA_CRIME_ID = "com.bignerdranch.android.criminalintent.crime_id"

class CrimePagerActivity : AppCompatActivity(), CrimeFragment.Callbacks {

    private lateinit var viewPager: ViewPager
    private lateinit var crimes: List<Crime>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crime_pager)

        val crimeId = intent.getSerializableExtra(EXTRA_CRIME_ID) as UUID

        viewPager = findViewById(R.id.crime_view_pager)
        viewPager.apply {
            crimes = CrimeLab.get().getCrimes()
            adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
                override fun getItem(p0: Int): Fragment {
                    val crime = crimes[p0]
                    return CrimeFragment.newInstance(crime.id)
                }

                override fun getCount(): Int = crimes.size
            }

            currentItem = crimes.indexOfFirst { it.id == crimeId }
        }
    }

    override fun onCrimeUpdates(crime: Crime) {
    }

    companion object {
        fun newIntent(context: Context, crimeId: UUID): Intent {
            return Intent(context, CrimePagerActivity::class.java).apply {
                putExtra(EXTRA_CRIME_ID, crimeId)
            }
        }
    }
}