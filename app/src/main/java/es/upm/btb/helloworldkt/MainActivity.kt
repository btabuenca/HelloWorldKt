package es.upm.btb.helloworldkt

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.TextView
import androidx.core.app.ActivityCompat
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.android.material.bottomnavigation.BottomNavigationView
import es.upm.btb.helloworldkt.persistence.room.AppDatabase
import es.upm.btb.helloworldkt.persistence.room.LocationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity(), LocationListener {
    private val TAG = "btaMainActivity"
    private lateinit var locationManager: LocationManager
    private var latestLocation: Location? = null
    private val locationPermissionCode = 2
    private lateinit var database: AppDatabase

    // companion is the same than an static object in java
    companion object {
        private const val RC_SIGN_IN = 123
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "onCreate: The activity is being created.")
        println("Hello world to test System.out standard output!")

        // ButtomNavigationMenu
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener { item ->
            val currentActivity = this::class.java.simpleName
            when (item.itemId) {
                R.id.navigation_home -> if (currentActivity != MainActivity::class.java.simpleName) {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                R.id.navigation_map -> if (currentActivity != OpenStreetMapActivity::class.java.simpleName) {
                    if (latestLocation != null) {
                        val intent = Intent(this, OpenStreetMapActivity::class.java)
                        val bundle = Bundle()
                        bundle.putParcelable("location", latestLocation)
                        intent.putExtra("locationBundle", bundle)
                        startActivity(intent)
                    }else{
                        Log.e(TAG, "Location not set yet.")
                        startActivity(Intent(this, OpenStreetMapActivity::class.java))
                    }
                    true
                }
                R.id.navigation_list -> if (currentActivity != SecondActivity::class.java.simpleName) {
                    startActivity(Intent(this, SecondActivity::class.java))
                }
            }
            true
        }

        // Configure Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Shared prefs. Check if the user identifier is already saved
        val userIdentifier = getUserTokenSharedPrefs()
        if (userIdentifier == null) {
            askForUserToken()
        } else {
            Toast.makeText(this, "User ID: $userIdentifier", Toast.LENGTH_LONG).show()
        }

        // Location manager init and permisssons
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                locationPermissionCode
            )
        } else {
            // The location is updated every 5000 milliseconds (or 5 seconds) and/or if the device moves more than 5 meters,
            // whichever happens first
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
        }

        // Room database init
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "coordinates").build()


        // Init authentication flow
        launchSignInFlow()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
                }
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        latestLocation = location
        val textView: TextView = findViewById(R.id.mainTextView)
        textView.text = "\uD83D\uDCCD Latitude: [${location.latitude}], Longitude: [${location.longitude}], UserToken: [${getUserTokenSharedPrefs()}]"
        Toast.makeText(this, "Coordinates update! [${location.latitude}][${location.longitude}]", Toast.LENGTH_LONG).show()

        // save coordinates to room databse
        val newLocation = LocationEntity(
            latitude = location.latitude,
            longitude = location.longitude,
            timestamp = System.currentTimeMillis()
        )
        lifecycleScope.launch(Dispatchers.IO) {
            database.locationDao().insertLocation(newLocation)
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}    
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}


    private fun askForUserToken() {
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Enter User Token")
            .setIcon(R.mipmap.ic_launcher)
            .setView(input)
            .setPositiveButton("Save") { dialog, which ->
                val userInput = input.text.toString()
                if (userInput.isNotBlank()) {
                    setUserTokenSharedPrefs(userInput)
                    Toast.makeText(this, "User token saved: $userInput", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "User token cannot be blank", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setUserTokenSharedPrefs(userIdentifier: String) {
        val sharedPreferences = this.getSharedPreferences("AppPreferences", MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("userIdentifier", userIdentifier)
            apply()
        }
    }

    private fun getUserTokenSharedPrefs(): String? {
        val sharedPreferences = this.getSharedPreferences("AppPreferences", MODE_PRIVATE)
        return sharedPreferences.getString("userIdentifier", null)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // user login succeeded
                val user = FirebaseAuth.getInstance().currentUser
                Toast.makeText(this, R.string.signed_in, Toast.LENGTH_SHORT).show();
                user?.let {
                    val name = user.displayName ?: "No Name"
                    val uid = user.uid ?: "ND"
                    Log.i(TAG, "onActivityResult " + getString(R.string.signed_in) +"["+uid+"]"+ name);
                }


            } else {
                // user login failed
                Log.e(TAG, "Error starting auth session: ${response?.error?.errorCode}")
                Toast.makeText(this, R.string.signed_cancelled, Toast.LENGTH_SHORT).show();
                finish()
            }
        }
    }

    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN
        )
    }

    private fun logout() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                // Restart activity after finishing
                val intent = Intent(this, MainActivity::class.java)
                // Clean back stack so that user cannot retake activity after logout
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
    }

    private fun updateUIWithUserData() {
        val user = FirebaseAuth.getInstance().currentUser
        val userNameTextView: TextView = findViewById(R.id.userNameTextView)
        user?.let {
            val name = user.displayName ?: "No Name"
            userNameTextView.text = "\uD83E\uDD35\u200D♂\uFE0F " + name
        }
    }
    override fun onResume() {
        super.onResume()
        updateUIWithUserData()
    }
}