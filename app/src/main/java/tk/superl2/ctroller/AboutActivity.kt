package tk.superl2.ctroller

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.widget.TextView
import eu.chainfire.libsuperuser.Shell
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException


class AboutActivity : AppCompatActivity() {
    private var is64bit: Boolean = false;

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

        val versionTextView = findViewById<TextView>(R.id.versions_textview)
        versionTextView.text = Html.fromHtml("<b>App:</b> ${BuildConfig.VERSION_NAME}<br><b>CTROLLER version:</b> Getting version, please wait...")
        GetCtrollerVersion(applicationInfo.nativeLibraryDir, versionTextView, is64bit).execute()
    }
}

@SuppressLint("StaticFieldLeak")
class GetCtrollerVersion(private val nativeLibraryDir: String, private val versionTextView: TextView, private val is64bit: Boolean): AsyncTask<Unit, Unit, String>() {
    override fun doInBackground(vararg params: Unit): String {
        return Shell.SU.run("$nativeLibraryDir/lib_ctroller_.so --version")[0]
    }

    override fun onPostExecute(result: String?) {
        versionTextView.text = Html.fromHtml("<b>App:</b> ${BuildConfig.VERSION_NAME}<br><b>CTROLLER version:</b> $result ${if (is64bit) "(ARM64)" else "(ARM)"}")
    }
}
