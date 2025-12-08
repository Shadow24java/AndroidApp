package com.example.buscaAnimeconexionspring

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.buscaAnimeconexionspring.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class Login : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var googleSignInClient : GoogleSignInClient

    // Launcher para el resultado del intent de Google
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                auth.signInWithCredential(credential)
                    .addOnCompleteListener(this) { taskAuth ->
                        if (taskAuth.isSuccessful) {
                            goToMain()
                        } else {
                            Toast.makeText(
                                this,
                                "Error con Google: ${taskAuth.exception?.localizedMessage}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } catch (e: ApiException) {
                Toast.makeText(this, "Error al obtener cuenta de Google", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Inicio con Google cancelado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
            }else {
                loginUser(email, password)
            }
        }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            goToMain()
        }
    }
    private fun loginUser(email:String, password:String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful) {
                    goToMain()
                }else {
                    Toast.makeText(this, "Error al iniciar sesión: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}