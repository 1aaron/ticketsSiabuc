package com.aaron.integradorsiabuc

import android.app.AlertDialog
import android.app.Fragment
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import android.widget.Toolbar
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.getbase.floatingactionbutton.FloatingActionButton
import com.getbase.floatingactionbutton.FloatingActionsMenu
import kotlinx.android.synthetic.main.activity_principal.*
import kotlinx.android.synthetic.main.app_bar_activity_principal.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ActivityPrincipal : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, MisTicketsFragment.OnFragmentInteractionListener,
TicketsLibres.OnFragmentInteractionListener, HistorialFragment.OnFragmentInteractionListener, RolesFragment.OnFragmentInteractionListener{

    companion object {
        lateinit var contexto: Context
        lateinit var sharedPreference: SharedPreferences
        lateinit var p_fabCerrar: FloatingActionButton
        lateinit var p_fabComentar: FloatingActionButton
        lateinit var p_fabTransfer: FloatingActionButton
        lateinit var p_menuFab: FloatingActionsMenu
        lateinit var p_fabAssign: FloatingActionButton
        lateinit var p_toolbar: android.support.v7.widget.Toolbar
    }

    lateinit var pDialog: ProgressDialog
    //private val URL_TICKET = "http://192.168.1.70/ticketsSiabuc/ticket/insert"
    private val URL_TICKET = "http://siabuc.hol.es/ticketsSiabuc/ticket/insert"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)
        setSupportActionBar(toolbar)
        p_toolbar = findViewById(R.id.toolbar)
        contexto = this

        p_fabCerrar = findViewById(R.id.fabCerrar)
        p_fabComentar = findViewById(R.id.fabComentar)
        p_fabAssign = findViewById(R.id.fabAsignar)
        p_menuFab = findViewById(R.id.menuFab)
        p_fabTransfer = findViewById(R.id.fabTransfer)

        pDialog = ProgressDialog(contexto)
        fab.setOnClickListener { view ->
            var alert = AlertDialog.Builder(contexto,AlertDialog.THEME_DEVICE_DEFAULT_DARK)
            val edittext = EditText(contexto)
            alert.setView(edittext)
            alert.setPositiveButton("Enviar",null)
            alert.setNegativeButton("Cancelar",null)
            val dialog = alert.create()
            dialog.setCanceledOnTouchOutside(false)
            dialog.setOnShowListener {
                val posButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                posButton.setOnClickListener(View.OnClickListener {
                    if(edittext.length() < 1) {
                        edittext.error = "Escribe un mensaje"
                    }else{
                        val mensaje = edittext.text.toString()
                        subirTicket(mensaje)
                        dialog.dismiss()
                    }
                })
            }
            dialog.setTitle("¿Cual es tu duda?")
            dialog.show()
        }

        sharedPreference = getSharedPreferences(resources.getString(R.string.prefs),Context.MODE_PRIVATE)
        val tipoUsuario = sharedPreference.getInt("tipoUsuario",-1)
        if(tipoUsuario != 3){
            val menuNav = nav_view.menu
            val menuStatics = menuNav.findItem(R.id.nav_statics)
            menuStatics.setVisible(false)
        }
        if(tipoUsuario == 1){
            val menuNav = nav_view.menu
            val menuStatics = menuNav.findItem(R.id.nav_usuarios)
            menuStatics.setVisible(false)
        }
        if(tipoUsuario == 0)
            configuracionEstudiante()
        else
            fab.visibility = View.GONE
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    private fun subirTicket(mensaje: String){
        pDialog.setMessage("Subiendo")
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pDialog.show()
        Log.e("ticket",URL_TICKET)
        val postRequest = object : StringRequest(Request.Method.POST,URL_TICKET, Response.Listener<String> {
            response ->
            Log.e("ticket",response)
            try {
                val json = JSONObject(response)
                if (json.getString("status").equals("1")) {
                    val toast = Toast.makeText(contexto, "Correcto", Toast.LENGTH_SHORT)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                } else {
                    Toast.makeText(this, "Error de subida, intenta de nuevo", Toast.LENGTH_SHORT).show()
                }
            }catch (e: Exception){e.printStackTrace()}
            pDialog.dismiss()
        }, Response.ErrorListener {
            response ->
            Log.e("ticket",response.toString())
            Toast.makeText(this,"Error de red",Toast.LENGTH_SHORT).show()
            pDialog.dismiss()
        }){
            override fun getParams(): HashMap<String, String> {
                val params = HashMap<String, String>()
                val usuario = sharedPreference.getInt("idUsuario",-1)
                var json = JSONObject()
                json.put("usuario",usuario)
                json.put("peticion",mensaje)
                val fecha = Date()
                val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                val fechaCompleta = formatter.format(fecha)
                json.put("fecha",fechaCompleta)
                params.put("info",json.toString())
                Log.e("ticket",params.toString())
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("tickets","T1Ck3t5")
                return headers
            }
        }
        postRequest.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        val queue = Volley.newRequestQueue(this)
        queue.add(postRequest)
    }

    private fun configuracionEstudiante(){
        val menuNav = nav_view.menu
        val menuStatics = menuNav.findItem(R.id.nav_statics)
        val menuUsuarios = menuNav.findItem(R.id.nav_usuarios)
        val menuLibres = menuNav.findItem(R.id.nav_Libres)

        menuLibres.setVisible(false)
        menuStatics.setVisible(false)
        menuUsuarios.setVisible(false)
    }
    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
            return
        }
        if(supportFragmentManager.backStackEntryCount < 1){
            val builder = AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_DARK)
            builder.setMessage("¿Seguro de salir?")
            builder.setPositiveButton("Si", DialogInterface.OnClickListener { dialogInterface, i ->
                finish()
            })
            builder.setNegativeButton("Cancelar",null)
            builder.create().show()
            return
        }
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.activity_principal, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_misTickets -> {
                val tipoUsuario: Int = sharedPreference.getInt("tipoUsuario",-1)
                val idUsuario: Int = sharedPreference.getInt("idUsuario",-1)
                val plantel: Int = sharedPreference.getInt("plantel",-1)
                val fragment: android.support.v4.app.Fragment = MisTicketsFragment.newInstance(idUsuario,tipoUsuario,plantel)
                supportFragmentManager.beginTransaction()
                        .replace(R.id.content_main,fragment)
                        .setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out)
                        .addToBackStack("misTickets")
                        .commit()
            }
            R.id.nav_Libres -> {
                val fragment: android.support.v4.app.Fragment = TicketsLibres.newInstance()
                supportFragmentManager.beginTransaction()
                        .replace(R.id.content_main,fragment)
                        .setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out)
                        .addToBackStack("misTickets")
                        .commit()
            }
            R.id.nav_statics -> {

            }
            R.id.nav_usuarios -> {
                val fragment: android.support.v4.app.Fragment = RolesFragment.newInstance("","")
                supportFragmentManager.beginTransaction()
                        .replace(R.id.content_main,fragment)
                        .setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out)
                        .addToBackStack("usuarios")
                        .commit()
            }
            R.id.nav_home ->{
                val stack = supportFragmentManager.backStackEntryCount
                var i = 0
                while (i<stack){
                    supportFragmentManager.popBackStack()
                    i++
                }
            }
            R.id.nav_logout ->{
                limpiarPrefs()
                val intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
    private fun limpiarPrefs(){
        val editor = sharedPreference.edit()
        editor.remove("tipoUsuario")
        editor.remove("idUsuario")
        editor.remove("plantel")
        editor.commit()
    }
    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
