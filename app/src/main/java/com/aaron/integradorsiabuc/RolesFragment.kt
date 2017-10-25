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
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_roles.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [RolesFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [RolesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RolesFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null
    private lateinit var adapter: ListAdapter
    private lateinit var usuarios: ArrayList<Usuario>
   /* private val URL_USUARIOS = "http://192.168.1.70/ticketsSiabuc/usuario/administrar"
    private val URL_ROL = "http://192.168.1.70/ticketsSiabuc/usuario/rol"*/
    private val URL_USUARIOS = "http://siabuc.hol.es/ticketsSiabuc/usuario/administrar"
    private val URL_ROL = "http://siabuc.hol.es/ticketsSiabuc/usuario/rol"
    private lateinit var pDialog: ProgressDialog
    private val arrayEncargado = arrayOf("Usuario normal","Bibliotecario")
    private val arrayMaster = arrayOf("Usuario normal","Bibliotecario","Encargado","Administrador")
    private  var tipoUsuario: Int? = null
    private  var plantel: Int? = null

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments.getString(ARG_PARAM1)
            mParam2 = arguments.getString(ARG_PARAM2)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        usuarios = ArrayList()
        pDialog = ProgressDialog(context)
        adapter = ListAdapter(context,usuarios)

        tipoUsuario = ActivityPrincipal.sharedPreference.getInt("tipoUsuario",-1)
        plantel = ActivityPrincipal.sharedPreference.getInt("plantel",-1)
        r_lstUsuarios.setOnItemClickListener { adapterView, view, i, l ->
            mostrarRoles(i)
        }
        getUsuarios()
    }

    private fun mostrarRoles(posicion: Int){
        val builder = AlertDialog.Builder(context,AlertDialog.THEME_DEVICE_DEFAULT_DARK)
        val usable: Any
        var seleccion = 0
        if(tipoUsuario == 2)
            usable = arrayEncargado
        else
            usable = arrayMaster
        builder.setSingleChoiceItems(usable,0,DialogInterface.OnClickListener { dialogInterface, i ->
            seleccion = i
        })
        builder.setPositiveButton("Aceptar", DialogInterface.OnClickListener { dialogInterface, i ->
            val chosenUser = usuarios.get(posicion)
            asignarRol(chosenUser,seleccion)
        })
        builder.setNegativeButton("Cancelar",null)
        val alerta = builder.create()
        alerta.setTitle("Selecciona rol a asignar")
        alerta.show()
    }

    private fun asignarRol(usuario: Usuario, rol: Int){
        pDialog.setMessage("Aplicando...")
        pDialog.setCanceledOnTouchOutside(false)
        pDialog.setCancelable(false)
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pDialog.show()
        var request = object: StringRequest(Request.Method.POST,URL_ROL, Response.Listener {
            response ->
            try {
                val jsonResponse = JSONObject(response)
                if (jsonResponse.getInt("status") == 1) {
                    Toast.makeText(context, "Realizado con éxito", Toast.LENGTH_SHORT).show()
                    getUsuarios()
                } else {
                    Toast.makeText(context, "ocurrió un error", Toast.LENGTH_SHORT).show()
                }
            }catch (e:Exception){e.printStackTrace()}
            pDialog.dismiss()
        },Response.ErrorListener {
            error ->
            Log.e("errorUsuarios",error.toString())
            Toast.makeText(context,"Error de red",Toast.LENGTH_SHORT).show()
            pDialog.dismiss()
        }){
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String,String>()
                var json = JSONObject()
                json.put("rol",rol)
                json.put("idUsuario",usuario.idUsuario)
                params.put("info",json.toString())
                Log.e("paramsURoles ",params.toString())
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("tickets","T1Ck3t5")
                return headers
            }
        }
        val queue = Volley.newRequestQueue(context)
        request.retryPolicy = DefaultRetryPolicy(5000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        queue.add(request)
    }

    private fun getUsuarios(){
        pDialog.setMessage("Buscando...")
        pDialog.setCanceledOnTouchOutside(false)
        pDialog.setCancelable(false)
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pDialog.show()
        var request = object: StringRequest(Request.Method.POST,URL_USUARIOS, Response.Listener {
            response ->
            try {
                val jsonResponse = JSONObject(response)
                if (jsonResponse.getInt("status") == 1) {
                    usuarios.clear()
                    adapter.notifyDataSetChanged()
                    val jsonArray = jsonResponse.getJSONArray("datos")
                    val limit = jsonArray.length()
                    var i = 0
                    while (i < limit) {
                        val thisJson = jsonArray.getJSONObject(i)
                        val thisUsuario = Usuario(thisJson.getInt("id"), thisJson.getString("nombre"), thisJson.getString("correo"),
                                thisJson.getInt("dependencia"), thisJson.getString("etiqueta"), thisJson.getInt("tipoUsuario"))
                        usuarios.add(thisUsuario)
                        i++
                    }
                    adapter = ListAdapter(context, usuarios)
                    r_lstUsuarios.adapter = adapter
                } else {
                    Toast.makeText(context, "No se encontraron usuarios", Toast.LENGTH_SHORT).show()
                }
            }catch (e: Exception){e.printStackTrace()}
            pDialog.dismiss()
        },Response.ErrorListener {
            error ->
            Log.e("errorUsuarios",error.toString())
            Toast.makeText(context,"Error de red",Toast.LENGTH_SHORT).show()
            pDialog.dismiss()
        }){
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String,String>()
                var json = JSONObject()
                json.put("tipoUsuario",tipoUsuario)
                json.put("dependencia",plantel)
                params.put("info",json.toString())
                Log.e("paramsURoles ",params.toString())
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("tickets","T1Ck3t5")
                return headers
            }
        }
        val queue = Volley.newRequestQueue(context)
        request.retryPolicy = DefaultRetryPolicy(5000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        queue.add(request)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_roles, container, false)
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
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
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RolesFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): RolesFragment {
            val fragment = RolesFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    inner class ListAdapter(context: Context?, usuarios: java.util.ArrayList<Usuario>) : ArrayAdapter<Usuario>(context, R.layout.roles_item, usuarios) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val inflater = LayoutInflater.from(context)
            var item = inflater.inflate(R.layout.roles_item,null)

            var txtDependencia: TextView = item.findViewById(R.id.u_txtDependencia)
            var txtEmail: TextView = item.findViewById(R.id.u_txtEmail)
            var txtNombre: TextView = item.findViewById(R.id.u_txtNombre)
            var txtRol: TextView = item.findViewById(R.id.u_txtRol)

            val actual: Usuario = usuarios.get(position)

            txtDependencia.text = actual.dependencia
            txtEmail.text = actual.correo
            txtNombre.text = actual.nombre
            var rol = "usuario"
            when (actual.tipoUsuario){
                0 ->{
                    rol = "Usuario normal"
                }
                1 ->{
                    rol = "Bibliotecario"
                }
                2 ->{
                    rol = "Encargado"
                }
                3 ->{
                    rol = "Administrador"
                }
            }

            txtRol.text = rol
            return item
        }
    }
}// Required empty public constructor
