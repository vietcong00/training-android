package com.example.basicqrcode.barcodedetection

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.basicqrcode.R
import com.example.basicqrcode.camera.WorkflowModel
import com.example.basicqrcode.camera.WorkflowModel.WorkflowState
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.android.synthetic.main.barcode_bottom_sheet.view.*
import java.util.*
import kotlin.collections.ArrayList

/** Displays the bottom sheet to present barcode fields contained in the detected barcode.  */
class BarcodeResultFragment : BottomSheetDialogFragment() {

    private var workflowModel: WorkflowModel? = null

    override fun onCreateView(
        layoutInflater: LayoutInflater,
        viewGroup: ViewGroup?,
        bundle: Bundle?
    ): View {
        val view = layoutInflater.inflate(R.layout.barcode_bottom_sheet, viewGroup)

        val arguments = arguments
        val barcodeFieldList: ArrayList<BarcodeField> =
            if (arguments?.containsKey(ARG_BARCODE_FIELD_LIST) == true) {
                arguments.getParcelableArrayList(ARG_BARCODE_FIELD_LIST) ?: ArrayList()
            } else {
                Log.e(TAG, "No barcode field list passed in!")
                ArrayList()
            }

        view.findViewById<RecyclerView>(R.id.barcode_field_recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            adapter = BarcodeFieldAdapter(barcodeFieldList)
        }

        workflowModel = ViewModelProviders.of(this).get(WorkflowModel::class.java)
//        observeData()
        return view
    }

    override fun onDismiss(dialogInterface: DialogInterface) {
        activity?.let {
            // Back to working state after the bottom sheet is dismissed.
            ViewModelProviders.of(it).get(WorkflowModel::class.java)
                .setWorkflowState(WorkflowState.DETECTING)
        }
        super.onDismiss(dialogInterface)
    }

    companion object {

        private const val TAG = "BarcodeResultFragment"
        private const val ARG_BARCODE_FIELD_LIST = "arg_barcode_field_list"

        fun show(
            fragmentManager: FragmentManager,
            typeQR: String,
            barcodeFieldArrayList: ArrayList<BarcodeField>
        ) {
            val barcodeResultFragment = BarcodeResultFragment()
            barcodeResultFragment.arguments = Bundle().apply {
                putParcelableArrayList(ARG_BARCODE_FIELD_LIST, barcodeFieldArrayList)
            }
            barcodeResultFragment.show(fragmentManager, TAG)
        }

        fun dismiss(fragmentManager: FragmentManager) {
            (fragmentManager.findFragmentByTag(TAG) as BarcodeResultFragment?)?.dismiss()
        }
    }

