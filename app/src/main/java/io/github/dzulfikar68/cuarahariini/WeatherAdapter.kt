package io.github.dzulfikar68.cuarahariini

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.github.dzulfikar68.cuarahariini.databinding.AdapterWeatherBinding
import kotlin.math.roundToInt

class WeatherAdapter : RecyclerView.Adapter<WeatherAdapter.MessageViewHolder>() {
    var listWeather = ArrayList<DailyResponse>()

    fun setList(messages: List<DailyResponse>) {
        this.listWeather.clear()
        this.listWeather.addAll(messages)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val messageAdapterBinding = AdapterWeatherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(messageAdapterBinding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val weather = listWeather[position]
        holder.bind(weather)
    }

    override fun getItemCount(): Int = listWeather.size

    class MessageViewHolder(private val binding: AdapterWeatherBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(weather: DailyResponse) {
            with(binding) {
                tvDay.text = Utils.timestampToDay(weather.dt ?: 0L)
                val celcius = weather.temp?.day ?: 0.0
                tvPercent.text = celcius.minus(273.15).roundToInt().toString() + " °C"
                Glide.with(binding.root)
                    .load(Utils.weatherToImage(weather.weather?.get(0)?.main))
                    .into(binding.ivPicture)
            }
        }
    }
}