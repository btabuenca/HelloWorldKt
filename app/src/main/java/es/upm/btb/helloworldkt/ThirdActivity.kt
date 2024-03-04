package es.upm.btb.helloworldkt

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import es.upm.btb.helloworldkt.persistence.retrofit.IOpenWeather
import es.upm.btb.helloworldkt.persistence.retrofit.WeatherAdapter
import es.upm.btb.helloworldkt.persistence.retrofit.data.WeatherData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ThirdActivity : AppCompatActivity() {
    private val TAG = "btaThirdActivity"

    private lateinit var weatherService: IOpenWeather
    private lateinit var weatherAdapter: WeatherAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)

        val timestamp = intent.getLongExtra("longitude", 0)
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

        val button: Button = findViewById(R.id.button_request_weather)
        button.setOnClickListener {
            requestWeatherData(latitude, longitude, userIdentifier?: "default_value")
        }

        // ButtomNavigationMenu
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_map -> {
                    val intent = Intent(this, OpenStreetMapActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_list -> {
                    val intent = Intent(this, SecondActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

    }

    private fun initRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/") // Replace with the base URL of your API
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherService = retrofit.create(IOpenWeather::class.java)
    }

    private fun requestWeatherData(latitude: Double, longitude: Double, apiKey: String) {
        val weatherDataCall = weatherService.getWeatherData(
            latitude = latitude,
            longitude = longitude,
            count = 10, // Asumiendo que 'count' se mantiene constante en este ejemplo
            apiKey = apiKey
        )

        weatherDataCall.enqueue(object : Callback<WeatherData> {
            override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                if (response.isSuccessful) {
                    response.body()?.let { weatherResponse ->
                        // Now it's safe to use weatherResponse.list
                        weatherAdapter.updateWeatherData(weatherResponse.list)
                        weatherAdapter.notifyDataSetChanged()
                        Toast.makeText(this@ThirdActivity, "Weather Data Retrieved", Toast.LENGTH_SHORT).show()
                    } ?: run {
                        Toast.makeText(this@ThirdActivity, "Response is null", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Log.e("MainActivity", "Error fetching weather data: ${response.errorBody()?.string()}")
                    Toast.makeText(this@ThirdActivity, "Failed to retrieve data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                // Handle error case
                Log.e("MainActivity", "Failure: ${t.message}")
                Toast.makeText(this@ThirdActivity, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getUserIdentifier(): String? {
        val sharedPreferences = this.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("userIdentifier", null)
    }
}