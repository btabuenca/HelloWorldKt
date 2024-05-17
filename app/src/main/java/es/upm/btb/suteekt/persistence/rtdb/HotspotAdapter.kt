package es.upm.btb.suteekt.persistence.rtdb

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.FirebaseDatabase
import es.upm.btb.suteekt.R

class HotspotAdapter(context: Context, hotspots: List<Hotspot>) : ArrayAdapter<Hotspot>(context, 0, hotspots) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val hotspot = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_hotspot, parent, false)

        hotspot?.let {
            view.findViewById<TextView>(R.id.latitudeTextView).text = "Latitude: ${it.latitude}"
            view.findViewById<TextView>(R.id.longitudeTextView).text = "Longitude: ${it.longitude}"
            view.findViewById<TextView>(R.id.reportTextView).text = "Report: ${it.report}"
            view.findViewById<TextView>(R.id.timestampTextView).text = "Timestamp: ${it.timestamp}"
            view.findViewById<TextView>(R.id.distanceTextView).text = "Distance: ${it.distance} meters"
        }

        view.setOnClickListener {
            if (context is Activity) {
                showUpdateDialog(hotspot, context)
            }
        }


        return view
    }

    private fun showUpdateDialog(hotspot: Hotspot?, context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Update or Delete Report")

        val inflater = LayoutInflater.from(context)
        val dialogLayout = inflater.inflate(R.layout.dialog_update_hotspot, null)
        val editTextReport = dialogLayout.findViewById<EditText>(R.id.editTextReport)
        editTextReport.setText(hotspot?.report)

        builder.setView(dialogLayout)

        builder.setPositiveButton("Update") { _, _ ->
            val newReport = editTextReport.text.toString()
            if (!newReport.isBlank()) {
                updateHotspot(hotspot, newReport)
            }
        }

        builder.setNeutralButton("Delete") { _, _ ->
            deleteHotspot(hotspot)
        }

        builder.setNegativeButton("Cancel", null)

        builder.show()
    }


    private fun updateHotspot(hotspot: Hotspot?, newReport: String) {
        if (hotspot != null) {
            val hotspotRef = FirebaseDatabase.getInstance().reference
                .child("hotspots")
                .child(hotspot.timestamp.toString())

            hotspotRef.child("report").setValue(newReport)
                .addOnSuccessListener {
                    Toast.makeText(context, "Report updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to update report", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteHotspot(hotspot: Hotspot?) {
        if (hotspot != null) {
            val hotspotRef = FirebaseDatabase.getInstance().reference
                .child("hotspots")
                .child(hotspot.timestamp.toString())

            hotspotRef.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(context, "Report deleted successfully", Toast.LENGTH_SHORT).show()
                    remove(hotspot) // Removes the item from the adapter's data set
                    notifyDataSetChanged() // Refreshes the ListView
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to delete report", Toast.LENGTH_SHORT).show()
                }
        }
    }

}
