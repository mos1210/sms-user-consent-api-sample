package com.mos1210.android.example.smsuserconsent

import android.app.Activity
import android.content.*
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
        private const val REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(smsVerificationReceiver, intentFilter)

        SmsRetriever.getClient(this).startSmsUserConsent(null)
    }

    override fun onPause() {
        super.onPause()

        unregisterReceiver(smsVerificationReceiver)
    }

    private val smsVerificationReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action
                && intent.extras != null
            ) {
                val status = intent.extras?.get(SmsRetriever.EXTRA_STATUS) as Status
                when (status.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        val consentIntent =
                            intent.extras?.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                        try {
                            startActivityForResult(consentIntent, REQUEST_CODE)
                        } catch (e: ActivityNotFoundException) {
                            e.printStackTrace()
                        }
                    }
                    CommonStatusCodes.TIMEOUT -> {
                        Log.d(TAG, "timeout")
                    }
                }
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

            REQUEST_CODE ->
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE) ?: return
                    val oneTimeCode = parseOneTimeCode(message)

                    findViewById<EditText>(R.id.editTextVerificationCode).setText(oneTimeCode)
                }
        }
    }

    private fun parseOneTimeCode(message: String): String {
        return message.split("\n")[0].split(":")[1]
    }
}