package es.upm.btb.suteekt

import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import es.upm.btb.suteekt.persistence.rtdb.HotspotAdapter
import es.upm.btb.suteekt.persistence.rtdb.Hotspot
import kotlin.math.*
import es.upm.btb.suteekt.databinding.ActivityHotspotsBinding
class HotspotsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHotspotsBinding
    private lateinit var database: DatabaseReference
    private lateinit var adapter: HotspotAdapter
    private var hotspotsList = mutableListOf<Hotspot>()

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHotspotsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener las coordenadas desde el Intent
        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)

        // Inicializar la referencia de la base de datos
        database = FirebaseDatabase.getInstance().getReference("hotspots")

        // Inicializar el ListView y el adaptador
        val listView: ListView = binding.listView
        adapter = HotspotAdapter(this, hotspotsList)
        listView.adapter = adapter

        // Cargar y mostrar los datos de la base de datos
        loadHotspots()
    }

    private fun loadHotspots() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                hotspotsList.clear()
                for (hotspotSnapshot in snapshot.children) {
                    val hotspot = hotspotSnapshot.getValue(Hotspot::class.java)
                    hotspot?.let {
                        val distance = calculateDistance(latitude, longitude, it.latitude, it.longitude)
                        it.distance = distance
                        hotspotsList.add(it)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HotspotsActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Función para calcular la distancia entre dos puntos geográficos
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371e3 // Radio de la tierra en metros
        val phi1 = lat1 * Math.PI / 180
        val phi2 = lat2 * Math.PI / 180
        val deltaPhi = (lat2 - lat1) * Math.PI / 180
        val deltaLambda = (lon2 - lon1) * Math.PI / 180

        val a = sin(deltaPhi / 2) * sin(deltaPhi / 2) +
                cos(phi1) * cos(phi2) *
                sin(deltaLambda / 2) * sin(deltaLambda / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c // Devuelve la distancia en metros
    }
}
