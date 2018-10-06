package digital.ollis.android.criminalintent

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.widget.DatePicker
import java.util.*

private const val ARG_DATE = "date"

class DatePickerFragment : DialogFragment() {

    private lateinit var datePicker: DatePicker

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val date = arguments?.getSerializable(ARG_DATE) as Date

        val calendar = Calendar.getInstance().apply {
            time = date
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_date, null)

        datePicker = view.findViewById(R.id.dialog_date_picker)
        datePicker.init(year, month, day, null)

        return AlertDialog.Builder(requireContext())
                .setView(view)
                .setTitle(R.string.date_picker_title)
                .setPositiveButton(android.R.string.ok) { _,_ ->
                    val resultDate = GregorianCalendar(datePicker.year,
                            datePicker.month, datePicker.dayOfMonth).time
                    sendResult(Activity.RESULT_OK, resultDate)
                }
                .create()
    }

    private fun sendResult(resultCode: Int, date: Date) {
        targetFragment?.let {
            val intent = Intent().apply {
                putExtra(EXTRA_DATE, date)
            }
            it.onActivityResult(targetRequestCode, resultCode, intent)
        }
    }

    companion object {
        const val EXTRA_DATE = "com.bignerdranch.android.criminalintent.date"

        fun newInstance(date: Date): DatePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
            }

            return DatePickerFragment().apply {
                arguments = args
            }
        }
    }
}