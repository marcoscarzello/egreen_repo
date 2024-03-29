package com.example.egreen_fragmentapplication.ui.main

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.BoolRes
import androidx.annotation.MainThread
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.GoogleAuthProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.bumptech.glide.load.model.LazyHeaders
import com.example.egreen_fragmentapplication.GlideApp
import com.example.egreen_fragmentapplication.R
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.internal.Storage
import com.google.android.gms.tasks.Task
import com.google.common.net.HttpHeaders.USER_AGENT
import com.google.firebase.auth.*
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.type.DateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.lang.Exception
import java.net.URI
import java.net.URL
import java.time.Instant.now
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.util.*
import kotlin.collections.HashMap

data class User(var email : String, var plantName : String, var username : String)

class MainViewModel : ViewModel () {


    private lateinit var googleSignInClient: GoogleSignInClient

    var selectedPlant = ""

    private var mutableLastLight = MutableLiveData<String>()
    val lastLight: LiveData<String> get() = mutableLastLight

    private var mutableDarkMode = MutableLiveData<Boolean>()
    val darkMode: LiveData<Boolean> get() = mutableDarkMode

    private var mutableCurrentUser = MutableLiveData<FirebaseUser?>()
    val currentuser: LiveData<FirebaseUser?> get() = mutableCurrentUser

    private var mutableHeigth = MutableLiveData<String?>()
    val heigth: LiveData<String?> get() = mutableHeigth

    private var mutableType = MutableLiveData<String?>()
    val plantType: LiveData<String?> get() = mutableType

    private var mutablePlantList = MutableLiveData<MutableList<String>>()
    val plantList: LiveData<MutableList<String>> get() = mutablePlantList

    private var mutablePlantListUri = MutableLiveData<MutableList<String>>()
    val plantListUri: LiveData<MutableList<String>> get() = mutablePlantListUri

    //livedata per osservare umidità
    private var mutableHumidityMap = MutableLiveData<MutableMap<String, String>>()
    val humidityMap: LiveData<MutableMap<String, String>> get() = mutableHumidityMap

    private var mutableWaterMap = MutableLiveData<MutableMap<String, String>>()
    val waterMap: LiveData<MutableMap<String, String>> get() = mutableWaterMap

    private var mutableWater = MutableLiveData<String>()
    val water: LiveData<String> get() = mutableWater

    private var mutableHumidity = MutableLiveData<String>()
    val humidity: LiveData<String> get() = mutableHumidity

    private var mutableUsername = MutableLiveData<String>()
    val username: LiveData<String> get() = mutableUsername

    private var mutabledataWtList = MutableLiveData<MutableList<String>>()
    val dataWtList: LiveData<MutableList<String>> get() = mutabledataWtList

    private var mutabledataHmList = MutableLiveData<MutableList<String>>()
    val dataHmList: LiveData<MutableList<String>> get() = mutabledataHmList

    open fun initialize(){
        mutablePlantList.value = mutableListOf()
        mutablePlantListUri.value = mutableListOf()
        mutabledataWtList.value = mutableListOf()
        mutabledataHmList.value = mutableListOf()
    }

    private var mutableRefDB = MutableLiveData<DatabaseReference?>()
    val refDB: LiveData<DatabaseReference?> get() = mutableRefDB

    private var mutableRefStorage = MutableLiveData<StorageReference?>()
    val refStorage: LiveData<StorageReference?> get() = mutableRefStorage

    private var mutableProfilePicPath = MutableLiveData<Uri>()
    val profilePicPath: LiveData<Uri> get() = mutableProfilePicPath


    private var mutablePlantPicPath = MutableLiveData<Uri>()
    val plantPicPath: LiveData<Uri> get() = mutablePlantPicPath


     open fun updateCurrentUser(){
     //open fun getCurrentUser():MutableLiveData<FirebaseUser>{
        //if (FirebaseAuth.getInstance().currentUser != null )
            mutableCurrentUser.value =  FirebaseAuth.getInstance().currentUser

         mutableRefDB.value = Firebase.database.reference.child("users").child((currentuser.value?.uid.toString()))
         mutableRefStorage.value = FirebaseStorage.getInstance().reference.child("Users").child((currentuser.value?.uid.toString()))    //firestore reference
     }

