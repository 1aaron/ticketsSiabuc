package com.aaron.integradorsiabuc

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.CardView
import android.util.Log
import android.view.View
import android.widget.*
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import android.view.animation.LayoutAnimationController
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.view.animation.AnimationSet
import com.jakewharton.rxbinding.widget.RxTextView
import org.json.JSONArray
import rx.Observer
import rx.functions.Action1


class MainActivity : AppCompatActivity() {

    //elementos login
    lateinit var btnSignup: ImageButton
    lateinit var btnLogin: ImageButton
    lateinit var layoutSignin: LinearLayout
    lateinit var edtEmail: EditText
    lateinit var edtPassword: EditText

    //elementos Registro
    lateinit var layoutRegister: LinearLayout
    lateinit var btnSignupRegister: ImageButton
    lateinit var spinnerPlantel : Spinner
    lateinit var edtNombre : EditText
    lateinit var edtPasswordRegister: EditText
    lateinit var edtPasswordRegisterRepeat: EditText
    lateinit var edtCorreoRegister: EditText

    //elementos independientes
    var registering = false
    var plantelSeleccionadoPos = 0
    private val TAG = "errorMain"
    private val urlRegistro = "http://siabuc.hol.es/ticketsSiabuc/user/register"
    //private val urlRegistro = "http://192.168.1.70/ticketsSiabuc/user/register"
    private val urlLogin = "http://siabuc.hol.es/ticketsSiabuc/user/login"
    //private val urlLogin = "http://192.168.1.70/ticketsSiabuc/user/login"
    //private val urlPlanteles = "http://192.168.1.70/ticketsSiabuc/user/planteles"
    private val urlPlanteles = "http://siabuc.hol.es/ticketsSiabuc/user/planteles"

    private lateinit var pDialog : ProgressDialog
    private lateinit var context: Context
    var idPlanteles: ArrayList<String> = ArrayList()
    var descPlanteles: ArrayList<String> = ArrayList()

