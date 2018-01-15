package tk.superl2.ctroller

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView
import java.io.PrintWriter
import java.util.*
import kotlin.collections.ArrayList


val KEYMAP_LABELS = listOf("A", "B", "X", "Y", "START", "SELECT", "L", "R", "ZL", "ZR", "UP", "DOWN", "LEFT", "RIGHT")
val keymapMappings: MutableList<String?> = arrayOfNulls<String>(14).toMutableList()
val unsavedKeymapChanges = ArrayList<Int>()

val primaryColour = TypedValue()

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var keymapSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Set primary color
        theme.resolveAttribute(R.attr.colorPrimary, primaryColour, true)

        // Add backwards compatible elevation for a shadow
        ViewCompat.setElevation(findViewById(R.id.settings_keymap_header), 10f)

        // Get the shared preferences object
        prefs = getSharedPreferences("keymap", Context.MODE_PRIVATE)

        // Load the key mappings from keymap file
        val keymapFileInput = try {
            openFileInput("keymap.txt")
        } catch (e: java.io.FileNotFoundException) {
            Log.w("ctroller-android", "Keymap file not found. Creating one with default mappings.")
            writeListToKeymapFile(KEYMAP_LABELS, "keymap.txt")
            openFileInput("keymap.txt")
        }
        val scanner = Scanner(keymapFileInput)
        var i = 0
        while (i < keymapMappings.size) {
            keymapMappings[i] = scanner.next()
            i++
        }
        scanner.close()
        keymapFileInput.close()
        Log.d("ctroller-android:", "Keymap file loaded: $keymapMappings")

        // Instantiate the custom adapter, set it as the keymap_list adapter, create an OnItemClickListener, and set it as the keymap_list onItemClickListener
        val itemsAdapter = KeymapListAdapter(this, android.R.layout.simple_expandable_list_item_1, KEYMAP_LABELS, keymapMappings)
        findViewById<ListView>(R.id.keymap_list).adapter = itemsAdapter
        findViewById<ListView>(R.id.keymap_list).onItemClickListener = AdapterView.OnItemClickListener(::onKeymapListClick)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_settings_options_menu, menu)

        // Get the keymap toggle switch
        keymapSwitch = menu!!.getItem(0).actionView.findViewById(R.id.keymap_switch)
        // Check if the keymap is enabled in shared preferences
        keymapSwitch.isChecked = prefs.getBoolean("keymapEnabled", false)
        updateKeymapSwitch(keymapSwitch.isChecked)
        // When the switch is clicked, update it
        keymapSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            val prefsEdit = prefs.edit()
            prefsEdit.putBoolean("keymapEnabled", isChecked)
            prefsEdit.apply()
            updateKeymapSwitch(isChecked)
        }

        return true
    }

    override fun onBackPressed() {
        // maybe add more checks later
        fun keymapValid(keymapList: List<String?>): Boolean {
            return if (keymapList.size == 14) {
                true
            } else {
                Log.wtf("ctroller-android", "Keymap list isn't the right size! Not allowing the keymap to save.")
                false
            }
        }

        if (keymapValid(keymapMappings)) {
            super.onBackPressed()
        }
        else {

        }
    }

    override fun onPause() {
        super.onPause()

        writeListToKeymapFile(keymapMappings, "keymap.txt")
    }

    override fun onStop() {
        super.onStop()

        // Clear ArrayList of unsaved keymaps
        unsavedKeymapChanges.clear()
    }

    private fun updateKeymapSwitch(isChecked: Boolean) {
        if (isChecked) {
            findViewById<TextView>(R.id.message_keymap_untoggled).visibility = View.GONE
            findViewById<TableLayout>(R.id.settings_keymap_layout).visibility = View.VISIBLE
        } else {
            findViewById<TextView>(R.id.message_keymap_untoggled).visibility = View.VISIBLE
            findViewById<TableLayout>(R.id.settings_keymap_layout).visibility = View.GONE
        }
    }

    // This function is called when the keymap list is clicked.
    private fun onKeymapListClick(parent: AdapterView<*>, keymapListView: View, keymapListPosition: Int, id: Long) {
        val builder = AlertDialog.Builder(this)
        var dialog: AlertDialog? = null
        val mAlertView = layoutInflater.inflate(R.layout.choose_key_alert, null)
        val mChooserList = mAlertView.findViewById<ListView>(R.id.keymap_alert_listview)

        fun onChooserListItemClick(chooserParent: AdapterView<*>, chooserView: View, chooserPosition: Int, chooserId: Long) {
            if (keymapMappings[keymapListPosition] != (chooserView as TextView).text as String?) {
                keymapMappings[keymapListPosition] = chooserView.text as String?
                unsavedKeymapChanges.add(keymapListPosition)
            }
            (parent.adapter as ArrayAdapter<*>).notifyDataSetChanged()
            dialog!!.dismiss()
        }

        mChooserList.adapter = KeymapKeyChooserAdapter(this, android.R.layout.simple_expandable_list_item_1, KEYMAP_LABELS, keymapListView.height)
        mChooserList.onItemClickListener = AdapterView.OnItemClickListener(::onChooserListItemClick)
        builder.setView(mAlertView)
        builder.setTitle("Choose button to map \"${KEYMAP_LABELS[keymapListPosition]}\" to. ")
        dialog = builder.create()
        dialog.show()
    }

    private fun writeListToKeymapFile(list: List<String?>, file: String) {
        val pw = PrintWriter(openFileOutput(file, Context.MODE_PRIVATE))
        for (label in list) pw.println(label)
        pw.close()
    }
}

// This is a custom adapter extended from ArrayAdapter.
class KeymapListAdapter(internal var context: Context, layoutResourceId: Int, private var originalKeymapLabels: List<String>, private var keymapMappings: MutableList<String?>) : ArrayAdapter<String>(context, layoutResourceId, originalKeymapLabels) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val labelView: View = convertView ?: (context as Activity).layoutInflater.inflate(R.layout.keymap_table, parent, false)
        val mKeymapOriginalLabel = labelView.findViewById<TextView>(R.id.keymap_original_label)
        val mKeymapMappedLabel = labelView.findViewById<TextView>(R.id.keymap_mapped_label)

        if (unsavedKeymapChanges.contains(position)) {
            mKeymapMappedLabel.setTextColor(primaryColour.data)
            mKeymapMappedLabel.setTypeface(mKeymapMappedLabel.typeface, Typeface.BOLD)
        } else mKeymapMappedLabel.setTextColor(Color.BLACK)
        mKeymapOriginalLabel.text = originalKeymapLabels[position]
        mKeymapMappedLabel.text = keymapMappings[position]
        return labelView
    }
}
class KeymapKeyChooserAdapter(context: Context, layoutResourceId: Int, keymapLabels: List<String>, val height: Int) : ArrayAdapter<String>(context, layoutResourceId, keymapLabels) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = super.getView(position, convertView, parent) as TextView
        view.layoutParams.height = height
        return view
    }
}