package io.github.dzulfikar68.cuarahariini

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import io.github.dzulfikar68.cuarahariini.databinding.FragmentSearchBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


class SearchDialogFragment(private var callback: SearchCallback?): DialogFragment() {
    companion object {
        const val LAUNCH_MAPS_ACTIVITY = 1
        fun newInstance(callback: SearchCallback?): SearchDialogFragment = SearchDialogFragment(
            callback
        )
    }

    private lateinit var fragmentSearchBinding: FragmentSearchBinding
    private var filteredAdapter: ArrayAdapter<City>? = null
    private var listCountry = mutableListOf<Country>()
    private var listCityOrigin = mutableListOf<City>()
    private var listCity = mutableListOf<City>()
    private var selected: Country? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentSearchBinding = FragmentSearchBinding.inflate(layoutInflater, container, false)
        return fragmentSearchBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (activity != null) {
            filteredAdapter = ArrayAdapter(
                view.context,
                android.R.layout.simple_list_item_1,
                listCity
            )
            fragmentSearchBinding.lvCities.adapter = filteredAdapter

            listCountry = JsonParserLocation(view.context).getDataCountryDataSet().toMutableList()
            val indonesia = listCountry.find { it.code == "ID" }
            listCountry.remove(indonesia)
            listCountry.sortBy { it.name }
            indonesia?.let { listCountry.add(0, it) }

            fragmentSearchBinding.btnManual.setOnClickListener {
                val dialog = AddDialogFragment.newInstance(object : AddDialogFragment.AddCallback {
                    override fun onClick(cityName: String, lat: String, lon: String) {
                        val city = City(
                            id = 0,
                            name = cityName,
                            country = "-",
                            lat = lat.toDouble(),
                            long = lon.toDouble()
                        )
                        callback?.onClick(city)
                        dismiss()
                    }
                })
                dialog.show(childFragmentManager, AddDialogFragment::class.java.simpleName)
            }

            fragmentSearchBinding.msCountry.setItems(listCountry)
            fragmentSearchBinding.msCountry.setOnItemSelectedListener { msView, position, id, item ->
                selected = item as Country?
                if (selected != null) {
                    gettingCities(selected!!) { list ->
                        listCityOrigin = list.toMutableList()
                        listCity.clear()
                        listCity.addAll(listCityOrigin)
                        filteredAdapter?.notifyDataSetChanged()
                    }
                }
            }

            fragmentSearchBinding.etSearch.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH && selected != null) {
                    onTextChangeAction()
                    true
                } else false
            }
            fragmentSearchBinding.etSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    onTextChangeAction()
                }
            })

            fragmentSearchBinding.lvCities.setOnItemClickListener { parent, lvView, position, id ->
                callback?.onClick(listCity[position])
                dismiss()
            }
            gettingCities(Country("ID", "Indonesia")) { list ->
                listCityOrigin = list.toMutableList()
                listCity.clear()
                listCity.addAll(listCityOrigin)
                filteredAdapter?.notifyDataSetChanged()
            }

            fragmentSearchBinding.btnClose.setOnClickListener {
                dismiss()
            }
            fragmentSearchBinding.btnMaps.setOnClickListener {
                val bundle = this.arguments
                if (bundle != null) {
//                    "lat":-6.2146,"long":106.8451
                    val lat = bundle.getDouble("lat", -6.2146)
                    val long = bundle.getDouble("long", 106.8451)
                    startActivityForResult(
                        Intent(context, MapsActivity::class.java)
                            .putExtra("lat", lat)
                            .putExtra("long", long), LAUNCH_MAPS_ACTIVITY
                    )
                }
            }
            fragmentSearchBinding.btnRefresh.setOnClickListener {
                gettingCities(selected ?: Country("ID", "Indonesia")) { list ->
                    listCityOrigin = list.toMutableList()
                    listCity.clear()
                    listCity.addAll(listCityOrigin)
                    filteredAdapter?.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LAUNCH_MAPS_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                val name = data?.getStringExtra("name") ?: ""
                val lat = data?.getDoubleExtra("lat", 0.0) ?: 0.0
                val long = data?.getDoubleExtra("long", 0.0) ?: 0.0
                val city = City(
                    0,
                    name,
                    "-",
                    lat,
                    long
                )
                callback?.onClick(city)
                dismiss()
            }
        }
    }


    private fun onTextChangeAction() {
        val willSearch = fragmentSearchBinding.etSearch.text.toString().trim().toLowerCase(Locale.getDefault())
        val listFiltered = listCityOrigin.filter { it.name.toLowerCase(Locale.getDefault()).contains(
            willSearch
        ) }
        listFiltered.sortedBy { it.name }
        listCity.clear()
        listCity.addAll(listFiltered)
        filteredAdapter?.notifyDataSetChanged()
    }

    private fun gettingCities(country: Country, callback: (List<City>) -> Unit) {
        fragmentSearchBinding.pgLoading.visibility = View.VISIBLE
        fragmentSearchBinding.lvCities.visibility = View.GONE
        fragmentSearchBinding.btnRefresh.visibility = View.GONE
        val retrofit = Retrofit.Builder()
            .baseUrl("https://simplemaps.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(OpenWeatherService::class.java)
        service.listCity(code = country.code.toLowerCase(Locale.getDefault())).enqueue(object :
            Callback<List<CityResponse>> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(
                call: Call<List<CityResponse>>,
                response: Response<List<CityResponse>>
            ) {
                fragmentSearchBinding.pgLoading.visibility = View.GONE
                fragmentSearchBinding.lvCities.visibility = View.VISIBLE
                val data = response.body() ?: listOf()
                val result = data.sortedByDescending {
                    try {
                        if (it.population.isNullOrEmpty()) 0 else it.population.toLong()
                    } catch (e: Exception) {
                        0
                    }
                }.map {
                    City(
                        id = 0,
                        name = it.city ?: "-",
                        country = it.iso2 ?: "ID",
                        lat = it.lat ?: 0.0,
                        long = it.lng ?: 0.0
                    )
                }
                callback.invoke(result)
            }

            override fun onFailure(call: Call<List<CityResponse>>, t: Throwable) {
                fragmentSearchBinding.pgLoading.visibility = View.GONE
                fragmentSearchBinding.lvCities.visibility = View.VISIBLE
                callback.invoke(listOf())
                Toast.makeText(
                    context,
                    "Terjadi Kesalahan Jaringan (Network Error)",
                    Toast.LENGTH_SHORT
                ).show()
                fragmentSearchBinding.btnRefresh.visibility = View.VISIBLE
            }
        })
    }

    override fun onDetach() {
        callback = null
        super.onDetach()
    }

    interface SearchCallback {
        fun onClick(city: City)
    }
}