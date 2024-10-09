package com.example.desafiodsm3.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.desafiodsm3.R
import com.example.desafiodsm3.model.Recurso
import com.example.desafiodsm3.network.RetrofitClient
import com.example.desafiodsm3.ui.ResourceActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddEditResourceActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etType: EditText
    private lateinit var etLink: EditText
    private lateinit var etImage: EditText
    private lateinit var btnSave: Button

    private var recursoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_resource)

        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        etType = findViewById(R.id.etType)
        etLink = findViewById(R.id.etLink)
        etImage = findViewById(R.id.etImage)
        btnSave = findViewById(R.id.btnSave)

        recursoId = intent.getStringExtra(ResourceActivity.EXTRA_RESOURCE_ID)

        if (recursoId != null) {
            // Modo edición
            title = "Editar Recurso"
            etTitle.setText(intent.getStringExtra(ResourceActivity.EXTRA_RESOURCE_TITULO))
            etDescription.setText(intent.getStringExtra(ResourceActivity.EXTRA_RESOURCE_DESCRIPCION))
            etType.setText(intent.getStringExtra(ResourceActivity.EXTRA_RESOURCE_TIPO))
            etLink.setText(intent.getStringExtra(ResourceActivity.EXTRA_RESOURCE_ENLACE))
            etImage.setText(intent.getStringExtra(ResourceActivity.EXTRA_RESOURCE_IMAGEN))
        } else {
            // Modo añadir
            title = "Añadir Recurso"
        }

        btnSave.setOnClickListener { saveResource() }
    }

    private fun saveResource() {
        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val type = etType.text.toString().trim()
        val link = etLink.text.toString().trim()
        val image = etImage.text.toString().trim()

        if (title.isEmpty() || description.isEmpty() || type.isEmpty() || link.isEmpty() || image.isEmpty()) {
            // Manejar error: campos vacíos
            return
        }

        val updatedRecurso = Recurso(
            id = recursoId ?: "",
            titulo = title,
            descripcion = description,
            tipo = type,
            enlace = link,
            imagen = image
        )

        if (recursoId == null) {
            // Crear nuevo recurso
            RetrofitClient.api.createRecurso(updatedRecurso).enqueue(object : Callback<Recurso> {
                override fun onResponse(call: Call<Recurso>, response: Response<Recurso>) {
                    if (response.isSuccessful) {
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        // Manejar error
                    }
                }

                override fun onFailure(call: Call<Recurso>, t: Throwable) {
                    // Manejar error de conexión
                }
            })
        } else {
            // Actualizar recurso existente
            RetrofitClient.api.updateRecurso(recursoId!!, updatedRecurso).enqueue(object : Callback<Recurso> {
                override fun onResponse(call: Call<Recurso>, response: Response<Recurso>) {
                    if (response.isSuccessful) {
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        // Manejar error
                    }
                }

                override fun onFailure(call: Call<Recurso>, t: Throwable) {
                    // Manejar error de conexión
                }
            })
        }
    }
}