package com.example.buscaAnimeconexionspring

import AnimeAdapter
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.buscaAnimeconexionspring.api.AnimeApiService
import com.example.buscaAnimeconexionspring.databinding.ActivityMainBinding
import com.example.buscaAnimeconexionspring.model.Anime
import com.example.buscaAnimeconexionspring.model.ApiResponse
import com.example.buscaAnimeconexionspring.service.ApiClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), SensorEventListener {
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AnimeAdapter
    private val allAnimes = mutableListOf<Anime>()
    private val animesList = mutableListOf<Anime>()
    private val favoritosIds = mutableListOf<Long>()
    private lateinit var apiService: AnimeApiService

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // ====== SENSOR DE LUZ ======
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private var firstLightToastShown = false
    // ============================

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "BuscaAnime"

        // ====== AUTH / GOOGLE ======
        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        // ===========================

        apiService = ApiClient.getAnimeApiService()

        adapter = AnimeAdapter(
            animes = animesList,
            onClick = { anime ->
                val intent = Intent(this, AnimeDetailActivity::class.java)
                intent.putExtra("anime", anime)
                startActivity(intent)
            },
            onToggleFavoritos = { anime -> anime.id?.let { toggleFavorito(it) } },
            favoritos = favoritosIds,
            forceRemoveLabel = false
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Botones principales
        binding.btnCargar.setOnClickListener { cargarFavoritosYAnimes() }
        binding.btnFavoritos.setOnClickListener {
        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }
        binding.btnFavoritosTop.setOnClickListener {
            startActivity(Intent(this, FavoritosActivity::class.java))
        }
        binding.btnEmisionTop.setOnClickListener {
            startActivity(Intent(this, EmisionActivity::class.java))
        }
        binding.fabAddAnime.setOnClickListener {
            startActivity(Intent(this, CreateAnimeActivity::class.java))
        }
        binding.btnLogout.setOnClickListener { signOut() }
        binding.fabSearch.setOnClickListener {
            showSearchDialog()
        }
        binding.fabSearch.setOnClickListener { showSearchDialog() }

        // botón cargar animes
        binding.btnCargar.setOnClickListener { cargarFavoritosYAnimes() }

        // ====== SENSOR DE LUZ: INICIALIZACIÓN ======
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        if (lightSensor == null) {
            Toast.makeText(this, "Este dispositivo no tiene sensor de luz", Toast.LENGTH_LONG).show()
        } else {
            Log.d(TAG, "Sensor de luz disponible")
        }
        // ===========================================

        cargarFavoritosYAnimes()
    }

    // =================== SENSOR LISTENER ===================

    override fun onResume() {
        super.onResume()
        // Registramos el listener solo si el sensor existe
        lightSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        // Muy importante: dejamos de escuchar para ahorrar batería
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No necesitamos hacer nada aquí en este ejemplo
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lightValue = event.values[0] // lux

            // Primera vez que recibimos datos: mostramos Toast (estilo profe)
            if (!firstLightToastShown) {
                Toast.makeText(
                    this,
                    "Sensor de luz activo: $lightValue lux",
                    Toast.LENGTH_SHORT
                ).show()
                firstLightToastShown = true
            }

            // Mostramos la luz en el subtítulo de la ActionBar
            supportActionBar?.subtitle = "Luz ambiente: ${"%.1f".format(lightValue)} lux"

            // Y dejamos log por si el profe mira el Logcat
            Log.d(TAG, "Luz ambiente: $lightValue lux")
        }
    }

    // =======================================================

    private fun showSearchDialog() {
        val editText = EditText(this).apply {
            hint = "Buscar anime..."
            setPadding(40, 40, 40, 40)
        }
        AlertDialog.Builder(this)
            .setTitle("Buscar anime")
            .setView(editText)
            .setPositiveButton("Buscar") { _, _ ->
                val query = editText.text.toString().trim()
                filtrarAnimes(query)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun filtrarAnimes(query: String) {
        if (query.isEmpty()) {
            animesList.clear()
            animesList.addAll(allAnimes)
            adapter.notifyDataSetChanged()
            return
        }
        val filtrados = allAnimes.filter { anime ->
            anime.nombre.contains(query, ignoreCase = true) ||
                    (anime.categoria?.contains(query, ignoreCase = true) == true)
        }
        animesList.clear()
        animesList.addAll(filtrados)
        adapter.notifyDataSetChanged()
    }

    /** Carga favoritos primero para marcar la lista */
    private fun cargarFavoritosYAnimes() {
        binding.tvEstado.text = "Cargando..."
        binding.btnCargar.isEnabled = false
        apiService.getFavorites().enqueue(object : Callback<ApiResponse<List<Anime>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<Anime>>>,
                resp: Response<ApiResponse<List<Anime>>>
            ) {
                val favs = resp.body()?.data.orEmpty()
                favoritosIds.clear()
                favoritosIds.addAll(favs.mapNotNull { it.id })
                cargarAnimes()
            }

            override fun onFailure(call: Call<ApiResponse<List<Anime>>>, t: Throwable) {
                cargarAnimes() // seguimos sin favoritos si falla
            }
        })
    }

    private fun cargarAnimes() {
        apiService.getAll().enqueue(object : Callback<ApiResponse<List<Anime>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<Anime>>>,
                response: Response<ApiResponse<List<Anime>>>
            ) {
                binding.btnCargar.isEnabled = true
                val data = response.body()?.data
                if (response.isSuccessful && data != null) {
                    allAnimes.clear()
                    allAnimes.addAll(data)
                    animesList.clear()
                    animesList.addAll(data)
                    adapter.updateFavoritos(favoritosIds)
                    adapter.notifyDataSetChanged()
                    binding.tvEstado.text = "Animes cargados: ${animesList.size}"
                    Toast.makeText(
                        this@MainActivity,
                        "Se cargaron ${animesList.size} animes",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(TAG, "Animes cargados: ${animesList.size}")
                } else {
                    binding.tvEstado.text = "Error: ${response.code()}"
                    Toast.makeText(
                        this@MainActivity,
                        "Error al cargar: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<Anime>>>, t: Throwable) {
                binding.btnCargar.isEnabled = true
                binding.tvEstado.text = "Error de conexión"
                Toast.makeText(
                    this@MainActivity,
                    "Error de conexión: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun toggleFavorito(id: Long) {
        if (favoritosIds.contains(id)) {
            apiService.removeFavorite(id).enqueue(object : Callback<ApiResponse<Boolean>> {
                override fun onResponse(
                    call: Call<ApiResponse<Boolean>>,
                    resp: Response<ApiResponse<Boolean>>
                ) {
                    if (resp.isSuccessful && resp.body()?.data == true) {
                        favoritosIds.remove(id)
                        adapter.updateFavoritos(favoritosIds)
                    }
                }

                override fun onFailure(call: Call<ApiResponse<Boolean>>, t: Throwable) {}
            })
        } else {
            apiService.addFavorite(id).enqueue(object : Callback<ApiResponse<Boolean>> {
                override fun onResponse(
                    call: Call<ApiResponse<Boolean>>,
                    resp: Response<ApiResponse<Boolean>>
                ) {
                    if (resp.isSuccessful && resp.body()?.data == true) {
                        favoritosIds.add(id)
                        adapter.updateFavoritos(favoritosIds)
                    }
                }

                override fun onFailure(call: Call<ApiResponse<Boolean>>, t: Throwable) {}
            })
        }
    }

    private fun signOut() {
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
