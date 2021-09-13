package io.github.dzulfikar68.cuarahariini

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.github.dzulfikar68.cuarahariini.databinding.ActivityMapsBinding
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


class MapsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapsBinding
    var map: MapView? = null
    private var mapController: IMapController? = null
    var mapSigned: Marker? = null

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Add By Maps (Lewat Peta)"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.etSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                getCountryByName()
                true
            } else false
        }
        binding.btnSearch.setOnClickListener {
            getCountryByName()
        }

        binding.btnSubmit.setOnClickListener {
            val nameCity = binding.etName.text.toString().trim()
            if (nameCity.isEmpty() || mapSigned == null) {
                Toast.makeText(
                    baseContext,
                    "Peta harus diberi tanda markup dan \nisian nama kota tidak boleh kosong...",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            val marker = mapSigned as Marker
            val returnIntent = Intent()
            returnIntent.putExtra("name", binding.etName.text.toString().trim())
            returnIntent.putExtra("lat", marker.position?.latitude)
            returnIntent.putExtra("long", marker.position?.longitude)
            setResult(RESULT_OK, returnIntent)
            finish()
        }

        val ctx: Context = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        map = findViewById<View>(R.id.map) as MapView
        map?.tileProvider?.clearTileCache()
        Configuration.getInstance().cacheMapTileCount = 12.toShort()
        Configuration.getInstance().cacheMapTileOvershoot = 12.toShort()

        // Create a custom tile source
//        map!!.setTileSource(object : OnlineTileSourceBase("", 1, 20, 512, ".png", arrayOf("https://a.tile.openstreetmap.org/")) {
//            override fun getTileURLString(pMapTileIndex: Long): String {
//                return (baseUrl
//                        + MapTileIndex.getZoom(pMapTileIndex)
//                        + "/" + MapTileIndex.getX(pMapTileIndex)
//                        + "/" + MapTileIndex.getY(pMapTileIndex)
//                        + mImageFilenameEnding)
//            }
//        })

        map?.setMultiTouchControls(true)
        mapController = map?.controller

        val mReceive: MapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                createMarker(null, p.latitude, p.longitude)
                return false
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                return false
            }
        }
        val overlayEvents = MapEventsOverlay(baseContext, mReceive)

//        val touchOverlay: Overlay = object : Overlay(this) {
////            var anotherItemizedIconOverlay: ItemizedIconOverlay<OverlayItem>? = null
//            override fun draw(arg0: Canvas?, arg1: MapView?, arg2: Boolean) {}
//            override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
//                return true
//            }
//        }
//
//        map?.getOverlays()?.add(touchOverlay)

        map?.overlays?.add(overlayEvents)

//        "lat":-6.2146,"long":106.8451
        val lat = intent?.getDoubleExtra("lat", 0.0)?: -6.2146
        val long = intent?.getDoubleExtra("long", 0.0)?: 106.8451
        gotoLocation(11.0, lat, long)

        Toast.makeText(
            baseContext,
            "Silahkan cari berdasarkan nama negara dan \ngeser/zoom peta untuk tempat yang dituju...",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun gotoLocation(zoom: Double, lat: Double, long: Double) {
        val startPoint = GeoPoint(lat, long)
        mapController?.setZoom(zoom)
        mapController?.setCenter(startPoint)
        map?.invalidate()
    }

    private fun createMarker(country: Country?, lat: Double, long: Double) {
        //checking map
        if (map == null) {
            return
        }

        //delete marker
        map?.overlays?.remove(mapSigned)
        map?.invalidate()

        //add marker
        val myMarker = Marker(map)
        myMarker.position = GeoPoint(lat, long)
        myMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        val latString = String.format("%.4f", lat)
        val longString = String.format("%.4f", long)
        val tooltips =  if (country != null) "Disini (Here)\nNegara: ${country.name}\nIbukota: ${country.capital}"
                        else "Disini (Here)\nLatitude: $latString\nLongitude: $longString"
        myMarker.title = tooltips
        myMarker.setPanToView(true)
        myMarker.showInfoWindow()
        map?.overlays?.add(myMarker)
        map?.invalidate()

        //save marker
        mapSigned = myMarker
    }

    private fun getCountryByName() {
        val nameCountry = binding.etSearch.text?.toString()?.trim() ?: ""
        if (nameCountry.isEmpty()) {
            Toast.makeText(
                baseContext,
                "Isian nama negara tidak boleh kosong...",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Sedang Menunggu (Loading)")
        progressDialog.setCancelable(false)
        progressDialog.show()
//        binding.pgLoading.visibility = View.VISIBLE
        val retrofit = Retrofit.Builder()
            .baseUrl("https://restcountries-v1.p.rapidapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(OpenWeatherService::class.java)
        service.getCountryByName(country = nameCountry).enqueue(
            object :
                Callback<List<Country>> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    call: Call<List<Country>>,
                    response: Response<List<Country>>
                ) {
                    progressDialog.dismiss()
//                binding.pgLoading.visibility = View.GONE
                    val data = response.body()?.get(0)
                    if (data == null) {
                        Toast.makeText(
                            baseContext,
                            "Data tidak tersedia",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        val lat = data.latlng?.get(0) ?: 0.0
                        val long = data.latlng?.get(1) ?: 0.0
                        gotoLocation(7.0, lat, long)
                        createMarker(data, lat, long)
                        binding.etName.setText(data.name)
                        hideKeyboard()
                        Toast.makeText(
                            baseContext,
                            "Silahkan geser/zoom peta dan \ncari tempat yang dituju...",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<Country>>, t: Throwable) {
                    progressDialog.dismiss()
//                binding.pgLoading.visibility = View.GONE
                    Toast.makeText(
                        this@MapsActivity,
                        "Terjadi Kesalahan Jaringan (Network Error)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}