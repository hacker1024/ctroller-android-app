package tk.superl2.ctroller

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.widget.TextView
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException


class AboutActivity : AppCompatActivity() {
    var is64bit: Boolean = false;
    private val CTROLLER_BINARY_VERSION_ARM = "android-0.1.0 (ARM)"
    private val CTROLLER_BINARY_VERSION_ARM64 = "android-0.1.1 (ARM64)"
    private lateinit var CTROLLER_BINARY_VERSION: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        try {
            val localBufferedReader = BufferedReader(FileReader("/proc/cpuinfo"))
            if (localBufferedReader.readLine().contains("AArch64", true)) {
                is64bit = true
            }
            localBufferedReader.close()
        } catch (e: IOException) {
            e.printStackTrace()
            is64bit = true
        }

        CTROLLER_BINARY_VERSION = when (is64bit) {true -> CTROLLER_BINARY_VERSION_ARM64; false -> CTROLLER_BINARY_VERSION_ARM}

        val versionTextTiew = findViewById<TextView>(R.id.textView4)
        versionTextTiew.text = Html.fromHtml("<b>App:</b> ${BuildConfig.VERSION_NAME}<br><b>CTROLLER Version:</b> $CTROLLER_BINARY_VERSION")
    }
}
