package com.example.desafiodsm3.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.desafiodsm3.R
import com.example.desafiodsm3.model.Recurso
import com.example.desafiodsm3.network.RetrofitClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResourceActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ResourceAdapter
    private lateinit var fabAddResource: FloatingActionButton
    private lateinit var searchView: SearchView
    private lateinit var btnClearSearch: Button

    private val searchJob = Job()
    private val searchScope = CoroutineScope(Dispatchers.Main + searchJob)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resource)

        recyclerView = findViewById(R.id.recyclerView)
        fabAddResource = findViewById(R.id.fabAddResource)
        searchView = findViewById(R.id.searchView)
        btnClearSearch = findViewById(R.id.btnClearSearch)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ResourceAdapter(emptyList(), this::onEditResource, this::onDeleteResource)
        recyclerView.adapter = adapter

        fabAddResource.setOnClickListener {
            val intent = Intent(this, AddEditResourceActivity::class.java)
            startActivityForResult(intent, ADD_RESOURCE_REQUEST)
        }

        setupSearchView()
        setupClearButton()
        fetchRecursos()
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchRecursoById(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchScope.launch {
                    delay(300) // Debounce de 300ms
                    newText?.let {
                        if (it.isNotEmpty()) {
                            searchRecursoById(it)
                        } else {
                            fetchRecursos()
                        }
                    }
                }
                return true
            }
        })
    }

    private fun setupClearButton() {
        btnClearSearch.setOnClickListener {
            searchView.setQuery("", false)
            searchView.clearFocus()
            fetchRecursos()
        }
    }

    private fun searchRecursoById(id: String) {
        RetrofitClient.api.getRecursoById(id).enqueue(object : Callback<Recurso> {
            override fun onResponse(call: Call<Recurso>, response: Response<Recurso>) {
                if (response.isSuccessful) {
                    val recurso = response.body()
                    recurso?.let {
                        updateRecyclerView(listOf(it))
                    } ?: run {
                        Toast.makeText(this@ResourceActivity, "Recurso no encontrado", Toast.LENGTH_SHORT).show()
                        updateRecyclerView(emptyList())
                    }
                } else {
                    Toast.makeText(this@ResourceActivity, "Error en la búsqueda: ${response.code()}", Toast.LENGTH_SHORT).show()
                    updateRecyclerView(emptyList())
                }
            }

            override fun onFailure(call: Call<Recurso>, t: Throwable) {
                Toast.makeText(this@ResourceActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                updateRecyclerView(emptyList())
            }
        })
    }

    private fun fetchRecursos() {
        RetrofitClient.api.getRecursos().enqueue(object : Callback<List<Recurso>> {
            override fun onResponse(call: Call<List<Recurso>>, response: Response<List<Recurso>>) {
                if (response.isSuccessful) {
                    val recursos = response.body() ?: emptyList()
                    updateRecyclerView(recursos)
                } else {
                    Log.e("ResourceActivity", "Error en la respuesta: ${response.code()}")
                    Toast.makeText(this@ResourceActivity, "Error al cargar recursos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Recurso>>, t: Throwable) {
                Log.e("ResourceActivity", "Error de conexión: ${t.message}")
                Toast.makeText(this@ResourceActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateRecyclerView(recursos: List<Recurso>) {
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        recyclerView.startAnimation(fadeIn)
        adapter.updateRecursos(recursos)
    }

    override fun onDestroy() {
        super.onDestroy()
        searchJob.cancel() // Cancelar el job de búsqueda cuando se destruye la actividad
    }

    private fun onEditResource(recurso: Recurso) {
        val intent = Intent(this, AddEditResourceActivity::class.java)
        intent.putExtra(EXTRA_RESOURCE_ID, recurso.id)
        intent.putExtra(EXTRA_RESOURCE_TITULO, recurso.titulo)
        intent.putExtra(EXTRA_RESOURCE_DESCRIPCION, recurso.descripcion)
        intent.putExtra(EXTRA_RESOURCE_TIPO, recurso.tipo)
        intent.putExtra(EXTRA_RESOURCE_ENLACE, recurso.enlace)
        intent.putExtra(EXTRA_RESOURCE_IMAGEN, recurso.imagen)
        startActivityForResult(intent, EDIT_RESOURCE_REQUEST)
    }


    private fun onDeleteResource(recurso: Recurso) {
        RetrofitClient.api.deleteRecurso(recurso.id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    fetchRecursos() // Recargar la lista después de eliminar
                } else {
                    Log.e("ResourceActivity", "Error al eliminar: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("ResourceActivity", "Error de conexión al eliminar: ${t.message}")
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                ADD_RESOURCE_REQUEST, EDIT_RESOURCE_REQUEST -> fetchRecursos()
            }
        }
    }

    companion object {
        const val ADD_RESOURCE_REQUEST = 1
        const val EDIT_RESOURCE_REQUEST = 2
        const val EXTRA_RESOURCE_ID = "extra_resource_id"
        const val EXTRA_RESOURCE_TITULO = "extra_resource_titulo"
        const val EXTRA_RESOURCE_DESCRIPCION = "extra_resource_descripcion"
        const val EXTRA_RESOURCE_TIPO = "extra_resource_tipo"
        const val EXTRA_RESOURCE_ENLACE = "extra_resource_enlace"
        const val EXTRA_RESOURCE_IMAGEN = "extra_resource_imagen"
    }
}