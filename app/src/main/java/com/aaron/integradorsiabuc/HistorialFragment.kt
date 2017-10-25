package com.aaron.integradorsiabuc

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.app_bar_activity_principal.*
import kotlinx.android.synthetic.main.fragment_historial.*
import kotlinx.android.synthetic.main.fragment_tickets_libres.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HistorialFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var idUsuario: Int? = null
    private var tipoUsuario: Int? = null
    /*private val URL_HISTORIAL = "http://192.168.1.70/ticketsSiabuc/historial/obtener"
    private val URL_COMENTARIO = "http://192.168.1.70/ticketsSiabuc/historial/insertar"
    private val URL_CERRAR = "http://192.168.1.70/ticketsSiabuc/ticket/cerrar"
    private val URL_CALIFICAR = "http://192.168.1.70/ticketsSiabuc/ticket/calificar"
    private val URL_USUARIOS_PLANTEL = "http://192.168.1.70/ticketsSiabuc/usuario/plantel"
    private val URL_ASIGNAR = "http://192.168.1.70/ticketsSiabuc/usuario/asignar"
    private val URL_ENCARGADOS = "http://192.168.1.70/ticketsSiabuc/usuario/encargado"
    private val URL_TRANSFERIR = "http://192.168.1.70/ticketsSiabuc/usuario/transferir"
    */
    private val URL_HISTORIAL = "http://siabuc.hol.es/ticketsSiabuc/historial/obtener"
    private val URL_COMENTARIO = "http://siabuc.hol.es/ticketsSiabuc/historial/insertar"
    private val URL_CERRAR = "http://siabuc.hol.es/ticketsSiabuc/ticket/cerrar"
    private val URL_CALIFICAR = "http://siabuc.hol.es/ticketsSiabuc/ticket/calificar"
    private val URL_USUARIOS_PLANTEL = "http://siabuc.hol.es/ticketsSiabuc/usuario/plantel"
    private val URL_ASIGNAR = "http://siabuc.hol.es/ticketsSiabuc/usuario/asignar"
    private val URL_ENCARGADOS = "http://siabuc.hol.es/ticketsSiabuc/usuario/encargado"
    private val URL_TRANSFERIR = "http://siabuc.hol.es/ticketsSiabuc/usuario/transferir"

    private var encargados = false
    private var rectificar = false
    private var mListener: OnFragmentInteractionListener? = null
    private lateinit var historiales: ArrayList<Historial>
    private lateinit var adaptador: ListAdapter
    lateinit var pDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            idUsuario = arguments.getInt(ARG_PARAM1)
            tipoUsuario = arguments.getInt(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_historial, container, false)
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        historiales = ArrayList<Historial>()
        pDialog = ProgressDialog(context)
        ActivityPrincipal.p_fabCerrar.visibility = View.VISIBLE
        ActivityPrincipal.p_fabCerrar.setOnClickListener {
            if(ticket.estado != 10) {
                val builder = AlertDialog.Builder(context,AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                val linear = LinearLayout(context)
                linear.orientation = LinearLayout.VERTICAL
                val edtRespuesta = EditText(context)
                edtRespuesta.hint = "Motivo del cierre"
                val edtComentario = EditText(context)
                edtComentario.hint = "puedes agregar un comentario extra aquí"
                linear.addView(edtRespuesta)
                linear.addView(edtComentario)
                builder.setView(linear)
                builder.setPositiveButton("Aceptar",null)
                builder.setNegativeButton("Cancelar",null)
                val dialog = builder.create()
                dialog.setTitle("Manda tu comentario")
                dialog.setCanceledOnTouchOutside(false)
                dialog.setOnShowListener {
                    val positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    positive.setOnClickListener {
                        if(edtRespuesta.length()<1){
                            edtRespuesta.error = "Escribe tu respuesta"
                        }else{
                            var comentario: String = "sin comentario"
                            if(edtComentario.length()>1)
                                comentario = edtComentario.text.toString()
                            val respuesta = edtRespuesta.text.toString()
                            clickCerrar(respuesta,comentario)
                            dialog.dismiss()
                        }
                    }
                }
                dialog.show()
                //clickCerrar()
            }else
                Toast.makeText(context,"Ya está cerrado",Toast.LENGTH_SHORT).show()
        }
        if(ticket.estado == 10){
            ActivityPrincipal.p_fabCerrar.visibility = View.GONE
        }
        if(tipoUsuario == 0 && ticket.estado == 10){
            ActivityPrincipal.p_fabComentar.visibility = View.GONE
        }else{
            ActivityPrincipal.p_fabComentar.visibility = View.VISIBLE
            ActivityPrincipal.p_fabComentar.setOnClickListener {
                if(tipoUsuario == 2 || tipoUsuario == 3){
                    if(ticket.asignadoA == -1){
                        Toast.makeText(context,"no está asignado aún",Toast.LENGTH_SHORT)
                    }else{
                        clickComentario()
                    }
                }else{
                    clickComentario()
                }
            }
        }

        if(tipoUsuario == 2 || tipoUsuario == 3){
            h_txtAsignacion.visibility = View.VISIBLE
            var quien = "No asignado"
            if(ticket.asignadoA != -1)
                quien = ticket.asignadoAlias
            h_txtAsignacion.text = "Asignado a: "+quien
            ActivityPrincipal.p_fabTransfer.visibility = View.VISIBLE
            ActivityPrincipal.p_fabTransfer.setOnClickListener {
                mostrarEncargados()
            }
            ActivityPrincipal.p_fabAssign.visibility = View.VISIBLE
            ActivityPrincipal.p_fabAssign.setOnClickListener {
                mostrarUsuarios()
            }
        }

        if(tipoUsuario != 0){
            h_txtAsignadoMuestra.text = "Usuario: "+ ticket.usuarioAlias
        }else{
            if(ticket.estado == 1)
                h_txtAsignadoMuestra.text = "Asignado: "+ "No asignado"
            else
                h_txtAsignadoMuestra.text = "Asignado: "+ ticket.asignadoAlias
        }
        h_peticionMuestra.text = ticket.peticion
        var estadoString = "En proceso"
        if(ticket.estado == 1 || ticket.estado == 10)
            estadoString = ticket.estadoAlias
        ActivityPrincipal.p_toolbar.setTitle("Folio: "+ ticket.folio + " "+ estadoString)
        h_rating.rating =  ticket.calificacion
        if(tipoUsuario != 0){
            h_rating.setIsIndicator(true)
        }else{
            h_rating.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { ratingBar, fl, b ->
                if(!rectificar){
                    val builder = AlertDialog.Builder(context,AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                    builder.setTitle("¿Actualizar calificación?")
                    builder.setMessage("Calificación: "+fl)
                    builder.setPositiveButton("Si", DialogInterface.OnClickListener { dialogInterface, i ->
                        enviarCalificacion(fl)
                    })
                    builder.setNegativeButton("Cancelar", DialogInterface.OnClickListener { dialogInterface, i ->
                        rectificar = true
                        h_rating.rating = ticket.calificacion
                    })
                    builder.setCancelable(false)
                    val dialogo = builder.create()
                    dialogo.setCanceledOnTouchOutside(false)
                    dialogo.show()
                }else{
                    rectificar = false
                }
            }
        }

        //h_txtFolioTitulo.text = "Folio: "+ ticket.folio + " "+ estadoString
        getHistorial()
    }

    private fun mostrarEncargados(){
        pDialog.setMessage("Espere...")
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pDialog.show()
        Log.e("historial",URL_ENCARGADOS)
        val postRequest = object : StringRequest(Request.Method.POST,URL_ENCARGADOS, Response.Listener<String> {
            response ->
            Log.e("historial",response)
            try{
                val json = JSONObject(response)
                if(json.getString("status").equals("1")) {
                    var userId = ArrayList<Int>()
                    var userName = ArrayList<String>()
                    val jsonArray = json.getJSONArray("datos")
                    val limite = jsonArray.length()
                    var i = 0
                    while (i < limite) {
                        val thisJson = jsonArray.getJSONObject(i)
                        userId.add(thisJson.getInt("id"))
                        userName.add(thisJson.getString("nombre"))
                        i++
                    }
                    encargados = true
                    desplegarUsuarios(userId,userName)
                }else{
                    Log.e("error de búsqueda",json.getString("status"))
                    rectificar = true
                    encargados = false
                    Toast.makeText(ActivityPrincipal.contexto,"error de búsqueda",Toast.LENGTH_SHORT).show()
                }
            }catch (e:Exception){e.printStackTrace()}
            pDialog.dismiss()
        }, Response.ErrorListener {
            response ->
            encargados = false
            Log.e("historial",response.toString())
            rectificar = true
            h_rating.rating = ticket.calificacion
            Toast.makeText(ActivityPrincipal.contexto,"Error de red",Toast.LENGTH_SHORT).show()
            pDialog.dismiss()
        }){
            override fun getParams(): HashMap<String, String> {
                val params = HashMap<String, String>()
                var json = JSONObject()
                val plantel = ActivityPrincipal.sharedPreference.getInt("plantel",-1)
                json.put("dependencia", plantel)
                json.put("idUsuario",idUsuario)
                params.put("info",json.toString())
                Log.e("historial",params.toString())
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("tickets","T1Ck3t5")
                return headers
            }
        }
        postRequest.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        val queue = Volley.newRequestQueue(ActivityPrincipal.contexto)
        queue.add(postRequest)
    }

    private fun mostrarUsuarios(){
        pDialog.setMessage("Espere...")
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pDialog.show()
        Log.e("historial",URL_USUARIOS_PLANTEL)
        val postRequest = object : StringRequest(Request.Method.POST,URL_USUARIOS_PLANTEL, Response.Listener<String> {
            response ->
            Log.e("historial",response)
            try {
                val json = JSONObject(response)
                if (json.getString("status").equals("1")) {
                    var userId = ArrayList<Int>()
                    var userName = ArrayList<String>()
                    val jsonArray = json.getJSONArray("datos")
                    val limite = jsonArray.length()
                    var i = 0
                    while (i < limite) {
                        val thisJson = jsonArray.getJSONObject(i)
                        userId.add(thisJson.getInt("id"))
                        userName.add(thisJson.getString("nombre"))
                        i++
                    }
                    encargados = false
                    desplegarUsuarios(userId, userName)
                } else {
                    Log.e("error de búsqueda", json.getString("status"))
                    rectificar = true
                    Toast.makeText(ActivityPrincipal.contexto, "error de búsqueda", Toast.LENGTH_SHORT).show()
                }
            }catch (e: Exception){e.printStackTrace()}
            pDialog.dismiss()
        }, Response.ErrorListener {
            response ->
            Log.e("historial",response.toString())
            rectificar = true
            h_rating.rating = ticket.calificacion
            Toast.makeText(ActivityPrincipal.contexto,"Error de red",Toast.LENGTH_SHORT).show()
            pDialog.dismiss()
        }){
            override fun getParams(): HashMap<String, String> {
                val params = HashMap<String, String>()
                var json = JSONObject()
                val plantel = ActivityPrincipal.sharedPreference.getInt("plantel",-1)
                json.put("dependencia", plantel)
                params.put("info",json.toString())
                Log.e("historial",params.toString())
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("tickets","T1Ck3t5")
                return headers
            }
        }
        postRequest.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        val queue = Volley.newRequestQueue(ActivityPrincipal.contexto)
        queue.add(postRequest)
    }

    private fun desplegarUsuarios(userId: ArrayList<Int>,userName: ArrayList<String>){
        val builder = AlertDialog.Builder(context,AlertDialog.THEME_DEVICE_DEFAULT_DARK)
        var usuarios = arrayOfNulls<String>(userName.size)
        userName.toArray(usuarios)
        var chosen = 0
        builder.setSingleChoiceItems(usuarios,0, DialogInterface.OnClickListener { dialogInterface, i ->
            chosen = i
        })
        builder.setPositiveButton("Aceptar", DialogInterface.OnClickListener { dialogInterface, i ->
            subirAsignacion(userId.get(chosen),userName[chosen])
            //Toast.makeText(context,"elegido: "+chosen+ " id:"+userId.get(chosen),Toast.LENGTH_SHORT).show()
        })
        builder.setNegativeButton("Cancelar", null)
        val alert = builder.create()
        alert.setTitle("Elije a quien asignarlo")
        alert.show()
    }

    private fun subirAsignacion(asignacion: Int, asignacionName: String){
        pDialog.setMessage("Asignando...")
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pDialog.show()
        var urlUsada = URL_ASIGNAR
        if(encargados)
            urlUsada = URL_TRANSFERIR

        Log.e("historial",URL_ASIGNAR)
        val postRequest = object : StringRequest(Request.Method.POST,urlUsada, Response.Listener<String> {
            response ->
            try {
                Log.e("historial", response)
                val json = JSONObject(response)
                if (json.getString("status").equals("1")) {
                    Toast.makeText(context, "Corecto", Toast.LENGTH_SHORT).show()
                    getHistorial()
                    ticket.asignadoA = asignacion
                    ticket.estadoAlias = asignacionName
                } else {
                    Log.e("errorComentario", json.getString("status"))
                    Toast.makeText(ActivityPrincipal.contexto, "Error", Toast.LENGTH_SHORT).show()
                }
            }catch (e: Exception){e.printStackTrace()}
            pDialog.dismiss()
            encargados = false
        }, Response.ErrorListener {
            response ->
            Log.e("historial",response.toString())
            Toast.makeText(ActivityPrincipal.contexto,"Error de red",Toast.LENGTH_SHORT).show()
            encargados = false
            pDialog.dismiss()
        }){
            override fun getParams(): HashMap<String, String> {
                val params = HashMap<String, String>()
                var json = JSONObject()
                val date = Date()
                val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                val fecha = formatter.format(date)
                var estado = 9
                if(ticket.asignadoA != -1){
                    estado = 8
                }
                if(encargados)
                    estado = 7
                json.put("folio", ticket.folio)
                json.put("usuario",idUsuario)
                json.put("asignacion",asignacion)
                json.put("fecha",fecha)
                json.put("estado",estado)

                params.put("info",json.toString())
                Log.e("historial",params.toString())
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("tickets","T1Ck3t5")
                return headers
            }
        }
        postRequest.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        val queue = Volley.newRequestQueue(ActivityPrincipal.contexto)
        queue.add(postRequest)
    }
    private fun enviarCalificacion(calificacion: Float){
        pDialog.setMessage("Cerrando...")
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pDialog.show()
        Log.e("historial",URL_CALIFICAR)
        val postRequest = object : StringRequest(Request.Method.POST,URL_CALIFICAR, Response.Listener<String> {
            response ->
            try {
                Log.e("historial", response)
                val json = JSONObject(response)
                if (json.getString("status").equals("1")) {
                    Toast.makeText(context, "Corecto", Toast.LENGTH_SHORT).show()
                    getHistorial()
                    ticket.calificacion = calificacion
                } else {
                    Log.e("errorComentario", json.getString("status"))
                    rectificar = true
                    Toast.makeText(ActivityPrincipal.contexto, "Error", Toast.LENGTH_SHORT).show()
                    h_rating.rating = ticket.calificacion
                }
            }catch (e:Exception){e.printStackTrace()}
            pDialog.dismiss()
        }, Response.ErrorListener {
            response ->
            Log.e("historial",response.toString())
            rectificar = true
            h_rating.rating = ticket.calificacion
            Toast.makeText(ActivityPrincipal.contexto,"Error de red",Toast.LENGTH_SHORT).show()
            pDialog.dismiss()
        }){
            override fun getParams(): HashMap<String, String> {
                val params = HashMap<String, String>()
                var json = JSONObject()
                json.put("folio", ticket.folio)
                json.put("calificacion",calificacion)
                params.put("info",json.toString())
                Log.e("historial",params.toString())
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("tickets","T1Ck3t5")
                return headers
            }
        }
        postRequest.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        val queue = Volley.newRequestQueue(ActivityPrincipal.contexto)
        queue.add(postRequest)
    }

    private fun clickComentario(){
        ActivityPrincipal.p_menuFab.collapse()
        val builder = AlertDialog.Builder(context,AlertDialog.THEME_DEVICE_DEFAULT_DARK)
            val linear = LinearLayout(context)
            linear.orientation = LinearLayout.VERTICAL
            val edtRespuesta = EditText(context)
            edtRespuesta.hint = "Tu respuesta aquí"
            val edtComentario = EditText(context)
            edtComentario.hint = "puees agregar un comentario extra aquí"
            linear.addView(edtRespuesta)
            linear.addView(edtComentario)
            builder.setView(linear)
            builder.setPositiveButton("Aceptar",null)
            builder.setNegativeButton("Cancelar",null)
            val dialog = builder.create()
            dialog.setTitle("Manda tu comentario")
            dialog.setCanceledOnTouchOutside(false)
            dialog.setOnShowListener {
                val positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                positive.setOnClickListener {
                    if(edtRespuesta.length()<1){
                        edtRespuesta.error = "Escribe tu respuesta"
                    }else{
                        var comentario: String = "sin comentario"
                        if(edtComentario.length()>1)
                            comentario = edtComentario.text.toString()
                        val respuesta = edtRespuesta.text.toString()
                        enviarComentario(comentario,respuesta, ticket.folio)
                        dialog.dismiss()
                    }
                }
            }
            dialog.show()
    }

    private fun clickCerrar(respuesta: String, comentario: String){
        ActivityPrincipal.p_menuFab.collapse()
        pDialog.setMessage("Cerrando...")
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pDialog.show()
        Log.e("historial",URL_CERRAR)
        val postRequest = object : StringRequest(Request.Method.POST,URL_CERRAR, Response.Listener<String> {
            response ->
            try {
                Log.e("historial", response)
                val json = JSONObject(response)
                if (json.getString("status").equals("1")) {
                    Toast.makeText(context, "Cerrado", Toast.LENGTH_SHORT).show()
                    historiales.clear()
                    adaptador.notifyDataSetChanged()
                    getHistorial()
                    ticket.estado = 10
                    ticket.estadoAlias = "Cerrado"
                    ActivityPrincipal.p_fabCerrar.visibility = View.GONE
                    ActivityPrincipal.p_toolbar.title = "Folio: " + ticket.folio + " Cerrado"
                } else {
                    Log.e("errorComentario", json.getString("status"))
                    Toast.makeText(ActivityPrincipal.contexto, "Error", Toast.LENGTH_SHORT).show()
                }
            }catch (e: Exception){e.printStackTrace()}
            pDialog.dismiss()
        }, Response.ErrorListener {
            response ->
            Log.e("historial",response.toString())
            Toast.makeText(ActivityPrincipal.contexto,"Error de red",Toast.LENGTH_SHORT).show()
            pDialog.dismiss()
        }){
            override fun getParams(): HashMap<String, String> {
                val params = HashMap<String, String>()
                val date = Date()
                var formatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                val fecha = formatter.format(date)
                var json = JSONObject()

                json.put("folio", ticket.folio)
                json.put("usuario",idUsuario)
                json.put("fecha",fecha)
                json.put("respuesta",respuesta)
                json.put("comentario",comentario)
                params.put("info",json.toString())
                Log.e("historial",params.toString())
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("tickets","T1Ck3t5")
                return headers
            }
        }
        postRequest.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        val queue = Volley.newRequestQueue(ActivityPrincipal.contexto)
        queue.add(postRequest)
    }
    private fun enviarComentario(comentario: String, respuesta: String, folio: Int){
        pDialog.setMessage("Enviando...")
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pDialog.show()
        Log.e("historial",URL_COMENTARIO)
        val postRequest = object : StringRequest(Request.Method.POST,URL_COMENTARIO, Response.Listener<String> {
            response ->
            try {
                Log.e("historial", response)
                val json = JSONObject(response)
                if (json.getString("status").equals("1")) {
                    Toast.makeText(context, "enviado", Toast.LENGTH_SHORT).show()
                    historiales.clear()
                    adaptador.notifyDataSetChanged()
                    getHistorial()
                } else {
                    Log.e("errorComentario", json.getString("status"))
                    Toast.makeText(ActivityPrincipal.contexto, "Error", Toast.LENGTH_SHORT).show()
                }
            }catch (e: Exception){e.printStackTrace()}
            pDialog.dismiss()
        }, Response.ErrorListener {
            response ->
            Log.e("historial",response.toString())
            Toast.makeText(ActivityPrincipal.contexto,"Error de red",Toast.LENGTH_SHORT).show()
            pDialog.dismiss()
        }){
            override fun getParams(): HashMap<String, String> {
                val params = HashMap<String, String>()
                val date = Date()
                var formatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                val fecha = formatter.format(date)
                var json = JSONObject()

                var estado = 4
                if(tipoUsuario != 0)
                    estado = 5
                if(ticket.estado == 10)
                    estado = 6

                json.put("folio",folio)
                json.put("estado",estado)
                json.put("usuario",idUsuario)
                json.put("respuesta",respuesta)
                json.put("comentario",comentario)
                json.put("fecha",fecha)
                params.put("info",json.toString())
                Log.e("historial",params.toString())
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("tickets","T1Ck3t5")
                return headers
            }
        }
        postRequest.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        val queue = Volley.newRequestQueue(ActivityPrincipal.contexto)
        queue.add(postRequest)
    }
    private fun getHistorial(){
        pDialog.setMessage("Buscando...")
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pDialog.show()
        Log.e("urlHistorial",URL_HISTORIAL)
        val postRequest = object : StringRequest(Request.Method.POST,URL_HISTORIAL, Response.Listener<String> {
            response ->
            try {
                Log.e("historial", response)
                val json = JSONObject(response)
                if (json.getString("status").equals("1")) {
                    var arrayDatos: JSONArray = json.getJSONArray("datos")
                    var i = 0
                    while (i < arrayDatos.length()) {
                        val jsonDeDatos: JSONObject = arrayDatos.getJSONObject(i)

                        val newHistorial = Historial(jsonDeDatos.getInt("folioTicket"), jsonDeDatos.getString("fecha"),
                                jsonDeDatos.getInt("estado"), jsonDeDatos.getString("estadoAlias"), jsonDeDatos.getInt("usuario"),
                                jsonDeDatos.getString("usuarioAlias"), jsonDeDatos.getString("respuesta"), jsonDeDatos.getString("comentario"))
                        historiales.add(newHistorial)
                        i++
                    }
                    adaptador = ListAdapter(ActivityPrincipal.contexto, historiales)
                    h_lstHistoriales.adapter = adaptador
                } else {
                    Toast.makeText(ActivityPrincipal.contexto, "no hay historial", Toast.LENGTH_SHORT).show()
                }
            }catch (e: Exception){e.printStackTrace()}
            pDialog.dismiss()
        }, Response.ErrorListener {
            response ->
            Log.e("historiales",response.toString())
            Toast.makeText(ActivityPrincipal.contexto,"Error de red",Toast.LENGTH_SHORT).show()
            pDialog.dismiss()
        }){
            override fun getParams(): HashMap<String, String> {
                val params = HashMap<String, String>()
                var json = JSONObject()
                json.put("folio", ticket.folio)
                params.put("info",json.toString())
                Log.e("historiales",params.toString())
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("tickets","T1Ck3t5")
                return headers
            }
        }
        postRequest.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        val queue = Volley.newRequestQueue(ActivityPrincipal.contexto)
        queue.add(postRequest)
    }
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        ActivityPrincipal.p_toolbar.setTitle("Rebuc")
        ActivityPrincipal.p_fabCerrar.visibility = View.GONE
        ActivityPrincipal.p_fabComentar.visibility = View.GONE
        ActivityPrincipal.p_fabAssign.visibility = View.GONE
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "idUsuario"
        private val ARG_PARAM2 = "tipoUsuario"
        private lateinit var ticket: Ticket

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HistorialFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(idUsuario: Int, tipoUsuario: Int, ticketR: Ticket): HistorialFragment {
            val fragment = HistorialFragment()
            val args = Bundle()
            args.putInt(ARG_PARAM1, idUsuario)
            args.putInt(ARG_PARAM2, tipoUsuario)
            ticket = ticketR
            fragment.arguments = args
            return fragment
        }
    }
    inner class ListAdapter(context: Context?, historiales: java.util.ArrayList<Historial>) : ArrayAdapter<Historial>(context, R.layout.historial_list_item, historiales) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val inflater = LayoutInflater.from(context)
            var item = inflater.inflate(R.layout.historial_list_item,null)

            var txtUsuario: TextView = item.findViewById(R.id.h_txtUsuario)
            var txtEstado: TextView = item.findViewById(R.id.h_txtEstado)
            var txtComentario: TextView = item.findViewById(R.id.h_comentario)
            var txtRespuesta: TextView = item.findViewById(R.id.h_respuesta)
            var txtFecha: TextView = item.findViewById(R.id.h_txtFecha)

            val actual: Historial = historiales.get(position)

            txtUsuario.text = actual.usuarioAlias
            txtEstado.text = actual.estadoAlias
            txtComentario.text = actual.comentario
            txtRespuesta.text = actual.respuesta
            txtFecha.text = actual.fecha
            return item
        }
    }
}// Required empty public constructor
