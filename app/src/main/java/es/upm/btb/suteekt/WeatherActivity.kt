package es.upm.btb.suteekt

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import es.upm.btb.suteekt.persistence.retrofit.IOpenWeather
import es.upm.btb.suteekt.persistence.retrofit.WeatherAdapter
import es.upm.btb.suteekt.persistence.retrofit.data.WeatherData
import es.upm.btb.suteekt.persistence.room.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.firebase.database.FirebaseDatabase

class WeatherActivity : AppCompatActivity() {
    private val TAG = "btaThirdActivity"

    private lateinit var weatherService: IOpenWeather
    private lateinit var weatherAdapter: WeatherAdapter

    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        // Get coordinates for selected item. Set default ones if  not obtained.
        val timestamp = intent.getLongExtra("timestamp", 0)
        val latitude = intent.getDoubleExtra("latitude", 40.475172)
        val longitude = intent.getDoubleExtra("longitude", -3.461757)

        // Shared prefs. Check if the user identifier is already saved
        val userIdentifier = getUserIdentifier()
        if (userIdentifier == null) {
            Toast.makeText(this, "User ID not set set. Request will not work", Toast.LENGTH_LONG).show()
        }
        Log.d(TAG, "Latitude: $latitude, Longitude: $longitude, Timestamp: $timestamp")

        // Find the TextView and set the coordinates
        val coordinatesTextView: TextView = findViewById(R.id.coordinatesTextView)
        coordinatesTextView.text = "Latitude: $latitude, Longitude: $longitude"

        // Initialize Retrofit to retrieve data from external web service
        initRetrofit()

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewWeather)
        recyclerView.layoutManager = LinearLayoutManager(this)
        weatherAdapter = WeatherAdapter(emptyList())
        recyclerView.adapter = weatherAdapter
        requestWeatherData(latitude, longitude, userIdentifier?: "default_value")

        // Delete item from room
        val deleteButton: Button = findViewById(R.id.deleteButton)
        deleteButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirm delete")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Yes") { dialog, which ->
                    database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "coordinates").build()
                    lifecycleScope.launch(Dispatchers.IO) {
                        Log.d(TAG, "Number of items in database before delete "+database.locationDao().getCount()+".");
                        database.locationDao().deleteLocationByTimestamp(timestamp)
                        Log.d(TAG, "Number of items in database after delete "+database.locationDao().getCount()+".");
                        withContext(Dispatchers.Main) {
                            finish()
                        }
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }


        val addReportButton: Button = findViewById(R.id.addReportButton)
        val editTextReport: EditText = findViewById(R.id.editTextReport)
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        addReportButton.setOnClickListener {
            val reportText = editTextReport.text.toString().trim()
            if (reportText.isNotEmpty() && userId != null) {
                val report = mapOf(
                    "userId" to userId,
                    "timestamp" to timestamp,
                    "report" to reportText,
                    "latitude" to latitude,
                    "longitude" to longitude
                )
                // Add item to Firebase realtime database
                addReportToDatabase(report)
            } else {
                Toast.makeText(this, "Report name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherService = retrofit.create(IOpenWeather::class.java)
    }

    private fun requestWeatherData(latitude: Double, longitude: Double, apiKey: String) {
        val weatherDataCall = weatherService.getWeatherData(
            latitude = latitude,
            longitude = longitude,
            count = 5,
            apiKey = apiKey
        )

        weatherDataCall.enqueue(object : Callback<WeatherData> {
            override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                if (response.isSuccessful) {
                    response.body()?.let { weatherResponse ->
                        // Now it's safe to use weatherResponse.list
                        weatherAdapter.updateWeatherData(weatherResponse.list)
                        weatherAdapter.notifyDataSetChanged()
                        Toast.makeText(this@WeatherActivity, "Weather Data Retrieved", Toast.LENGTH_SHORT).show()
                    } ?: run {
                        Toast.makeText(this@WeatherActivity, "Response is null", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Log.e("MainActivity", "Error fetching weather data: ${response.errorBody()?.string()}")
                    Toast.makeText(this@WeatherActivity, "Failed to retrieve data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                // Handle error case
                Log.e("MainActivity", "Failure: ${t.message}")
                Toast.makeText(this@WeatherActivity, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getUserIdentifier(): String? {
        val sharedPreferences = this.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("userIdentifier", null)
    }

    /**
     * Add item to firebase realtime database in collection hotspots
     */
    private fun addReportToDatabase(report: Map<String, Any>) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("hotspots").push()
        databaseReference.setValue(report)
            .addOnSuccessListener {
                Toast.makeText(this, "Report added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add report: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


}