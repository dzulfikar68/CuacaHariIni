package io.github.dzulfikar68.cuarahariini

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import io.github.dzulfikar68.cuarahariini.databinding.FragmentAddBinding

class AddDialogFragment(private var callback: AddCallback?): BottomSheetDialogFragment() {
    companion object {
        fun newInstance(callback: AddCallback?): AddDialogFragment = AddDialogFragment(callback)
    }

    private lateinit var fragmentAddBinding: FragmentAddBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentAddBinding = FragmentAddBinding.inflate(layoutInflater, container, false)
        return fragmentAddBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (activity != null) {
            fragmentAddBinding.btnSubmit.setOnClickListener {
                val cityName = fragmentAddBinding.etName.text.trim().toString()
                val latitude = fragmentAddBinding.etLat.text.trim().toString()
                val longitude = fragmentAddBinding.etLon.text.trim().toString()
                if (cityName.isBlank() || latitude.isBlank() || longitude.isBlank()) {
                    Snackbar.make(fragmentAddBinding.root, "Please fill all form", Snackbar.LENGTH_LONG).show()
                    Toast.makeText(
                            context,
                            "Silakan isi semua formulir (Please fill all form)",
                            Toast.LENGTH_SHORT
                    ).show()
                } else {
                    callback?.onClick(cityName, latitude, longitude)
                }
                dismiss()
            }
        }
    }

    override fun onDetach() {
        callback = null
        super.onDetach()
    }

    interface AddCallback {
        fun onClick(cityName: String, lat: String, lon: String)
    }
}