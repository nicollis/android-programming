package digital.ollis.android.criminalintent

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import javax.security.auth.callback.Callback

private const val SAVED_SUBTITLE_VISIBLE = "subtitle"

class CrimeListFragment : Fragment() {

    private lateinit var crimeRecyclerView: RecyclerView
    private var adapter: CrimeAdapter? = null
    private var subtitleVisible: Boolean = false
    private var callbacks: Callbacks? = null

    interface Callbacks {
        fun onCrimeSelected(crime: Crime)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view : RecyclerView = inflater.inflate(R.layout.fragment_crime_list, container, false) as RecyclerView

        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view)

        crimeRecyclerView.layoutManager = LinearLayoutManager(context)

        subtitleVisible = savedInstanceState?.getBoolean(SAVED_SUBTITLE_VISIBLE) ?: false

        updateUI()

        return view
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, subtitleVisible)
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.fragment_crime_list, menu)

        val subTitleItem = menu?.findItem(R.id.show_subtitle)
        subTitleItem?.title = if (subtitleVisible)
            getString(R.string.hide_subtitle)
        else
            getString(R.string.show_subtitle)
    }

    fun updateUI() {
        val crimeLab = CrimeLab.get()
        val crimes = crimeLab.getCrimes()

        adapter?.let {
            it.crimes = crimes
            it.notifyDataSetChanged()
        } ?: run {
            adapter = CrimeAdapter(crimes)
            crimeRecyclerView.adapter = adapter
        }

        updateSubtitle()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.new_crime -> {
                val crime = Crime()
                CrimeLab.get().addCrime(crime)
                updateUI()
                callbacks?.onCrimeSelected(crime)
                return true
            }
            R.id.show_subtitle -> {
                subtitleVisible = !subtitleVisible
                activity?.invalidateOptionsMenu()
                updateSubtitle()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun updateSubtitle() {
        val crimeLab = CrimeLab.get()
        val crimeCount = crimeLab.getCrimes().size
        val subtitle = if (subtitleVisible)
            getString(R.string.subtitle_format, crimeCount)
        else
            ""

        val activity = activity as AppCompatActivity
        activity.supportActionBar?.subtitle = subtitle
    }

    private inner class CrimeHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private lateinit var crime: Crime

        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = this.crime.title
            dateTextView.text = this.crime.date.toString()

            solvedImageView.visibility = if (crime.isSolved)
                View.VISIBLE
            else
                View.GONE
        }

        override fun onClick(v: View) {
            callbacks?.onCrimeSelected(crime)
        }
    }

    private inner class CrimeAdapter(var crimes: List<Crime>) : RecyclerView.Adapter<CrimeHolder>() {

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): CrimeHolder {
            val layoutInflater = LayoutInflater.from(context)
            val view = layoutInflater.inflate(R.layout.list_item_crime, p0, false)
            return CrimeHolder(view)
        }

        override fun onBindViewHolder(p0: CrimeHolder, p1: Int) {
            val crime = crimes[p1]
            p0.bind(crime)
        }

        override fun getItemCount(): Int {
            return crimes.size
        }
    }
}