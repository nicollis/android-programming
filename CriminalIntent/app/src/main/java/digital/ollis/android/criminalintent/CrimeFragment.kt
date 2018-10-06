package digital.ollis.android.criminalintent

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.io.File
import java.util.*

private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0
private const val REQUEST_CONTACT = 1
private const val REQUEST_PHOTO = 2

class CrimeFragment : Fragment() {
    private lateinit var crime: Crime
    private lateinit var photoFile: File

    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView

    private var callbacks: Callbacks? = null

    interface Callbacks {
        fun onCrimeUpdates(crime: Crime)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val crimeId = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crime = CrimeLab.get().getCrime(crimeId) ?: Crime()
        photoFile = CrimeLab.get().getPhotoFile(requireContext(), crime)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.fragment_chrime, container, false)

        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton
        photoView = view.findViewById(R.id.crime_photo) as ImageView

        val titleWatcher = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Intentionally left blank
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                crime.title = s.toString()
                updateCrime()
            }

            override fun afterTextChanged(s: Editable?) {
                // Intentionally left blank
            }
        }

        titleField.apply {
            setText(crime.title)
            addTextChangedListener(titleWatcher)
        }

        dateButton.apply {
           updateDate()
            dateButton.setOnClickListener {
                DatePickerFragment.newInstance(crime.date).apply {
                    setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                    val fragmentManager = this@CrimeFragment.fragmentManager
                    show(fragmentManager, DIALOG_DATE)
                }
            }
        }

        solvedCheckBox.apply {
            isChecked = crime.isSolved
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
                updateCrime()
            }
        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plan"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }.also {
               startActivity(
                       Intent.createChooser(it, getString(R.string.send_report ))
               )
            }
        }

        suspectButton.apply {
            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)

            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }

            if (crime.suspect.isNotEmpty()) {
                text = crime.suspect
            }

            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(pickContactIntent,
                    PackageManager.MATCH_DEFAULT_ONLY)

            if (resolvedActivity == null)
                isEnabled = false
        }

        photoButton.apply {
            val packageManager = requireActivity().packageManager

            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(captureImage, PackageManager.MATCH_DEFAULT_ONLY)

            if (resolvedActivity == null) isEnabled = false

            setOnClickListener {
                val uri = FileProvider.getUriForFile(requireContext(),
                        "digital.ollis.android.criminalintent.fileprovider", photoFile)
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri)

                val cameraActivities: List<ResolveInfo> =
                        packageManager.queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY)
                cameraActivities.forEach {
                    requireActivity().grantUriPermission(
                            it.activityInfo.packageName,
                            uri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }

                startActivityForResult(captureImage, REQUEST_PHOTO)
            }
        }

        updatePhotoView()

        return view
    }

    override fun onPause() {
        super.onPause()
        CrimeLab.get().updateCrime(crime)
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private fun updateCrime() {
        CrimeLab.get().updateCrime(crime)
        callbacks?.onCrimeUpdates(crime)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return
            requestCode == REQUEST_DATE && data != null -> {
                crime.date = data.getSerializableExtra(DatePickerFragment.EXTRA_DATE) as Date
                updateCrime()
                updateDate()
            }

            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri = data.data
                val queryFields: Array<String> = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                val cursor = requireActivity().contentResolver
                        .query(contactUri, queryFields, null, null, null)

                cursor?.use {
                    if (it.count == 0) return

                    it.moveToFirst()
                    crime.suspect = it.getString(0)
                    updateCrime()
                    suspectButton.text = crime.suspect
                }
            }

            requestCode == REQUEST_PHOTO -> {
                val uri = FileProvider.getUriForFile(requireActivity(),
                        "digital.ollis.android.criminalintent.fileprovider",
                        photoFile)

                requireActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                updateCrime()
                updatePhotoView()
            }
        }
    }

    private fun updateDate() {
        dateButton.text = crime.date.toString()
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved)
            getString(R.string.crime_report_solved)
        else
            getString(R.string.crime_report_unsolved)

        val dateString = DateFormat.format("EEE, MMM, dd", crime.date).toString()
        val suspect = if (crime.suspect.isEmpty())
            getString(R.string.crime_report_no_suspect)
        else
            getString(R.string.crime_report_suspect)

        val report = getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)

        return report
    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            photoView.setImageBitmap(getScaledBitmap(photoFile.path, requireActivity()))
            photoView.contentDescription = getString(R.string.crime_photo_image_description)
        } else {
            photoView.setImageDrawable(null)
            photoView.contentDescription = getString(R.string.crime_photo_no_image_description)
        }
    }

    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }

            return CrimeFragment().apply {
                arguments = args
            }
        }
    }
}