    fun observeData() {
        workflowModel?.detectedBarcode?.observe(viewLifecycleOwner, Observer { barcode ->
            if (barcode != null) {
                val barcodeFieldList = java.util.ArrayList<BarcodeField>()
                val valueType = barcode.valueType
                var valueName = ""
                var nameButtonAction = ""
                when (valueType) {
                    // QR Code type Email
                    Barcode.TYPE_EMAIL -> {
                        val rawValue = barcode.email!!
                        nameButtonAction = "Open Email"
                        barcodeFieldList.add(BarcodeField("Type QR Code", "Email"))
                        barcodeFieldList.add(BarcodeField("Address", rawValue.address ?: ""))
                        barcodeFieldList.add(BarcodeField("Subject", rawValue.subject ?: ""))
                        barcodeFieldList.add(BarcodeField("Body", rawValue.body ?: ""))

                        /* Create the Intent */
                        val emailIntent = Intent(Intent.ACTION_SEND);

                        /* Fill it with Data */
                        emailIntent.type = "plain/text";
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(rawValue.address));
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, rawValue.subject);
                        emailIntent.putExtra(Intent.EXTRA_TEXT, rawValue.body);

                        /* Send it off to the Activity-Chooser */
                        this.startActivity(Intent.createChooser(emailIntent, "Open With"));
                    }

                    // QR Code type Message
                    Barcode.TYPE_SMS -> {
                        val rawValue = barcode.sms!!
                        Log.i("tesss", "sms: " + barcode.rawValue)
                        barcodeFieldList.add(BarcodeField("Type QR Code", "Event"))
                        barcodeFieldList.add(
                            BarcodeField(
                                "Phone Number",
                                rawValue.phoneNumber ?: ""
                            )
                        )
                        barcodeFieldList.add(BarcodeField("Message", rawValue.message ?: ""))

                        val smsIntent = Intent(Intent.ACTION_VIEW, Uri.parse(barcode.rawValue))
                        smsIntent.type = "vnd.android-dir/mms-sms"
                        smsIntent.putExtra("address", rawValue.phoneNumber)
                        smsIntent.putExtra("sms_body", rawValue.message)
                        startActivity(smsIntent);

                    }

                    // QR Code type Contact
                    Barcode.TYPE_CONTACT_INFO -> {
                        val rawValue = barcode.contactInfo!!
                        Log.i("tesss", "Contact: " + barcode.rawValue)
                        barcodeFieldList.add(BarcodeField("Type QR Code", "Contact"))
                        barcodeFieldList.add(
                            BarcodeField(
                                "Name",
                                rawValue.name!!.formattedName ?: ""
                            )
                        )
                        barcodeFieldList.add(BarcodeField("Phone", rawValue.phones[0].number ?: ""))
                        barcodeFieldList.add(BarcodeField("Web1", rawValue.urls[0] ?: ""))
                        barcodeFieldList.add(BarcodeField("Web2", rawValue.urls[1] ?: ""))
                    }

                    // QR Code type URL, Facebook, Twitter, Instagram
                    Barcode.TYPE_URL -> {
                        val rawValue = barcode.url!!
                        Log.i("tesss", "URL: " + barcode.rawValue)
                        barcodeFieldList.add(BarcodeField("Type QR Code", "URL"))
                        barcodeFieldList.add(BarcodeField("Title", rawValue.title ?: ""))
                        barcodeFieldList.add(BarcodeField("URL", rawValue.url ?: ""))

                        try {
                            val applicationInfo: ApplicationInfo =
                                requireActivity().packageManager.getApplicationInfo("com.facebook.katana", 0)
                            if (applicationInfo.enabled) {
                                var uri = "fb://facewebmodal/f?href=" + rawValue.url
                                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                                this.startActivity(Intent.createChooser(webIntent, "Open With"));
                            }
                        } catch (ignored: PackageManager.NameNotFoundException) {
                            val webIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(rawValue.url)
                            )
                            this.startActivity(Intent.createChooser(webIntent, "Open With"));
                        }
                    }

                    // QR Code type Wifi
                    Barcode.TYPE_WIFI -> {
                        val rawValue = barcode.wifi!!
                        Log.i("tesss", "Wifi: " + barcode.rawValue)
                        barcodeFieldList.add(BarcodeField("Type QR Code", "WiFi"))
                        barcodeFieldList.add(BarcodeField("SSID", rawValue.ssid ?: ""))
                        barcodeFieldList.add(BarcodeField("Password", rawValue.password ?: ""))
                        barcodeFieldList.add(
                            BarcodeField(
                                "Encryption Type",
                                rawValue.encryptionType.toString() ?: ""
                            )
                        )

                    }

                    // QR Code type Location
                    Barcode.TYPE_GEO -> {
                        val rawValue = barcode.geoPoint!!
                        Log.i("tesss", "Geo: " + barcode.rawValue)
                        barcodeFieldList.add(BarcodeField("Type QR Code", "Geo"))
                        barcodeFieldList.add(BarcodeField("Lat", rawValue.lat.toString() ?: ""))
                        barcodeFieldList.add(BarcodeField("Lng", rawValue.lng.toString() ?: ""))
                    }

                    // QR Code type Event
                    Barcode.TYPE_CALENDAR_EVENT -> {
                        val rawValue = barcode.calendarEvent!!
                        Log.i("tesss", "event: " + barcode.rawValue)
                        barcodeFieldList.add(BarcodeField("Type QR Code", "Event"))
                        barcodeFieldList.add(BarcodeField("Summary", rawValue.summary ?: ""))
                        barcodeFieldList.add(
                            BarcodeField(
                                "Description",
                                rawValue.description ?: ""
                            )
                        )
                        barcodeFieldList.add(BarcodeField("location", rawValue.location ?: ""))
                        barcodeFieldList.add(
                            BarcodeField(
                                "Start Day",
                                rawValue.start!!.day.toString() ?: ""
                            )
                        )
                        barcodeFieldList.add(
                            BarcodeField(
                                "End Day",
                                rawValue.end!!.day.toString() ?: ""
                            )
                        )
                        val start = rawValue.start!!
                        val end = rawValue.end!!

                        val intent = Intent(
                            Intent.ACTION_INSERT,
                            CalendarContract.Events.CONTENT_URI
                        ).apply {
                            val beginTime: Calendar = Calendar.getInstance().apply {
                                set(
                                    start.year,
                                    start.month - 1,
                                    start.day,
                                    start.hours,
                                    start.minutes
                                )
                            }
                            val endTime = Calendar.getInstance().apply {
                                set(end.year, end.month - 1, end.day, end.hours, end.minutes)
                            }
                            putExtra(
                                CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                beginTime.timeInMillis
                            )
                            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.timeInMillis)
                            putExtra(CalendarContract.Events.TITLE, rawValue.summary)
                            putExtra(CalendarContract.Events.EVENT_LOCATION, rawValue.location)
                        }

                        startActivity(intent)
                    }

                    // QR Code type Text
                    Barcode.TYPE_TEXT -> {
                        val rawValue = barcode.rawValue!!
                        Log.i("tesss", "Text: " + barcode.rawValue)
                        barcodeFieldList.add(BarcodeField("Type QR Code", "Text"))
                        barcodeFieldList.add(BarcodeField("Note", rawValue ?: ""))
                    }
                }
                show(requireActivity().supportFragmentManager, valueName, barcodeFieldList)
            }
        })
    }
}
