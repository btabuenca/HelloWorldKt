package es.upm.btb.helloworldkt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button

class ThirdActivity : AppCompatActivity() {
    private val TAG = "btaThirdActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)

        Log.d(TAG, "onCreate: The activity is being created.");


        val buttonPrevious: Button = findViewById(R.id.thirdButton)
        buttonPrevious.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }

    }
}