    lateinit var sharedPreference: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pDialog = ProgressDialog(this)
        edtEmail = findViewById(R.id.edtEmail)
        edtPassword = findViewById(R.id.edtPassword)
        btnLogin = findViewById(R.id.btnLogin)
        context = this
        sharedPreference = getSharedPreferences(resources.getString(R.string.prefs),Context.MODE_PRIVATE)
        btnLogin.setOnClickListener({
            if(edtEmail.text.length<1 || edtPassword.text.length<1){
                Toast.makeText(this,"Ingresa todos lo campos",Toast.LENGTH_SHORT).show()
            }else if(!edtEmail.text.toString().contains("@ucol.mx")){
                edtEmail.error = "Correo inválido"
            }else {
                loginRequest()
            }
        })
        btnSignup = findViewById(R.id.btnSignUp)
        layoutSignin = findViewById(R.id.layoutSignIn)
        edtNombre = findViewById(R.id.edtNombre)
        edtPasswordRegister = findViewById(R.id.edtPasswordRegister)
        edtPasswordRegisterRepeat = findViewById(R.id.edtPasswordRegisterRepeat)
        edtCorreoRegister = findViewById(R.id.edtCorreoRegister)
        spinnerPlantel = findViewById(R.id.spnPlantel)
        //val adapter = ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,descPlanteles)
        //spinnerPlantel.adapter = adapter
        //spinnerPlantel.setSelection(0)
        spinnerPlantel.onItemSelectedListener = object:  AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {
                Log.e(TAG,"nada seleccionado")
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                plantelSeleccionadoPos = p2
            }
        }

        /*RxTextView.textChanges(edtPasswordRegister)
                .subscribe(Action1<CharSequence> (){
                    @Override
                    public void call(avoid: Void){

                    }
                })*/
        RxTextView.textChanges(edtPasswordRegisterRepeat)
                .subscribe { charSecuence ->
                    if(!edtPasswordRegister.text.toString().equals(charSecuence.toString()))
                        edtPasswordRegisterRepeat.error = "no coiciden"
                    else
                        edtPasswordRegisterRepeat.error = null
                }
        btnSignupRegister = findViewById(R.id.btnSignUpRegister)
        btnSignupRegister.setOnClickListener({
            if(edtNombre.text.length<1 || edtCorreoRegister.text.length<1 || edtPasswordRegister.text.length<1){
                Toast.makeText(this,"Ingresa todos lo campos",Toast.LENGTH_SHORT).show()
            }else if(!edtCorreoRegister.text.toString().contains("@ucol.mx")){
                edtCorreoRegister.error = "Correo inválido"
            }else {
                registerRequest()
            }
        })
        layoutRegister = findViewById(R.id.layoutSignUp)
        btnSignup.setOnClickListener({
            animar(layoutSignin,false)
            layoutSignin.visibility = LinearLayout.GONE
            animar(layoutRegister,true)
            layoutRegister.visibility = LinearLayout.VISIBLE
            registering = true
            getPlanteles()
        })
    }


    private fun getPlanteles(){
        descPlanteles.clear()
        idPlanteles.clear()
        pDialog.setMessage("Espere...")
        pDialog.setCanceledOnTouchOutside(false)
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pDialog.show()
        Log.e(TAG,urlPlanteles)
        val getRequest = object  : StringRequest(Request.Method.GET,urlPlanteles,Response.Listener<String> {
            response ->
            try{
                Log.e(TAG,response)
                val json = JSONObject(response)
                if(json.getString("status").equals("1")){
                    val jsonDatos = json.getJSONArray("datos")
                    var i = 0
                    while (i<jsonDatos.length()) {
                        val jsonInfo: JSONObject = jsonDatos.getJSONObject(i)
                        idPlanteles.add(jsonInfo.getString("id"))
                        descPlanteles.add(jsonInfo.getString("etiqueta"))
                        i++
                    }
                    val adapter = ArrayAdapter<String>(context,android.R.layout.simple_spinner_item,descPlanteles)
                    spinnerPlantel.adapter = adapter
                    spinnerPlantel.setSelection(0)
                }else{
                    Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show()
                }
            }catch (e: Exception){e.printStackTrace()}
            pDialog.dismiss()
        }, Response.ErrorListener {
            response ->
            Log.e(TAG,response.toString())
            Toast.makeText(this,"Error de red",Toast.LENGTH_SHORT).show()
            pDialog.dismiss()
        }){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("tickets","T1Ck3t5")
                return headers
            }
        }
        getRequest.retryPolicy = DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        val queue = Volley.newRequestQueue(this)
        queue.add(getRequest)
    }

    private fun loginRequest(){
        pDialog.setMessage("Validando")
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pDialog.show()
        Log.e(TAG,urlLogin)
        val postRequest = object : StringRequest(Request.Method.POST,urlLogin,Response.Listener<String> {
            response ->
            try {
                Log.e(TAG,response)
                val json = JSONObject(response)
                if (json.getString("status").equals("1")) {
                    Toast.makeText(this, "Login exitoso", Toast.LENGTH_SHORT).show()
                    val editor = sharedPreference.edit()
                    editor.putInt("idUsuario", json.getInt("idUsuario"))
                    editor.putInt("plantel", json.getInt("dependencia"))
                    editor.putInt("tipoUsuario", json.getInt("tipoUsuario"))
                    editor.commit()
                    remove()
                    val i = Intent(context, ActivityPrincipal::class.java)
                    startActivity(i)
                    finish()
                } else if (json.getString("status").equals("2")) {
                    edtEmail.error = "datos incorrectos"
                } else {
                    Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT)
                }
            }catch (e: Exception){e.printStackTrace()}
            pDialog.dismiss()
        }, Response.ErrorListener {
            response ->
            Log.e(TAG,response.toString())
            Toast.makeText(this,"Error de red",Toast.LENGTH_SHORT).show()
            pDialog.dismiss()
        }){
            override fun getParams(): HashMap<String, String> {
                val params = HashMap<String, String>()
                val email = edtEmail.text.toString()
                val password = edtPassword.text.toString()
                val json = "{ \"email\":\"$email\", \"password\": \"$password\"}"
                params.put("info",json)
                Log.e(TAG,params.toString())
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("tickets","T1Ck3t5")
                return headers
            }
        }
        postRequest.retryPolicy = DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        val queue = Volley.newRequestQueue(this)
        queue.add(postRequest)
    }

    private fun registerRequest(){
        pDialog.setMessage("Registrando")
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pDialog.show()
        val response: String? = null
        val finalResponse = response
        val postRequest = object : StringRequest(Request.Method.POST,urlRegistro,Response.Listener<String> {
            response ->
            Log.e(TAG,response)
            val json = JSONObject(response)
            if(json.getString("status").equals("1")){
                Toast.makeText(this,"Registro exitoso, inicia sesión",Toast.LENGTH_SHORT).show()
                remove()
                animar(layoutRegister,false)
                layoutRegister.visibility = LinearLayout.GONE
                animar(layoutSignin,true)
                layoutSignin.visibility = LinearLayout.VISIBLE
                registering = false
            }else{
                Toast.makeText(this,json.getString("status"),Toast.LENGTH_SHORT).show()
            }
            pDialog.dismiss()
        }, Response.ErrorListener {
            response ->
            Toast.makeText(this,"Error de red",Toast.LENGTH_SHORT).show()
            Log.e(TAG,response.toString())
            pDialog.dismiss()
        }){
            override fun getParams(): HashMap<String, String> {
                val params = HashMap<String, String>()
                val email = edtCorreoRegister.text.toString()
                val password = edtPasswordRegister.text.toString()
                val nombre = edtNombre.text.toString()
                val dependencia = idPlanteles[plantelSeleccionadoPos]
                val tipoUsuario = "0"
                val json = "{ \"email\":\"$email\", \"password\": \"$password\", \"nombre\":\"$nombre\", \"dependencia\":\"$dependencia\", " +
                        "\"tipoUsuario\":\"$tipoUsuario\"}"
                Log.e(TAG,json)
                params.put("info",json)
                Log.e(TAG,params.toString())
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("tickets","T1Ck3t5")
                return headers
            }
        }
        postRequest.retryPolicy = DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        val queue = Volley.newRequestQueue(this)
        queue.add(postRequest)
    }
    private fun remove(){
        edtCorreoRegister.setText("")
        edtPasswordRegister.setText("")
        edtNombre.setText("")
        edtEmail.setText("")
        edtPassword.setText("")
    }

    private fun animar(linearLayout: LinearLayout,mostrar: Boolean) {
        val set = AnimationSet(true)
        var animation: Animation? = null
        if (mostrar) {
            //desde la esquina inferior derecha a la superior izquierda
            animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f)
        } else {    //desde la esquina superior izquierda a la esquina inferior derecha
            animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f)
        }
        //duración en milisegundos
        animation.duration = 500
        set.addAnimation(animation)
        val controller = LayoutAnimationController(set, 0.25f)

        linearLayout.setLayoutAnimation(controller)
        linearLayout.startAnimation(animation)
    }

    override fun onBackPressed() {
        if(registering){
            registering = false
            animar(layoutRegister,false)
            layoutRegister.visibility = LinearLayout.GONE
            animar(layoutSignin,true)
            layoutSignin.visibility = LinearLayout.VISIBLE
        }else{
            super.onBackPressed()
        }
    }
}
