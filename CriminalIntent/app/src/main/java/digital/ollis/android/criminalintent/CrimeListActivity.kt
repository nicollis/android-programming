package digital.ollis.android.criminalintent

import android.support.v4.app.Fragment
import android.widget.FrameLayout

class CrimeListActivity : SingleFragmentActivity(),
        CrimeListFragment.Callbacks, CrimeFragment.Callbacks {
    override fun createFragment(): Fragment {
        return CrimeListFragment()
    }

    override fun getLayoutResId() = R.layout.activity_masterdetail

    override fun onCrimeSelected(crime: Crime) {
        if(findViewById<FrameLayout>(R.id.detail_fragment_container) == null)
            startActivity(CrimePagerActivity.newIntent(this, crime.id))
        else
            supportFragmentManager.beginTransaction()
                    .replace(R.id.detail_fragment_container, CrimeFragment.newInstance(crime.id))
                    .commit()
    }

    override fun onCrimeUpdates(crime: Crime) {
        val listFragment = supportFragmentManager
                .findFragmentById(R.id.fragment_container) as CrimeListFragment
        listFragment.updateUI()
    }
}