package tk.superl2.ctroller

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import eu.chainfire.libsuperuser.Shell


class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main_options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true;
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private lateinit var libDir: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.start_button).setOnClickListener(this)

        findViewById<TextView>(R.id.ip_message).text = "";
    }

    override fun onClick(v: View) {
        if (findViewById<Button>(R.id.start_button).text == "START CTROLLER") {
            Start().execute(applicationInfo.nativeLibraryDir)
            findViewById<Button>(R.id.start_button).text = "STOP CTROLLER"
        } else if (findViewById<Button>(R.id.start_button).text == "STOP CTROLLER") {
            Shell.SU.run("echo \$(ps | grep lib_ctroller_.so) | cut -d' ' -f2 | xargs kill")
            findViewById<Button>(R.id.start_button).text = "START CTROLLER"
        }
    }
}

private class Start : AsyncTask<String, Void, Void>() {
    override fun doInBackground(vararg params: String): Void? {
        Shell.SU.run("${params[0]}/lib_ctroller_.so")
        return null
    }
}