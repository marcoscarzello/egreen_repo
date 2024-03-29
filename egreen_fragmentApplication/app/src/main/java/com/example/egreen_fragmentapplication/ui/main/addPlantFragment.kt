package com.example.egreen_fragmentapplication.ui.main

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.egreen_fragmentapplication.R
import com.example.egreen_fragmentapplication.databinding.FragmentAddPlantBinding
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_login.*


/**
 * A simple [Fragment] subclass.
 * Use the [addPlantFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
//Alessandro
class addPlantFragment : Fragment(R.layout.fragment_add_plant) {

    val viewModel: MainViewModel by activityViewModels()

    private var _binding: FragmentAddPlantBinding?= null

    private var mSpinner: Spinner? = null
    private var spinnerResult: String? = null
    private var mActivityCallback: ActivityInterface? = null
    private var spinnerHeight: Spinner? = null
    var spinnerHeightResult:String? = null
    var plantName: String? = null
    var plantHeight: String? = null

    var selectedValue: String? = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivityCallback = context as? ActivityInterface

    }



    override fun onResume() {
        super.onResume()

        val plantTypeArray = resources.getStringArray(R.array.plant_types)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, plantTypeArray)

        //(plantType?.editableText as? AutoCompleteTextView)?.setAdapter(arrayAdapter)

        val autocompleteTV = view?.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)
        // set adapter to the autocomplete tv to the arrayAdapter
        autocompleteTV?.setAdapter(arrayAdapter)

        autocompleteTV?.onItemClickListener =
            OnItemClickListener { adapterView, view, position, id ->
                selectedValue = arrayAdapter.getItem(position)
            }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val plantTypeArray = resources.getStringArray(R.array.plant_types)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, plantTypeArray)

        val autocompleteTV = view.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)
        // set adapter to the autocomplete tv to the arrayAdapter
        autocompleteTV.setAdapter(arrayAdapter)

        autocompleteTV.onItemClickListener =
            OnItemClickListener { adapterView, view, position, id ->
                selectedValue = arrayAdapter.getItem(position)
            }


        viewModel.changeImgCalledFrom(1)
        val viewModel: MainViewModel by activityViewModels()
        val nameEditText = view.findViewById(R.id.plant_name) as EditText
        var photoImage = view.findViewById<ImageView>(R.id.plant_settings_image)

        viewModel.plantPicPath.observe(this, Observer { pp ->
            viewModel.downTakenPic(this@addPlantFragment.requireContext(), photoImage, pp)
        })

        photoImage.setOnClickListener {
            Log.d("Ho Cliccato ", "la foto")
            viewModel.changeImgCalledFrom(1)    //qua dico che sto chiamando camera fragment da add plant
            findNavController().navigate(R.id.action_addPlantFragment_to_cameraFragment2)
        }

        //close keyboard on enter
        nameEditText.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                context?.hideKeyboard(v)
            }
        }



        val saveButton = view.findViewById<Button>(R.id.save_Button)
        saveButton.text = "CREATE PLANT"
        saveButton.setOnClickListener {
            //apre la schermata del dettaglio pianta
            spinnerResult = mSpinner?.selectedItem as String?
            spinnerHeightResult = spinnerHeight?.selectedItem as String?
            plantName = nameEditText.text.toString()
            //plantHeight = heightEditText.text.toString()
            //plantType = spinnerResult


            // error for empty editText
            when {
                TextUtils.isEmpty(nameEditText.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        requireContext(),
                        "Please enter plant name.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                selectedValue == "" ->{
                    Toast.makeText(
                        requireContext(),
                        "Please select a plant type.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
/*

                TextUtils.isEmpty(heightEditText.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        requireContext(),
                        "Please enter plant height.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

 */
                else -> {
                    //crea infine la pianta
                    viewModel.addPlant(
                        plantName.toString(),
                        plantHeight.toString(),
                        selectedValue.toString()
                    )
                    viewModel.changeSelectedPlant(plantName.toString())

                    //findNavController().navigate(R.id.action_addPlantFragment_to_plantFragment)
                    findNavController().navigate(R.id.action_addPlantFragment_to_networkFragment)
                }
            }
            mActivityCallback?.onContinueButtonPressed()


        }

/*
        //IMMAGINE PIANTAAAA
        val openCamera = view.findViewById<Button>(R.id.photoAPI_Button)
        openCamera.text = "PHOTO RECOGNITION"
        openCamera.setOnClickListener {
        }


        setUpPlantSpinner()
        setUpHeightSpinner()
 */
    }

    private fun setUpPlantSpinner() {

        val arrayList = arrayOf("Pianta Grassa", "Pianta Tropicale", "Pianta Carnivora", "Ent", "Altro tipo di pianta")
        mSpinner?.adapter = ArrayAdapter<String>(requireContext(),android.R.layout.simple_spinner_item, arrayList)
        mSpinner?.prompt = "Tipo di pianta"
        mSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                //salva il risultato dello spinner
                spinnerResult = mSpinner?.selectedItem as String?
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //non serve che succeda nulla
                TODO()
            }
        }

    }

    private fun setUpHeightSpinner() {
        val arrayList = arrayOf("cm", "m", "mm")
        spinnerHeight?.adapter = ArrayAdapter<String>(requireContext(),android.R.layout.simple_spinner_item, arrayList)
        spinnerHeight?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                spinnerHeightResult = mSpinner?.selectedItem as String?
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //non serve che succeda nulla
                TODO()
            }
        }

    }

    interface ActivityInterface {
        fun onContinueButtonPressed()
        fun onOpenCameraPressed()
    }


    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

}
