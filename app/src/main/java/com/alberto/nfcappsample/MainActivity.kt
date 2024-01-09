package com.alberto.nfcappsample

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.alberto.nfcappsample.ui.theme.NFCAppSampleTheme
import java.util.Arrays

class MainActivity : ComponentActivity() {

    private lateinit var pendingIntent: PendingIntent
    private lateinit var readFilters: Array<IntentFilter>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)

            val urlFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
            urlFilter.addDataScheme("http")
            urlFilter.addDataAuthority("google.com", null)

            val textFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED, "text/plain")

            readFilters = arrayOf(urlFilter, textFilter)

            initUI()
        } catch (e: IntentFilter.MalformedMimeTypeException) {
            e.printStackTrace()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        initUI()
    }

    override fun onResume() {
        super.onResume()
        enableRead(pendingIntent, readFilters)
    }

    override fun onPause() {
        super.onPause()
        disableRead()
    }

    private fun enableRead(pendingIntent: PendingIntent, readFilters: Array<IntentFilter>) {
        NfcAdapter.getDefaultAdapter(this)
            .enableForegroundDispatch(this, pendingIntent, readFilters, null)
    }

    private fun disableRead() {
        NfcAdapter.getDefaultAdapter(this)
            .disableForegroundDispatch(this)
    }

    private fun initUI() {
        setContent {
            NFCAppSampleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProcessNFC(intent)
                }
            }
        }
    }

    @Composable
    private fun ProcessNFC(intent: Intent) {
        intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.forEach { ndefMessage ->
            (ndefMessage as NdefMessage).records.forEach { ndefRecord ->
                when (ndefRecord.tnf) {
                    NdefRecord.TNF_WELL_KNOWN -> {
                        val text = "WELL KNOWN: "
                        if (Arrays.equals(ndefRecord.type, NdefRecord.RTD_TEXT)) {
                            text.plus("TEXT: ${ndefRecord.payload}")
                        } else if (Arrays.equals(ndefRecord.type, NdefRecord.RTD_URI)) {
                            text.plus("URI: ${ndefRecord.payload}")
                        }
                        Message(text)
                    }
                }
            }
        }
    }
}

@Composable
fun Message(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun MessagePreview() {
    NFCAppSampleTheme {
        Message("Android")
    }
}