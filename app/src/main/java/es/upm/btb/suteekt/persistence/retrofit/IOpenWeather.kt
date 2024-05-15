package es.upm.btb.suteekt.persistence.retrofit

import es.upm.btb.suteekt.persistence.retrofit.data.WeatherData
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call


interface IOpenWeather {

    @GET("data/2.5/find")
    fun getWeatherData(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("cnt") count: Int,
        @Query("APPID") apiKey: String
    ): Call<WeatherData>

}
