package com.example.buscaAnimeconexionspring.ui.dashboard

import AnimeAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.buscaAnimeconexionspring.R
import com.example.buscaAnimeconexionspring.api.AnimeApiService
import com.example.buscaAnimeconexionspring.model.Anime
import com.example.buscaAnimeconexionspring.model.ApiResponse
import com.example.buscaAnimeconexionspring.service.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AnimeAdapter
    private val animesList = mutableListOf<Anime>()
    private lateinit var btnCargar: Button
    private lateinit var tvEstado: TextView
    private lateinit var apiService: AnimeApiService

    companion object { private const val TAG = "DashboardFragment" }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)

        recyclerView = root.findViewById(R.id.recyclerView)
        btnCargar = root.findViewById(R.id.btnCargar)
        tvEstado = root.findViewById(R.id.tvEstado)

        apiService = ApiClient.getAnimeApiService()

        adapter = AnimeAdapter(
            animes = animesList,
            onClick = { /* abre detalle */ },
            onToggleFavoritos = { /* añade/quita favoritos */ },
            favoritos = mutableListOf()
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        btnCargar.setOnClickListener { cargarAnimes() }
        cargarAnimes()

        return root
    }

    private fun cargarAnimes() {
        tvEstado.text = "Cargando..."
        btnCargar.isEnabled = false

        // Si tu API devuelve ApiResponse<List<Anime>> usa esta firma:
        val call: Call<ApiResponse<List<Anime>>> = apiService.getAll()
        // Si devuelve List<Anime> directo: val call: Call<List<Anime>> = apiService.getAll()

        call.enqueue(object : Callback<ApiResponse<List<Anime>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<Anime>>>,
                response: Response<ApiResponse<List<Anime>>>
            ) {
                btnCargar.isEnabled = true
                val body = response.body()
                val data = body?.data
                if (response.isSuccessful && data != null) {
                    animesList.clear()
                    animesList.addAll(data)
                    adapter.notifyDataSetChanged()
                    tvEstado.text = "Animes cargados: ${animesList.size}"
                    Toast.makeText(requireContext(), "Cargados ${animesList.size}", Toast.LENGTH_SHORT).show()
                } else {
                    tvEstado.text = "Error: ${response.code()}"
                    Toast.makeText(requireContext(), "Error al cargar: ${response.code()}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Error en respuesta: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<Anime>>>, t: Throwable) {
                btnCargar.isEnabled = true
                tvEstado.text = "Error de conexión"
                Toast.makeText(requireContext(), "Error de conexión: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Error en petición", t)
            }
        })
    }
}
