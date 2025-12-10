package com.example.buscaAnimeconexionspring

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.buscaAnimeconexionspring.databinding.ActivityProfileBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var map: GoogleMap
    private val storage = FirebaseStorage.getInstance()
    
    // Ubicación seleccionada por el usuario
    private var selectedLocation: LatLng? = null
    
    // Launcher para permisos de ubicación
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted || coarseLocationGranted) {
            // Permisos concedidos, reintentar cargar el mapa
            initializeMap()
        }
    }

    // Launcher para seleccionar imagen
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                uploadImageToFirebase(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Datos de usuario
        loadUserData()

        binding.btnLogout.setOnClickListener { signOut() }
        binding.btnCambiarFoto.setOnClickListener {
            openImagePicker()
        }

        // Cargar ubicación guardada
        loadSavedLocation()
        
        // Inicializar mapa con verificación de permisos
        checkLocationPermissionsAndInitMap()

        // ====== BOTTOM NAVIGATION ======
        binding.bottomNavigation.selectedItemId = R.id.navigation_perfil
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_favoritos -> {
                    startActivity(Intent(this, FavoritosActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_emision -> {
                    startActivity(Intent(this, EmisionActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_mis_animes -> {
                    startActivity(Intent(this, MisAnimesActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_perfil -> {
                    // Ya estamos aquí
                    true
                }
                else -> false
            }
        }
        // =====================
    }
    
    private fun checkLocationPermissionsAndInitMap() {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        if (fineLocationPermission == PackageManager.PERMISSION_GRANTED || 
            coarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            initializeMap()
        } else {
            // Solicitar permisos
            locationPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }
    
    private fun initializeMap() {
        try {
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
            if (mapFragment != null) {
                mapFragment.getMapAsync(this)
            } else {
                Toast.makeText(this, "No se pudo cargar el mapa", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al cargar el mapa: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        
        // Configurar el mapa para mejor visualización
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        map.uiSettings.apply {
            isZoomControlsEnabled = true
            isZoomGesturesEnabled = true
            isScrollGesturesEnabled = true
            isTiltGesturesEnabled = true
            isRotateGesturesEnabled = true
            isMyLocationButtonEnabled = false
            isMapToolbarEnabled = true
        }
        
        // Cargar ubicación guardada o usar ubicación por defecto
        val location = selectedLocation ?: LatLng(37.6000, -0.9800) // Colegio Miralmonte por defecto
        
        // Mover cámara con animación para mejor experiencia
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        
        // Agregar marcador
        map.addMarker(
            MarkerOptions()
                .position(location)
                .title("Mi ubicación")
        )
        
        // Listener para cuando el usuario hace clic en el mapa
        map.setOnMapClickListener { latLng ->
            // Limpiar marcadores anteriores
            map.clear()
            
            // Agregar nuevo marcador
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Mi ubicación")
            )
            
            // Guardar la ubicación seleccionada
            selectedLocation = latLng
            saveLocation(latLng)
            
            // Mostrar coordenadas
            val coordenadas = "Lat: ${"%.4f".format(latLng.latitude)}, Lng: ${"%.4f".format(latLng.longitude)}"
            binding.tvUbicacionInfo.text = coordenadas
            
            Toast.makeText(
                this,
                "Ubicación guardada: $coordenadas",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun saveLocation(location: LatLng) {
        val sharedPref = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putFloat("latitude", location.latitude.toFloat())
            putFloat("longitude", location.longitude.toFloat())
            apply()
        }
    }
    
    private fun loadSavedLocation() {
        val sharedPref = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val lat = sharedPref.getFloat("latitude", 37.6000f).toDouble()
        val lng = sharedPref.getFloat("longitude", -0.9800f).toDouble()
        
        if (lat != 37.6000 || lng != -0.9800) {
            selectedLocation = LatLng(lat, lng)
            val coordenadas = "Lat: ${"%.4f".format(lat)}, Lng: ${"%.4f".format(lng)}"
            binding.tvUbicacionInfo.text = coordenadas
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser
        binding.tvNombre.text = user?.displayName ?: "Usuario"
        binding.tvEmail.text = user?.email ?: ""
        val photo = user?.photoUrl
        Glide.with(this)
            .load(photo)
            .placeholder(R.drawable.ic_person_placeholder)
            .circleCrop()
            .into(binding.imgAvatar)
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val user = auth.currentUser ?: return
        
        // Mostrar progreso
        binding.btnCambiarFoto.isEnabled = false
        binding.btnCambiarFoto.text = "Subiendo..."

        // Referencia al storage
        val storageRef = storage.reference
        val profileImageRef = storageRef.child("profile_images/${user.uid}.jpg")

        // Subir imagen
        profileImageRef.putFile(imageUri)
            .addOnSuccessListener {
                // Obtener URL de descarga
                profileImageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    updateUserProfile(downloadUri)
                }
            }
            .addOnFailureListener { e ->
                binding.btnCambiarFoto.isEnabled = true
                binding.btnCambiarFoto.text = "Cambiar foto"
                Toast.makeText(this, "Error al subir imagen: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserProfile(photoUri: Uri) {
        val user = auth.currentUser ?: return
        
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(photoUri)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                binding.btnCambiarFoto.isEnabled = true
                binding.btnCambiarFoto.text = "Cambiar foto"
                
                if (task.isSuccessful) {
                    Toast.makeText(this, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show()
                    // Actualizar la imagen en la vista
                    Glide.with(this)
                        .load(photoUri)
                        .placeholder(R.drawable.ic_person_placeholder)
                        .circleCrop()
                        .into(binding.imgAvatar)
                } else {
                    Toast.makeText(this, "Error al actualizar perfil", Toast.LENGTH_SHORT).show()
                }
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

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
