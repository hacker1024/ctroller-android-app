package tk.superl2.ctroller

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import eu.chainfire.libsuperuser.Shell

private lateinit var prefs: SharedPreferences

class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main_options_menu, menu)
        return true
    }

    // When an item from the menu is selected, start the right activity.
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true;
            }
            R.id.settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true;
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private lateinit var startButton: TextView;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get the shared preferences object
        prefs = getSharedPreferences("keymap", Context.MODE_PRIVATE)

        startButton = findViewById<Button>(R.id.start_button)
        // Set this class as an onClickListener for the start button.
        startButton.setOnClickListener(this)

        findViewById<TextView>(R.id.ip_message).text = "";

        // Check if CTROLLER is already running.
        if (!Shell.SU.run("ps | grep lib_ctroller_.so").isEmpty()) startButton.text = "STOP CTROLLER"
    }

    override fun onClick(v: View) {
        // If the button says "START CTROLLER", run the ctroller executable (lib_ctroller_.so) from the native library dir as root in an asynctask.
        if (findViewById<Button>(R.id.start_button).text == "START CTROLLER") {
            Start().execute(applicationInfo.nativeLibraryDir, filesDir.absolutePath + "/keymap.txt")
            startButton.text = "STOP CTROLLER"
        // If not, kill ctroller.
        } else if (findViewById<Button>(R.id.start_button).text == "STOP CTROLLER") {
            Shell.SU.run("echo \$(ps | grep lib_ctroller_.so) | cut -d' ' -f2 | xargs kill")
            startButton.text = "START CTROLLER"
        }
    }
}

private class Start : AsyncTask<String, Unit, Unit>() {
    lateinit var ctrollerLog: List<String>
    override fun doInBackground(vararg params: String) {
        if (prefs.getBoolean("keymapEnabled", false)) {
            ctrollerLog = Shell.SU.run("${params[0]}/lib_ctroller_.so --keymap ${params[1]}")
        } else {
            Shell.SU.run("${params[0]}/lib_ctroller_.so")
        }
    }
}