    open fun logOut(activity: Activity){
        FirebaseAuth.getInstance().signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("500292871531-l6n061cpjo6srokcd8eh6dodr5n1j735.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient =  GoogleSignIn.getClient(activity, gso)
        googleSignInClient.signOut()
        updateCurrentUser()
        Log.d(TAG, "User logged out successfully.")
    }

    open fun addPlant(plantName: String, plantHeight: String, plantType : String){

        var plantData: MutableMap<String, String> = HashMap()
        var params: MutableMap<String, String> = HashMap()

        var provvisoria: MutableMap<String, String> = HashMap()     //da togliere quando si caricherà da arduino

        plantData["plantName"] = plantName
        plantData["plantHeigth"] = plantHeight
        plantData["plantType"] = plantType

        params["last5humidity"] = ""
        params["last5waterlevel"] = ""


        provvisoria["a"] = "0"
        provvisoria["b"] = "0"
        provvisoria["c"] = "0"
        provvisoria["d"] = "0"
        provvisoria["e"] = "0"



        mutableRefDB.value?.child("plants")?.child(plantName)?.setValue(plantData)
        mutableRefDB.value?.child("plants")?.child(plantName)?.child("params")?.setValue(params)

        //da eliminare (simula ultimi dati caricati da arduino
        mutableRefDB.value?.child("plants")?.child(plantName)?.child("params")?.child("last5humidity")?.setValue(provvisoria)
        mutableRefDB.value?.child("plants")?.child(plantName)?.child("params")?.child("last5waterlevel")?.setValue(provvisoria)
        mutableRefDB.value?.child("plants")?.child(plantName)?.child("params")?.child("humidityarray")?.setValue(provvisoria)


        mutableRefDB.value?.child("plants")?.child(plantName)?.child("piantaimgUrl")?.setValue(plantPicPath.value.toString())       //metto nel database il link all'immagine della pianta

        mutableRefDB.value?.child("plants")?.child(plantName)?.child("params")?.child("lastLight")?.setValue(null)

    }

    open fun deletePlant(plantName: String){
        mutableRefDB.value?.child("plants")?.child(plantName)?.removeValue()
        mutablePlantList.value?.remove(plantName)
    }

    open fun changeSelectedPlant(name: String) {

        selectedPlant = name

        mutableRefDB.value?.child("plants")?.child(selectedPlant)?.child("params")?.child("humidityarray")?.addValueEventListener(object: ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                mutableHumidityMap.value = snapshot.getValue<MutableMap<String, String>>()
                //Log.d("HUMIDITY MAP READ BY VM", humidityMap.value.toString())
                //getHmValues()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        mutableRefDB.value?.child("plants")?.child(selectedPlant)?.child("params")?.child("last5waterlevel")?.child("a")?.addValueEventListener(object: ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                var wt = snapshot.getValue<String>()
                if (wt != null) {
                    mutableWater.value = (100-(100*wt.toInt()/20)).toString()
                }
                //Log.d("Water MAP READ BY VM", waterMap.value.toString())
                //getWtValues()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        var provvisoria: MutableMap<String, String> = HashMap()

        mutableRefDB.value?.child("plants")?.child(selectedPlant)?.child("params")?.child("last5humidity")?.child("a")?.addValueEventListener(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                var hmu = snapshot.getValue<String>()
                if (hmu != null  && hmu != humidityMap.value!!.getValue("a")) {
                    mutableHumidity.value = hmu.toString()

                    provvisoria["e"] = humidityMap.value!!.getValue("d")
                    provvisoria["d"] = humidityMap.value!!.getValue("c")
                    provvisoria["c"] = humidityMap.value!!.getValue("b")
                    provvisoria["b"] = humidityMap.value!!.getValue("a")
                    provvisoria["a"] = hmu
                    Log.e("provvisoria", provvisoria.toString())
                    mutableRefDB.value?.child("plants")?.child(selectedPlant)?.child("params")?.child("humidityarray")?.setValue(provvisoria)

                }
            }
            //Log.d("Water MAP READ BY VM", waterMap.value.toString())
            //getWtValues()


            override fun onCancelled(error: DatabaseError) {
            }
        })

        mutableRefDB.value?.child("plants")?.child(selectedPlant)?.child("params")?.child("last5waterlevel")?.addValueEventListener(object: ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {

                mutableWaterMap.value = snapshot.getValue<MutableMap<String, String>>()
                //Log.d("Water MAP READ BY VM", waterMap.value.toString())
                //getWtValues()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        mutableRefDB.value?.child("plants")?.child(selectedPlant)?.child("params")?.child("lastLight")?.addValueEventListener(object: ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                mutableLastLight.value = snapshot.getValue<String>()
                //Log.d("lastlight READ BY VM",lastLight.value.toString())

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    open fun getSelectedPlantHeigth() {
    //open fun getSelectedPlantHeigth() : String {
        var v: String = "default"
        mutableRefDB.value?.child("plants")?.child(selectedPlant)?.child("plantHeigth")?.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mutableHeigth.value = snapshot.getValue<String>().toString()
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        //return heigth.value.toString()
    }
    open fun getSelectedPlantType() {

        mutableRefDB.value?.child("plants")?.child(selectedPlant)?.child("plantType")?.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mutableType.value = snapshot.getValue<String>().toString()
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        //return heigth.value.toString()
    }


    //setta il valore di dark mode
    open fun setDarkMode(boolean: Boolean){
        mutableRefDB.value?.child("darkMode")?.setValue(boolean)    //salvo su firebase la preferenza utente della dark mode
    }

    //aggiunge il listener al ramo darkmode dell'utente e ne assegna il valore a mutableDarkMode.
    open fun getDarkMode(){
        mutableRefDB.value?.child("darkMode")?.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                mutableDarkMode.value = snapshot.getValue<Boolean>()
                Log.e("dm from vm", darkMode.value.toString())
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    open fun getSelectedPlantName() : String {
        return selectedPlant
    }

    open fun modifyPlant(plantHeight: String) {
        mutableRefDB.value?.child("plants")?.child(selectedPlant)?.child("plantHeigth")?.setValue(plantHeight)

    }

    //private var mutableWtlev = MutableLiveData<String>()
    //val wtlev: LiveData<String> get() = mutableWtlev



    open fun getWtValues(){

        //child("params")?.child("last5waterlevel")?.child("a")?

        /*mutableRefDB.value?.child("plants")?.child("Painta1")?.child("params")?.child("last5waterlevel")?.child("a")?.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val wt = snapshot.getValue<String>().toString()

                Log.d("SUPER TEST WT", wt)

                mutabledataWt.value = snapshot.getValue<String>()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        return mutabledataWt.value.toString()*/

        mutableRefDB.value?.child("plants")?.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                //if (mutabledataWtList.value?.size!! > 0) {
                    mutabledataWtList.value?.clear()
                //}

                for (ds in snapshot.children) {
                    val pianta = ds.child("plantName").getValue(String::class.java)

                    if (pianta != null) {
                        mutableRefDB.value?.child("plants")?.child(pianta)?.child("params")?.child("last5waterlevel")?.child("a")?.addValueEventListener(object: ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(snapshot.getValue<String>().toString() != ""){

                                    val wt = snapshot.getValue<String>().toString()
                                    Log.e("aggiungo a wt:  ", wt)
                                    if(wt != "null") {
                                        val n = (100 - (100 * wt.toInt() / 20)).toString()
                                        //Log.d("Il wt lev", wt)
                                        mutabledataWtList.value?.add(n)
                                    }
                                    }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {} })
    }

    open fun getHmValues(){

        mutableRefDB.value?.child("plants")?.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                mutabledataHmList.value?.clear()

                for (ds in snapshot.children) {
                    val plant = ds.child("plantName").getValue(String::class.java)

                    if (plant != null) {
                        mutableRefDB.value?.child("plants")?.child(plant)?.child("params")?.child("last5humidity")?.child("a")?.addValueEventListener(object: ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val hm = snapshot.getValue<String>().toString()
                                //Log.d("Il hm lev", hm)
                                //mutabledataHmList.value?.clear()
                                mutabledataHmList.value?.add(snapshot.getValue<String>().toString())
                                //Log.d("LISTA hm", mutabledataHmList.value.toString())
                            }
                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {} })
    }

    open fun getPlants() {
        Log.e(TAG, "get plants dentroooooo")
        mutableRefDB.value?.child("plants")?.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                mutablePlantList.value?.clear()

                for (ds in snapshot.children) {
                    val pianta = ds.child("plantName").getValue(String::class.java)
                    //Log.d("DS", ds.toString())
                    //Log.d("PIANTA", pianta.toString())

                    if (pianta != null && mutablePlantList.value?.contains(pianta) == false) mutablePlantList.value?.add(pianta)
                    //Log.d("LISTA PAINTE", mutablePlantList.value.toString())
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    open fun getPlantsUri() {
        mutableRefDB.value?.child("plants")?.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                mutablePlantListUri.value?.clear()

                for (ds in snapshot.children) {
                    val piantaUri = ds.child("piantaimgUrl").getValue(String::class.java)
                    //Log.d("DS", ds.toString())
                    //Log.d("PIANTA", pianta.toString())

                    if (piantaUri != null) mutablePlantListUri.value?.add(piantaUri)
                    //Log.d("LISTA PAINTE Uri", mutablePlantListUri.value.toString())
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    //DELETE ACCOUNT
    open fun deleteAccount(activity: Activity){
        val user = mutableCurrentUser.value
        val refDB = mutableRefDB.value
        logOut(activity)
        Log.d( "User deleting account :" , user.toString())
        user?.delete()!!
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User account deleted.")
                    refDB?.setValue(null)                       //cancella dal database ramo e sottorami relativi all'utente in questione
                }
            }

    }
    open fun getEmail(): String {
        return mutableCurrentUser.value?.email.toString()
    }

    open fun getUsername() {

        mutableRefDB.value?.child("username")?.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    mutableUsername.value = snapshot.getValue<String>().toString()
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    open fun setUsername(username: String){
        mutableRefDB.value?.child("username")?.setValue(username)

    }


    //CHANGE PSW
     open fun changePsw(newPsw: String){

        mutableCurrentUser.value!!.updatePassword(newPsw)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User password updated.")
                }
            }
    }

    open fun reAuthUser(current_password: String): Boolean{
        Log.d("currentpsw in VM: ", current_password)
        var success = false
        if(!current_password.isEmpty() && current_password != null) {
            Log.d("entrato nell if VM", "yes")
            Log.d("EMAIL", mutableCurrentUser.value?.email.toString())
            val credential: AuthCredential = EmailAuthProvider.getCredential(mutableCurrentUser.value?.email.toString(), current_password)
            Log.d("CREDENTIAL", credential.toString())


            mutableCurrentUser.value!!.reauthenticate(credential).addOnSuccessListener {
                        success = true
                        Log.d(TAG, "User re-authenticated.")}.addOnFailureListener{

                        success = false
                        Log.d(TAG, "not  authhenticated CAZZO")
                    }


        }
        Log.d("SUCCESSSSSS: ", success.toString())
        return success
    }


    //TODO: update profile settings

    //TODO: get user data

    open fun resetTmpPlantPath(){
        mutablePlantPicPath.value = "".toUri()
    }
    fun downTmpPlantPic(context: Context, imageView: ImageView, uri: Uri?){
        GlideApp.with(context).load(uri.toString().toUri()).into(imageView)
        Log.e("URItmp", uri.toString())
    }

    fun downProfilePic(context: Context, imageView: ImageView){

        mutableRefDB.value?.child("profileImgUrl")?.addValueEventListener(
            object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                        GlideApp.with(context).load(snapshot.value.toString().toUri()).into(imageView)
                    //Log.d("Test", snapshot.value.toString().toUri().c)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            }
        )
    }

    fun downPlantPic(context: Context, imageView: ImageView){
        mutableRefDB.value?.child("plants/$selectedPlant/piantaimgUrl")?.addValueEventListener(
            object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value.toString() == ""){
                        imageView.setImageResource(R.drawable.plant_placeholder)
                    }else
                        GlideApp.with(context).load(snapshot.value.toString().toUri()).into(imageView)
                    //Log.d("Test", snapshot.value.toString().toUri().c)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            }
        )
    }

    fun downTakenPic(context: Context, imageView: ImageView, uri: Uri?){
        Log.e(TAG, picFrom.toString())
        when (picFrom){
            0 -> downProfilePic(context, imageView)
            1 -> downTmpPlantPic(context, imageView, uri)
            2 -> downPlantPic(context, imageView)
        }
    }


    private val TAG = "FirebaseStorageManager"
    private lateinit var mProgressDialog: ProgressDialog

    var picFrom = 100

    @RequiresApi(Build.VERSION_CODES.N)
    fun uploadPic(mContext: Context, imageURI: Uri){

        mProgressDialog = ProgressDialog(mContext)
        mProgressDialog.setMessage("Please wait, image being uploading.....")
        mProgressDialog.show()

        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault() )
        val now = Date()
        val fileName = formatter.format(now)

        when (picFrom) {

                    //metto/cambio immagine profilo
            0 -> {
                val uploadTask =
                    mutableRefStorage.value?.child("/profilePic.png")?.putFile(imageURI)
                uploadTask?.addOnSuccessListener {
                    //success
                    Log.e(TAG, "Image upload successfully")
                    mProgressDialog.dismiss()

                    val downloadURLTask =
                        mutableRefStorage.value?.child("/profilePic.png")?.downloadUrl
                    downloadURLTask?.addOnSuccessListener {
                        Log.e(TAG, "IMAGE PATH: $it")
                        mutableProfilePicPath.value = it    // URL immagine profilo
                        mutableRefDB.value?.child("profileImgUrl")?.setValue(it.toString()) //metto imgUrl nel ramo del firebase dell'utente    !IMPORTANTE!
                        mProgressDialog.dismiss()
                    }?.addOnFailureListener {
                        mProgressDialog.dismiss()
                    }

                }?.addOnFailureListener {
                    Log.e(TAG, "Image upload failed")
                    mProgressDialog.dismiss()
                }
            }
                //img nuova pianta in fase di creazione della stessa
            1 ->{
                val uploadTask =
                    mutableRefStorage.value?.child("/plants/$fileName")?.putFile(imageURI)      //la chiamo provvisoriamente con l'ora corrente (salvata in filename)
                uploadTask?.addOnSuccessListener {
                    //success
                    Log.e(TAG, "Image upload successfully")
                    mProgressDialog.dismiss()

                    val downloadURLTask =
                        mutableRefStorage.value?.child("/plants/$fileName")?.downloadUrl
                    downloadURLTask?.addOnSuccessListener {
                        Log.e(TAG, "PLANT PATH: $it")
                        mutablePlantPicPath.value = it    // URL immagine nuova pianta

                        mProgressDialog.dismiss()


                    }?.addOnFailureListener {
                        mProgressDialog.dismiss()
                    }

                }?.addOnFailureListener {
                    Log.e(TAG, "Image upload failed")
                    mProgressDialog.dismiss()
                }
            }

                    //cambio immagine pianta già creata
            2->{
                val uploadTask =
                    mutableRefStorage.value?.child("/$selectedPlant")?.putFile(imageURI)
                uploadTask?.addOnSuccessListener {
                    //success
                    Log.e(TAG, "Image upload successfully")
                    mProgressDialog.dismiss()

                    val downloadURLTask =
                        mutableRefStorage.value?.child("/$selectedPlant")?.downloadUrl
                    downloadURLTask?.addOnSuccessListener {
                        Log.e(TAG, "IMAGE PATH: $it")
                        mutableProfilePicPath.value = it    // URL immagine profilo
                        mutableRefDB.value?.child("plants/$selectedPlant/piantaimgUrl")?.setValue(it.toString()) //metto imgUrl nel ramo del firebase dell'utente    !IMPORTANTE!
                        mProgressDialog.dismiss()
                    }?.addOnFailureListener {
                        mProgressDialog.dismiss()
                    }

                }?.addOnFailureListener {
                    Log.e(TAG, "Image upload failed")
                    mProgressDialog.dismiss()
                }

            }

        }
    }

    fun changeImgCalledFrom(int: Int){
        picFrom = int

        /*
        0 se da account settings
        1 se da add plant
        ....
         */
    }

}