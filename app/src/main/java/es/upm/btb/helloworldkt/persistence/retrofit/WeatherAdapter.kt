package es.upm.btb.helloworldkt.persistence.retrofit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import es.upm.btb.helloworldkt.R

class WeatherAdapter(private var weatherList: List<es.upm.btb.helloworldkt.persistence.retrofit.data.List>): RecyclerView.Adapter<WeatherViewHolder>() {

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_weather, parent, false)
        return WeatherViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the contents of the view with that element
        val weatherItem = weatherList[position]
        holder.cityName.text = weatherItem.name
        holder.temperature.text = "${weatherItem.main?.temp ?: "N/A"}°C"

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = weatherList.size

    // Method to update weather data and notify adapter
    fun updateWeatherData(newWeatherList: List<es.upm.btb.helloworldkt.persistence.retrofit.data.List>) {
        weatherList = newWeatherList
        notifyDataSetChanged() // Notify any registered observers that the data set has changed.
    }
}
