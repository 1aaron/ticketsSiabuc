package com.aaron.integradorsiabuc

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.app_bar_activity_principal.*
import kotlinx.android.synthetic.main.fragment_tickets_libres.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [TicketsLibres.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [TicketsLibres.newInstance] factory method to
 * create an instance of this fragment.
 */
class TicketsLibres : Fragment() {

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var tipoUsuario: Int? = null
    private var dependencia: Int? = null
    /*private val URL_LIBRES = "http://192.168.1.70/ticketsSiabuc/ticket/libres"
    private val URL_TOMAR = "http://192.168.1.70/ticketsSiabuc/ticket/tomar"*/
    private val URL_LIBRES = "http://siabuc.hol.es/ticketsSiabuc/ticket/libres"
    private val URL_TOMAR = "http://siabuc.hol.es/ticketsSiabuc/ticket/tomar"

    lateinit var tickets: ArrayList<Ticket>
    lateinit var adaptador: ListAdapter
    lateinit var pDialog: ProgressDialog
    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*if (arguments != null) {
            mParam1 = arguments.getString(ARG_PARAM1)
            mParam2 = arguments.getString(ARG_PARAM2)
        }*/
        dependencia = ActivityPrincipal.sharedPreference.getInt("plantel",-1)
        tipoUsuario = ActivityPrincipal.sharedPreference.getInt("tipoUsuario",-1)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_tickets_libres, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        pDialog = ProgressDialog(context)
        tickets = ArrayList<Ticket>()
        lstTicketsLibres.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val thisTicket = tickets.get(i)
            val builcer = AlertDialog.Builder(context,AlertDialog.THEME_DEVICE_DEFAULT_DARK)
            builcer.setTitle("¿Seguro?")
            builcer.setMessage("¿Desea tomar el ticket "+ tickets.get(i).folio+ "?")
            builcer.setPositiveButton("Si", DialogInterface.OnClickListener { dialogInterface, i ->
                tomarTicket(thisTicket)
            })
            builcer.setNegativeButton("Cancelar",null)
            builcer.create().show()
        }
        getTickets()
    }

    private fun tomarTicket(ticket: Ticket){
        pDialog.setMessage("Espere...")
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pDialog.setCanceledOnTouchOutside(false)
        pDialog.show()

        val tipoUsuario = ActivityPrincipal.sharedPreference.getInt("tipoUsuario",-1)
        val idUsuario = ActivityPrincipal.sharedPreference.getInt("idUsuario",-1)

        var stringRequest = object: StringRequest(Request.Method.POST,URL_TOMAR,Response.Listener<String> {
            response ->
            try {
                val respuesta = JSONObject(response)
                if (respuesta.getInt("status") == 1) {
                    val toast = Toast.makeText(context, "Correcto", Toast.LENGTH_SHORT)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()

                    val fragment = MisTicketsFragment.newInstance(idUsuario, tipoUsuario, dependencia!!)
                    activity.supportFragmentManager.beginTransaction()
                            .addToBackStack("misTickets")
                            .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                            .replace(R.id.content_main, fragment)
                            .commit()
                } else {
                    val toast = Toast.makeText(context, "No se pudo", Toast.LENGTH_SHORT)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                }
            }catch (e: Exception){e.printStackTrace()}
            pDialog.dismiss()
        },Response.ErrorListener {
            error ->
            Log.e("Error libres","error: ",error)
            Toast.makeText(context,"Error de red",Toast.LENGTH_SHORT).show()
            pDialog.dismiss()
        }){
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String,String>()
                var json = JSONObject()
                val date = Date()
                val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                val fecha = formatter.format(date)
                json.put("usuario",idUsuario)
                json.put("folio",ticket.folio)
                json.put("fecha",fecha)

                params.put("info",json.toString())
                Log.e("paramsLibres ",params.toString())
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("tickets","T1Ck3t5")
                return headers
            }
        }
        val qeue = Volley.newRequestQueue(context)
        stringRequest.retryPolicy = DefaultRetryPolicy(5000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        qeue.add(stringRequest)
    }


    private fun getTickets(){
        pDialog.setMessage("Buscando...")
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pDialog.setCanceledOnTouchOutside(false)
        pDialog.show()
        var stringRequest = object: StringRequest(Request.Method.POST,URL_LIBRES,Response.Listener<String> {
            response ->
            try {
                val respuesta = JSONObject(response)
                if (respuesta.getInt("status") == 1) {
                    val array = respuesta.getJSONArray("datos")
                    val cantidad = array.length()
                    var i = 0
                    while (i < cantidad) {
                        val json = array.getJSONObject(i)
                        val ticketNow = Ticket(json.getInt("folio"), json.getInt("estado"), json.getString("estadoAlias"),
                                json.getInt("usuario"), json.getString("nombre"), -1, "", json.getString("fechaInicio"),
                                json.getString("peticion"), "", -1.0f, -1, "")
                        tickets.add(ticketNow)
                        i++
                    }
                    adaptador = ListAdapter(context, tickets)
                    lstTicketsLibres.adapter = adaptador
                } else {
                    val toast = Toast.makeText(context, "No hay tickets libres", Toast.LENGTH_SHORT)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                }
            }catch (e:Exception){e.printStackTrace()}
            pDialog.dismiss()
        },Response.ErrorListener {
            error ->
            Log.e("Error libres","error: ",error)
            Toast.makeText(context,"Error de red",Toast.LENGTH_SHORT).show()
            pDialog.dismiss()
        }){
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String,String>()
                var json = JSONObject()
                json.put("tipoUsuario",tipoUsuario)
                json.put("dependencia",dependencia)
                params.put("info",json.toString())
                Log.e("paramsLibres ",params.toString())
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("tickets","T1Ck3t5")
                return headers
            }
        }
        val qeue = Volley.newRequestQueue(context)
        stringRequest.retryPolicy = DefaultRetryPolicy(5000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        qeue.add(stringRequest)
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
         * @return A new instance of fragment TicketsLibres.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(): TicketsLibres {
            val fragment = TicketsLibres()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    inner class ListAdapter(context: Context?, tickets: ArrayList<Ticket>?) : ArrayAdapter<Ticket>(context, R.layout.mi_ticket_item, tickets) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val inflater = LayoutInflater.from(context)
            val item = inflater.inflate(R.layout.mi_ticket_item,null)
            var txtFolio: TextView = item.findViewById(R.id.txtFolio)
            var txtPeticion: TextView = item.findViewById(R.id.txtPeticion)
            var txtFecha: TextView = item.findViewById(R.id.txtFecha)
            var txtEstado: TextView = item.findViewById(R.id.txtEstado)

            val actual: Ticket = tickets.get(position)
            txtFolio.text = actual.folio.toString()
            txtFecha.text = actual.fechaInicio
            txtEstado.text = actual.estadoAlias
            txtPeticion.text = actual.peticion
            return item
        }
    }
}// Required empty public constructor
