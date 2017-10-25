package com.aaron.integradorsiabuc

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Layout
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
import kotlinx.android.synthetic.main.fragment_mis_tickets.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [MisTicketsFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MisTicketsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MisTicketsFragment : Fragment(), HistorialFragment.OnFragmentInteractionListener {
    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // TODO: Rename and change types of parameters
    var idUsuario: Int? = null
    var tipoUsuario: Int? = null
    var plantel: Int? = null
    private var mListener: OnFragmentInteractionListener? = null
    private val TAG = "misFragment"
    //private val URL_MIS_TICKETS = "http://192.168.1.70/ticketsSiabuc/ticket/misTickets"
    private val URL_MIS_TICKETS = "http://siabuc.hol.es/ticketsSiabuc/ticket/misTickets"

    //lateinit var lstTickets: ListView
    lateinit var tickets: ArrayList<Ticket>
    lateinit var adaptador: Listadapter
    lateinit var pDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            idUsuario = arguments.getInt(ARG_PARAM1)
            tipoUsuario = arguments.getInt(ARG_PARAM2)
            plantel = arguments.getInt(ARG_PARAM3)
        }

    }

    private fun getTickets(){
        pDialog.setMessage("Buscando...")
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pDialog.show()
        Log.e(TAG,URL_MIS_TICKETS)
        val postRequest = object : StringRequest(Request.Method.POST,URL_MIS_TICKETS, Response.Listener<String> {
            response ->
            try {
                Log.e(TAG, response)
                val json = JSONObject(response)
                if (json.getString("status").equals("1")) {
                    var arrayDatos: JSONArray = json.getJSONArray("datos")
                    var i = 0
                    while (i < arrayDatos.length()) {
                        val jsonDeDatos: JSONObject = arrayDatos.getJSONObject(i)

                        var asignadoA: Int = -1
                        var asignadoAlias: String = ""
                        var fechaCierre: String = ""

                        try {
                            asignadoA = jsonDeDatos.getInt("asignadoA")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        try {
                            asignadoAlias = jsonDeDatos.getString("asignadoAlias")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        try {
                            fechaCierre = jsonDeDatos.getString("fechaCierre")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        val calificacion = jsonDeDatos.getString("calificacion").toFloat()
                        //val calificacion2 = jsonDeDatos.getDouble("calificacion").toFloat()

                        val newTicket: Ticket = Ticket(jsonDeDatos.getInt("folio"), jsonDeDatos.getInt("estado"), jsonDeDatos.getString("estadoAlias"),
                                jsonDeDatos.getInt("usuario"), jsonDeDatos.getString("usuarioAlias"), asignadoA,
                                asignadoAlias, jsonDeDatos.getString("fechaInicio"), jsonDeDatos.getString("peticion"),
                                fechaCierre, calificacion, -1, "")
                        tickets.add(newTicket)
                        i++
                    }
                    adaptador = Listadapter(ActivityPrincipal.contexto, tickets)
                    listTickets.adapter = adaptador
                } else {
                    Toast.makeText(ActivityPrincipal.contexto, "no hay tickets", Toast.LENGTH_SHORT).show()
                }
            }catch (e:Exception){e.printStackTrace()}
            pDialog.dismiss()
        }, Response.ErrorListener {
            response ->
            Log.e(TAG,response.toString())
            Toast.makeText(ActivityPrincipal.contexto,"Error de red",Toast.LENGTH_SHORT).show()
            pDialog.dismiss()
        }){
            override fun getParams(): HashMap<String, String> {
                val params = HashMap<String, String>()
                var json = JSONObject()
                json.put("idUsuario",idUsuario)
                json.put("tipoUsuario",tipoUsuario)
                json.put("plantel",plantel)
                params.put("info",json.toString())
                Log.e(TAG,params.toString())
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //lstTickets = view!!.findViewById(R.id.listTickets)
        tickets = ArrayList<Ticket>()
        listTickets.onItemClickListener = AdapterView.OnItemClickListener() { adapterView, view, i, l ->
            val tiketSeleccionado: Ticket = tickets.get(i)

            val fragment = HistorialFragment.newInstance(idUsuario!!,tipoUsuario!!,tiketSeleccionado)
            activity.supportFragmentManager.beginTransaction()
                    .replace(R.id.content_main,fragment)
                    .setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out)
                    .addToBackStack("historial")
                    .commit()
        }
        pDialog = ProgressDialog(ActivityPrincipal.contexto)
        getTickets()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_mis_tickets, container, false)
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
        private val ARG_PARAM1 = "idUsuario"
        private val ARG_PARAM2 = "tipoUsuario"
        private val ARG_PARAM3 = "plantel"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param idUsuario Parameter 1.
         * @param tipoUsuario Parameter 2.
         * @param plantel Parameter 3.
         * @return A new instance of fragment MisTicketsFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(idUsuario: Int, tipoUsuario: Int, plantel: Int): MisTicketsFragment {
            val fragment = MisTicketsFragment()
            val args = Bundle()
            args.putInt(ARG_PARAM1, idUsuario)
            args.putInt(ARG_PARAM2, tipoUsuario)
            args.putInt(ARG_PARAM3,plantel)
            fragment.arguments = args
            return fragment
        }
    }
    inner class Listadapter(context: Context?, tickets: ArrayList<Ticket>) : ArrayAdapter<Ticket>(context, R.layout.mi_ticket_item, tickets) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val inflater = LayoutInflater.from(context)
            var item = inflater.inflate(R.layout.mi_ticket_item,null)
            var txtFolio: TextView = item.findViewById(R.id.txtFolio)
            var txtPeticion: TextView = item.findViewById(R.id.txtPeticion)
            var txtFecha: TextView = item.findViewById(R.id.txtFecha)
            var txtEstado: TextView = item.findViewById(R.id.txtEstado)

            val actual: Ticket = tickets.get(position)
            txtFolio.text = actual.folio.toString()
            txtFecha.text = actual.fechaInicio
            if(actual.estado != 1 && actual.estado != 10)
                txtEstado.text = "En proceso"
            else
                txtEstado.text = actual.estadoAlias
            txtPeticion.text = actual.peticion
            return item
        }
    }
}// Required empty public constructor
