package ae.motf.nfcreaderandroid

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

class MainActivity : ComponentActivity() {
    private lateinit var nfcAdapter: NfcAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        setContent {
            NFCReaderScreen(
                enableNfcDispatch = this::enableNfcForegroundDispatch,
                disableNfcDispatch = this::disableNfcForegroundDispatch
            )
        }
    }

    override fun onResume() {
        super.onResume()
        val intent = intent
        val action = intent.action
        enableNfcForegroundDispatch()
        /*Log.d("TAG", "action = $action")
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == action) {
            val nfcMessage: NdefMessage? =
                intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                    ?.get(0) as NdefMessage?
            val nfcRecord: NdefRecord? = nfcMessage?.records?.get(0)
            val payload = String(nfcRecord?.payload ?: byteArrayOf())

            Log.d("TAG", "payload: $payload")
            // Handle the received NFC message here
            // Update the nfcMessage state or invoke a callback
        }*/
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("TAG", "onNewIntent intent.action: ${intent.action} ${intent.extras}")
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            val nfcMessage: NdefMessage? = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.get(0) as NdefMessage?
            val nfcRecord: NdefRecord? = nfcMessage?.records?.get(0)
            val payload: String = String(nfcRecord?.payload ?: byteArrayOf())
            Log.d("TAG", "onNewIntent payload: $payload")
            // Handle the received NFC message here
        }
        // Prevent the default system dialog from appearing
        nfcAdapter.setNdefPushMessage(null, this)
    }

    private fun enableNfcForegroundDispatch() {
        // Enable NFC foreground dispatch and handle NFC intent
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val filters = arrayOf(IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED))
        val techLists = arrayOf<Array<String>>()

        nfcAdapter.enableReaderMode(this,
            { tag ->
                val hexString = tag?.id?.joinToString("") { byte ->
                    String.format("%02X", byte) // Convert each byte to a two-digit hexadecimal representation
                }
                Log.d("TAG", "onTagDiscovered: $hexString")
            }, NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NFC_F or
                NfcAdapter.FLAG_READER_NFC_V or
                NfcAdapter.FLAG_READER_NFC_BARCODE or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK or
                NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
            Bundle()
        )
//        nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, techLists)
    }

    private fun disableNfcForegroundDispatch() {
        // Disable NFC foreground dispatch
        nfcAdapter.disableForegroundDispatch(this)
    }

}

@Composable
fun NFCReaderScreen(
    enableNfcDispatch: () -> Unit,
    disableNfcDispatch: () -> Unit
) {
    val nfcMessage = remember { mutableStateOf("") }
    val isNfcReadingEnabled = remember { mutableStateOf(false) }

    Surface(color = MaterialTheme.colorScheme.background) {
        Column {
            Text(text = "NFC Message: ${nfcMessage.value}")

            Button(onClick = { isNfcReadingEnabled.value = true }) {
                Text(text = "Start NFC Reading")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            disableNfcDispatch()
        }
    }
/*
    SideEffect {
        if (isNfcReadingEnabled.value) {
            enableNfcDispatch()
        } else {
            disableNfcDispatch()
        }
    }*/
}
