package com.example.buscaAnimeconexionspring.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.buscaAnimeconexionspring.R
import com.example.buscaAnimeconexionspring.adapter.PeliculaAdapter
import com.example.buscaAnimeconexionspring.api.PeliculaApiService
import com.example.buscaAnimeconexionspring.databinding.FragmentHomeBinding
import com.example.buscaAnimeconexionspring.model.Pelicula
import com.example.buscaAnimeconexionspring.service.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PeliculaAdapter
    private var peliculasList: MutableList<Pelicula> = mutableListOf()
    private lateinit var btnCargar: Button
    private lateinit var tvEstado: TextView
    private lateinit var apiService: PeliculaApiService

    companion object {
        private const val TAG = "HomeFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Inicializar componentes
        recyclerView = binding.root.findViewById(R.id.recyclerView)
        btnCargar = binding.root.findViewById(R.id.btnCargar)
        tvEstado = binding.root.findViewById(R.id.tvEstado)

        // Configurar RecyclerView con callbacks (HomeFragment solo muestra, no edita)
        adapter = PeliculaAdapter(
            peliculasList,
            onEditarClick = { pelicula ->
                Toast.makeText(
                    requireContext(),
                    "Para editar, ve a la pestaña Dashboard",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onEliminarClick = { pelicula ->
                Toast.makeText(
                    requireContext(),
                    "Para eliminar, ve a la pestaña Dashboard",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Inicializar API Service
        apiService = ApiClient.getApiService()

        // Configurar botón
        btnCargar.setOnClickListener {
            cargarPeliculas()
        }

        // Cargar películas al iniciar
        cargarPeliculas()

        return root
    }

    private fun cargarPeliculas() {
        tvEstado.text = "Cargando..."
        btnCargar.isEnabled = false

        val call = apiService.getAllPeliculas()

        call.enqueue(object : Callback<List<Pelicula>> {
            override fun onResponse(call: Call<List<Pelicula>>, response: Response<List<Pelicula>>) {
                btnCargar.isEnabled = true

                if (response.isSuccessful && response.body() != null) {
                    adapter.updatePeliculas(response.body()!!)
                    peliculasList = response.body()!!.toMutableList()

                    tvEstado.text = "Películas cargadas: ${peliculasList.size}"
                    Toast.makeText(
                        requireContext(),
                        "Se cargaron ${peliculasList.size} películas",
                        Toast.LENGTH_SHORT
                    ).show()

                    Log.d(TAG, "Películas cargadas: ${peliculasList.size}")
                } else {
                    tvEstado.text = "Error: ${response.code()}"
                    Toast.makeText(
                        requireContext(),
                        "Error al cargar: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "Error en respuesta: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Pelicula>>, t: Throwable) {
                btnCargar.isEnabled = true
                tvEstado.text = "Error de conexión"
                Toast.makeText(
                    requireContext(),
                    "Error de conexión: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e(TAG, "Error en petición", t)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}