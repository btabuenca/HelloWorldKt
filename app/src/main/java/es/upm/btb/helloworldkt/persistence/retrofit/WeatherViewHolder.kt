package es.upm.btb.helloworldkt.persistence.retrofit

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import es.upm.btb.helloworldkt.R

class WeatherViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val cityName: TextView = view.findViewById(R.id.textViewCityName)
    val temperature: TextView = view.findViewById(R.id.textViewTemperature)
}
