package com.example.weatherman.Activites

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.DnsResolver
import android.net.DnsResolver.Callback
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.example.weatherman.Models.WeatherModel
import com.example.weatherman.R
import com.example.weatherman.Utilities.ApiUtilities
import com.example.weatherman.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Response
import java.math.RoundingMode

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding // Using Data Binding to access UI elements
    private lateinit var currentLocation: Location // Stores the current location
    private lateinit var fusedLocationProvider:FusedLocationProviderClient // Provides access to fused location data
    private val LOCATION_REQUEST_CODE = 101 // Request code for location permission
    private val apiKey="886c87496579bc71fda6b9c466222424" // API key for accessing weather data

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding=DataBindingUtil.setContentView(this,R.layout.activity_main)




        // getting current location
        fusedLocationProvider= LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocation()
        // Listener for search action in the search bar
        binding.locationSearch.setOnEditorActionListener { textView, i, keyEvent ->

           if(i==EditorInfo.IME_ACTION_SEARCH){// If search action is triggered

               getCityWeather(binding.locationSearch.text.toString())// Get weather data for the specified city

               binding.locationSearch.text.clear()// Clear the search text field

               val view=this.currentFocus
               if (view!=null){

                   val imm:InputMethodManager=getSystemService(INPUT_METHOD_SERVICE)
                   as InputMethodManager

                   imm.hideSoftInputFromWindow(view.windowToken,0) // Hide the keyboard
                   binding.locationSearch.clearFocus()// Clear focus from the search text field
               }

               return@setOnEditorActionListener true

           }
            else{

                return@setOnEditorActionListener false


           }

        }
        // Listener for current location button
        binding.currentLocation.setOnClickListener {

            getCurrentLocation()// Get the current location
        }
        // Listener for search option button
        binding.searchOption.setOnClickListener {

            binding.locationOptionBar.visibility= View.VISIBLE
            binding.locationSearchBar.visibility=View.VISIBLE
        }

        binding.back.setOnClickListener {

            hideKeyboard(this)// Hide the keyboard

            binding.locationOptionBar.visibility= View.VISIBLE
            binding.locationSearchBar.visibility=View.GONE
        }


    }
    private fun hideKeyboard(activity:Activity){
        val imm= activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        var view =activity.currentFocus
        if (view==null){

            view= View(activity)
        }


        imm.hideSoftInputFromWindow(view.windowToken,0)


    }

    private fun getCurrentLocation(){

        if (checkPermissions()){   // Check if location permissions are granted

            if (isLocationEnabled()) {   // Check if location services are enabled

                if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                    requestPermission() // Request location permissions

                    return
                }

                fusedLocationProvider.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            currentLocation = location

                            binding.progrssbar.visibility = View.VISIBLE // Show progress bar
                            // Fetch weather data for the current location
                            fetchCurrentLocationWeather(
                                location.latitude.toString(),
                                location.longitude.toString()


                            )
                        }


                    }
            }
                        else{

                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)

                            startActivity(intent) // Open location settings to enable location services
            }


                        }


        else{

            requestPermission()  // Request location permissions

            }

        }

    private fun getCityWeather(city:String){
        binding.progrssbar.visibility=View.VISIBLE  // Show progress bar

        // Get weather data for the specified city using the API
        ApiUtilities.getApiInterface()?.getCityWeatherData(city,apiKey)
            ?.enqueue(@RequiresApi(Build.VERSION_CODES.Q)
            object : Callback<WeatherModel>, retrofit2.Callback<WeatherModel> {

                // Handle successful API response
                override fun onResponse(
                    call: Call<WeatherModel>,
                    response: Response<WeatherModel>
                ) {

                   if(response.isSuccessful){

                       binding.locationOptionBar.visibility=View.GONE// Hide location option bar
                       binding.locationSearchBar.visibility=View.GONE// Hide location search bar
                       binding.progrssbar.visibility=View.GONE// Hide progress bar

                       response.body()?.let {

                           setData(it) // Set weather data to the UI
                       }
                   }
                    else{
                       binding.locationOptionBar.visibility=View.VISIBLE// Show location option bar
                       binding.locationSearchBar.visibility=View.VISIBLE// Show location search bar
                       binding.progrssbar.visibility=View.VISIBLE// Show progress bar

                       Toast.makeText(this@MainActivity, "No City Found", Toast.LENGTH_SHORT).show()



                   }
                }

                override fun onFailure(call: Call<WeatherModel>, t: Throwable) {

                }

                override fun onAnswer(p0: WeatherModel, p1: Int) {

                }

                override fun onError(p0: DnsResolver.DnsException) {

                }

            })

    }

    private fun fetchCurrentLocationWeather(latitude: String, longitude: String) {
        // Fetch weather data for the specified latitude and longitude using the API
        ApiUtilities.getApiInterface()?.getCurrentWeatherData(latitude,longitude,apiKey)
            ?.enqueue(object : retrofit2.Callback<WeatherModel> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<WeatherModel>, response: Response<WeatherModel>) {

                    if (response.isSuccessful){

                        binding.progrssbar.visibility= View.GONE // Hide progress bar

                        response.body()?.let {
                            setData(it) // Set weather data to the UI
                        }

                    }


                }

                override fun onFailure(call: Call<WeatherModel>, t: Throwable) {


                }

            })


    }

    private fun requestPermission() {
        // Request location permissions
        ActivityCompat.requestPermissions(
            this,
            arrayOf( android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)


    }

    private fun isLocationEnabled(): Boolean {

        val locationManager: LocationManager =getSystemService(Context.LOCATION_SERVICE)
                as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                ||locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        // Check if either GPS or network location providers are enabled




    }

    private fun checkPermissions(): Boolean {

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
            ==PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){

            return true // Location permissions are granted

        }

        return false // Location permissions are not granted



    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==LOCATION_REQUEST_CODE){

            if (grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){

                getCurrentLocation() // If location permissions are granted, get the current location

            }
            else{
                // Handle the case when location permissions are not granted

            }



        }



    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setData(body:WeatherModel){
        // Set weather data to the UI elements

        binding.apply {


            selectedLocation.text=body.name
            weatherTemp.text=""+k2c(body?.main?.temp!!)+"°" //Change fron kelvin to celcius
            weatherState.text=body.weather[0].main
            weatherHumidity.text=body.main.humidity.toString()+"%"
            weatherWindspd.text=body.wind.speed.toString()+"m/s"


        }

        updateUI(body.weather[0].id)   // Update the UI based on weather conditions


    }

    private fun k2c(t:Double):Double{

        var intTemp=t

        intTemp=intTemp.minus(273)

        return intTemp.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
    }
    // Round the temperature to one decimal place and return the result


    private fun updateUI(id: Int) {

        binding.apply {

            when(id){

//  Thunderstorm
                in 200..232 -> {
                    weatherLogo.setImageResource(R.drawable.ic_storm_weather)


                }
 //Drizzle
                in 300..321 -> {
                    weatherLogo.setImageResource(R.drawable.ic_few_clouds)

//rainy
                }
                in 500..531 -> {
                    weatherLogo.setImageResource(R.drawable.ic_rainy_weather)

//snowing
                }
                in 600..622 -> {
                    weatherLogo.setImageResource(R.drawable.ic_snow_weather)

//Atmosphere
                }
                in 701..781 -> {
                    weatherLogo.setImageResource(R.drawable.ic_broken_clouds)

//clear
                }
               800 -> {
                    weatherLogo.setImageResource(R.drawable.ic_clear_day)

//clouds
                }
                in 810..804 -> {
                    weatherLogo.setImageResource(R.drawable.ic_cloudy_weather)

                }
                //unknown
                else->{

                    weatherLogo.setImageResource(R.drawable.ic_unknown)

                }
            }



        }

    }


}