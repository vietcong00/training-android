package com.example.basicqrcode

import android.animation.AnimatorSet
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.hardware.Camera
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.basicqrcode.barcodedetection.BarcodeField
import com.example.basicqrcode.barcodedetection.BarcodeProcessor
import com.example.basicqrcode.barcodedetection.BarcodeResultFragment
import com.example.basicqrcode.camera.CameraSource
import com.example.basicqrcode.camera.CameraSourcePreview
import com.example.basicqrcode.camera.GraphicOverlay
import com.example.basicqrcode.camera.WorkflowModel
import com.example.basicqrcode.camera.WorkflowModel.WorkflowState
import com.example.basicqrcode.settings.SettingsActivity
import com.google.android.material.chip.Chip
import com.google.common.base.Objects
import com.google.mlkit.vision.barcode.common.Barcode
import java.io.IOException
import java.util.*


/** Demonstrates the barcode scanning workflow using camera preview.  */
class MainActivity : AppCompatActivity(), OnClickListener {

    private var cameraSource: CameraSource? = null
    private var preview: CameraSourcePreview? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var settingsButton: View? = null
    private var flashButton: View? = null
    private var promptChip: Chip? = null
    private var promptChipAnimator: AnimatorSet? = null
    private var workflowModel: WorkflowModel? = null
    private var currentWorkflowState: WorkflowState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        preview = findViewById(R.id.camera_preview)
        graphicOverlay = findViewById<GraphicOverlay>(R.id.camera_preview_graphic_overlay).apply {
            setOnClickListener(this@MainActivity)
            cameraSource = CameraSource(this)
        }

//        promptChip = findViewById(R.id.bottom_prompt_chip)
//        promptChipAnimator =
//            (AnimatorInflater.loadAnimator(this, R.animator.bottom_prompt_chip_enter) as AnimatorSet).apply {
//                setTarget(promptChip)
//            }

        findViewById<View>(R.id.close_button).setOnClickListener(this)
        flashButton = findViewById<View>(R.id.flash_button).apply {
            setOnClickListener(this@MainActivity)
        }
        settingsButton = findViewById<View>(R.id.settings_button).apply {
            setOnClickListener(this@MainActivity)
        }
        setUpWorkflowModel()
    }

    override fun onResume() {
        super.onResume()

        if (!Utils.allPermissionsGranted(this)) {
            Utils.requestRuntimePermissions(this)
        }
        workflowModel?.markCameraFrozen()
        settingsButton?.isEnabled = true
        currentWorkflowState = WorkflowState.NOT_STARTED
        cameraSource?.setFrameProcessor(BarcodeProcessor(graphicOverlay!!, workflowModel!!))
        workflowModel?.setWorkflowState(WorkflowState.DETECTING)
    }

    override fun onPostResume() {
        super.onPostResume()
        BarcodeResultFragment.dismiss(supportFragmentManager)
    }

    override fun onPause() {
        super.onPause()
        currentWorkflowState = WorkflowState.NOT_STARTED
        stopCameraPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource?.release()
        cameraSource = null
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.close_button -> onBackPressed()
            R.id.flash_button -> {
                flashButton?.let {
                    if (it.isSelected) {
                        it.isSelected = false
                        cameraSource?.updateFlashMode(Camera.Parameters.FLASH_MODE_OFF)
                    } else {
                        it.isSelected = true
                        cameraSource!!.updateFlashMode(Camera.Parameters.FLASH_MODE_TORCH)
                    }
                }
            }
            R.id.settings_button -> {
                settingsButton?.isEnabled = false
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
    }

    private fun startCameraPreview() {
        val workflowModel = this.workflowModel ?: return
        val cameraSource = this.cameraSource ?: return
        if (!workflowModel.isCameraLive) {
            try {
                workflowModel.markCameraLive()
                preview?.start(cameraSource)
            } catch (e: IOException) {
                Log.e(TAG, "Failed to start camera preview!", e)
                cameraSource.release()
                this.cameraSource = null
            }
        }
    }

    private fun stopCameraPreview() {
        val workflowModel = this.workflowModel ?: return
        if (workflowModel.isCameraLive) {
            workflowModel.markCameraFrozen()
            flashButton?.isSelected = false
            preview?.stop()
        }
    }

    private fun setUpWorkflowModel() {
        workflowModel = ViewModelProviders.of(this).get(WorkflowModel::class.java)

        // Observes the workflow state changes, if happens, update the overlay view indicators and
        // camera preview state.
        workflowModel!!.workflowState.observe(this, Observer { workflowState ->
            if (workflowState == null || Objects.equal(currentWorkflowState, workflowState)) {
                return@Observer
            }

            currentWorkflowState = workflowState
            Log.d(TAG, "Current workflow state: ${currentWorkflowState!!.name}")

            val wasPromptChipGone = promptChip?.visibility == View.GONE

            when (workflowState) {
                WorkflowState.DETECTING -> {
                    promptChip?.visibility = View.VISIBLE
                    promptChip?.setText(R.string.prompt_point_at_a_barcode)
                    startCameraPreview()
                }
                WorkflowState.DETECTED -> {
                    promptChip?.visibility = View.GONE
                    stopCameraPreview()
                }
                else -> promptChip?.visibility = View.GONE
            }

            val shouldPlayPromptChipEnteringAnimation =
                wasPromptChipGone && promptChip?.visibility == View.VISIBLE
            promptChipAnimator?.let {
                if (shouldPlayPromptChipEnteringAnimation && !it.isRunning) it.start()
            }
        })

        workflowModel?.detectedBarcode?.observe(this, Observer { barcode ->
            if (barcode != null) {
//            if (false) {
                val barcodeFieldList = ArrayList<BarcodeField>()
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

                        //Open Email app
                        val emailIntent = Intent(Intent.ACTION_SEND);

                        emailIntent.type = "plain/text";
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(rawValue.address));
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, rawValue.subject);
                        emailIntent.putExtra(Intent.EXTRA_TEXT, rawValue.body);

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

                        //Open SMS app
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

                        //Open Contact app
                        val smsIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                        startActivity(smsIntent);
                    }

                    // QR Code type URL, Facebook, Twitter, Instagram
                    Barcode.TYPE_URL -> {
                        val rawValue = barcode.url!!
                        Log.i("tesss", "URL: " + barcode.rawValue)
                        barcodeFieldList.add(BarcodeField("Type QR Code", "URL"))
                        barcodeFieldList.add(BarcodeField("Title", rawValue.title ?: ""))
                        barcodeFieldList.add(BarcodeField("URL", rawValue.url ?: ""))

                        //Open Facebook app, Twitter app, Instagram app or Browser
                        try {
                            val applicationInfo: ApplicationInfo =
                                packageManager.getApplicationInfo("com.facebook.katana", 0)
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

                        // Open Calendar app
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
                BarcodeResultFragment.show(supportFragmentManager, valueName, barcodeFieldList)
            }
        })
    }

    companion object {
        private const val TAG = "LiveBarcodeActivity"
    }
}
