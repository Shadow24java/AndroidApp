package com.example.buscaAnimeconexionspring.ui.dashboard

import android.app.AlertDialog
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
import com.example.buscaAnimeconexionspring.databinding.FragmentDashboardBinding
import com.example.buscaAnimeconexionspring.model.Pelicula
import com.example.buscaAnimeconexionspring.service.ApiClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PeliculaAdapter
    private var peliculasList: MutableList<Pelicula> = mutableListOf()
    private lateinit var btnCargar: Button
    private lateinit var tvEstado: TextView
    private lateinit var fabAgregar: FloatingActionButton
    private lateinit var apiService: PeliculaApiService

    companion object {
        private const val TAG = "DashboardFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Inicializar componentes
        recyclerView = root.findViewById(R.id.recyclerView) 
            ?: throw IllegalStateException("recyclerView no encontrado en el layout")
        btnCargar = root.findViewById(R.id.btnCargar) 
            ?: throw IllegalStateException("btnCargar no encontrado en el layout")
        tvEstado = root.findViewById(R.id.tvEstado) 
            ?: throw IllegalStateException("tvEstado no encontrado en el layout")
        fabAgregar = root.findViewById(R.id.fabAgregar) 
            ?: throw IllegalStateException("fabAgregar no encontrado en el layout")

        // Inicializar API Service
        apiService = ApiClient.getApiService()

        // Configurar RecyclerView con callbacks
        adapter = PeliculaAdapter(
            peliculasList,
            onEditarClick = { pelicula -> mostrarDialogoEditar(pelicula) },
            onEliminarClick = { pelicula -> mostrarDialogoEliminar(pelicula) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Configurar botones
        btnCargar.setOnClickListener {
            cargarPeliculas()
        }

        fabAgregar.setOnClickListener {
            mostrarDialogoCrear()
        }

        // Cargar películas al iniciar
        cargarPeliculas()

        return root
    }

    // READ - Cargar todas las películas
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

    // CREATE - Mostrar diálogo para crear nueva película
    private fun mostrarDialogoCrear() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_pelicula, null)

        val etTitulo: TextInputEditText = dialogView.findViewById(R.id.etTitulo)
        val etGenero: TextInputEditText = dialogView.findViewById(R.id.etGenero)
        val etAnio: TextInputEditText = dialogView.findViewById(R.id.etAnio)
        val etDuracion: TextInputEditText = dialogView.findViewById(R.id.etDuracion)
        val etDirector: TextInputEditText = dialogView.findViewById(R.id.etDirector)
        val btnGuardar: Button = dialogView.findViewById(R.id.btnGuardar)
        val btnCancelar: Button = dialogView.findViewById(R.id.btnCancelar)
        val tvTituloDialog: TextView = dialogView.findViewById(R.id.tvTituloDialog)

        tvTituloDialog.text = "Nueva Película"

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnGuardar.setOnClickListener {
            val titulo = etTitulo.text.toString().trim()
            val genero = etGenero.text.toString().trim()
            val anioStr = etAnio.text.toString().trim()
            val duracionStr = etDuracion.text.toString().trim()
            val director = etDirector.text.toString().trim()

            if (validarCampos(titulo, genero, anioStr, duracionStr, director)) {
                val nuevaPelicula = Pelicula(
                    titulo = titulo,
                    genero = genero,
                    anio = anioStr.toInt(),
                    duracion = duracionStr.toInt(),
                    director = director
                )
                crearPelicula(nuevaPelicula)
                dialog.dismiss()
            }
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // UPDATE - Mostrar diálogo para editar película
    private fun mostrarDialogoEditar(pelicula: Pelicula) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_pelicula, null)

        val etTitulo: TextInputEditText = dialogView.findViewById(R.id.etTitulo)
        val etGenero: TextInputEditText = dialogView.findViewById(R.id.etGenero)
        val etAnio: TextInputEditText = dialogView.findViewById(R.id.etAnio)
        val etDuracion: TextInputEditText = dialogView.findViewById(R.id.etDuracion)
        val etDirector: TextInputEditText = dialogView.findViewById(R.id.etDirector)
        val btnGuardar: Button = dialogView.findViewById(R.id.btnGuardar)
        val btnCancelar: Button = dialogView.findViewById(R.id.btnCancelar)
        val tvTituloDialog: TextView = dialogView.findViewById(R.id.tvTituloDialog)

        tvTituloDialog.text = "Editar Película"

        // Precargar datos
        etTitulo.setText(pelicula.titulo ?: "")
        etGenero.setText(pelicula.genero ?: "")
        etAnio.setText(pelicula.anio?.toString() ?: "")
        etDuracion.setText(pelicula.duracion?.toString() ?: "")
        etDirector.setText(pelicula.director ?: "")

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnGuardar.setOnClickListener {
            val titulo = etTitulo.text.toString().trim()
            val genero = etGenero.text.toString().trim()
            val anioStr = etAnio.text.toString().trim()
            val duracionStr = etDuracion.text.toString().trim()
            val director = etDirector.text.toString().trim()

            if (validarCampos(titulo, genero, anioStr, duracionStr, director)) {
                val peliculaActualizada = Pelicula(
                    idPelicula = pelicula.idPelicula,
                    titulo = titulo,
                    genero = genero,
                    anio = anioStr.toInt(),
                    duracion = duracionStr.toInt(),
                    director = director
                )
                actualizarPelicula(peliculaActualizada)
                dialog.dismiss()
            }
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // DELETE - Mostrar diálogo de confirmación para eliminar
    private fun mostrarDialogoEliminar(pelicula: Pelicula) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Película")
            .setMessage("¿Estás seguro de que deseas eliminar '${pelicula.titulo}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarPelicula(pelicula)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // CREATE - Crear nueva película
    private fun crearPelicula(pelicula: Pelicula) {
        val call = apiService.createPelicula(pelicula)

        call.enqueue(object : Callback<Pelicula> {
            override fun onResponse(call: Call<Pelicula>, response: Response<Pelicula>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Película creada exitosamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(TAG, "Película creada: ${response.body()?.titulo}")
                    cargarPeliculas() // Recargar lista
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error al crear: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "Error al crear película: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Pelicula>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Error de conexión: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e(TAG, "Error en petición de creación", t)
            }
        })
    }

    // UPDATE - Actualizar película existente
    private fun actualizarPelicula(pelicula: Pelicula) {
        val call = apiService.updatePelicula(pelicula)

        call.enqueue(object : Callback<Pelicula> {
            override fun onResponse(call: Call<Pelicula>, response: Response<Pelicula>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Película actualizada exitosamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(TAG, "Película actualizada: ${response.body()?.titulo}")
                    cargarPeliculas() // Recargar lista
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error al actualizar: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "Error al actualizar película: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Pelicula>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Error de conexión: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e(TAG, "Error en petición de actualización", t)
            }
        })
    }

    // DELETE - Eliminar película
    private fun eliminarPelicula(pelicula: Pelicula) {
        val id = pelicula.idPelicula ?: return
        
        val call = apiService.deletePelicula(id)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Película eliminada exitosamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(TAG, "Película eliminada: ${pelicula.titulo}")
                    cargarPeliculas() // Recargar lista
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error al eliminar: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "Error al eliminar película: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Error de conexión: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e(TAG, "Error en petición de eliminación", t)
            }
        })
    }

    // Validar campos del formulario
    private fun validarCampos(
        titulo: String,
        genero: String,
        anio: String,
        duracion: String,
        director: String
    ): Boolean {
        if (titulo.isEmpty()) {
            Toast.makeText(requireContext(), "El título es obligatorio", Toast.LENGTH_SHORT).show()
            return false
        }
        if (genero.isEmpty()) {
            Toast.makeText(requireContext(), "El género es obligatorio", Toast.LENGTH_SHORT).show()
            return false
        }
        if (anio.isEmpty()) {
            Toast.makeText(requireContext(), "El año es obligatorio", Toast.LENGTH_SHORT).show()
            return false
        }
        try {
            val anioInt = anio.toInt()
            if (anioInt < 1900 || anioInt > 2100) {
                Toast.makeText(requireContext(), "El año debe ser válido", Toast.LENGTH_SHORT).show()
                return false
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "El año debe ser un número", Toast.LENGTH_SHORT).show()
            return false
        }
        if (duracion.isEmpty()) {
            Toast.makeText(requireContext(), "La duración es obligatoria", Toast.LENGTH_SHORT).show()
            return false
        }
        try {
            val duracionInt = duracion.toInt()
            if (duracionInt <= 0) {
                Toast.makeText(requireContext(), "La duración debe ser mayor a 0", Toast.LENGTH_SHORT).show()
                return false
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "La duración debe ser un número", Toast.LENGTH_SHORT).show()
            return false
        }
        if (director.isEmpty()) {
            Toast.makeText(requireContext(), "El director es obligatorio", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
