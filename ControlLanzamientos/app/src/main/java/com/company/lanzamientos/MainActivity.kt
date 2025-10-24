package com.company.lanzamientos

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.company.lanzamientos.data.Repo
import com.company.lanzamientos.data.RepoImpl
import com.company.lanzamientos.importer.ExcelImporter
import com.company.lanzamientos.ui.ProductsFragment
import com.company.lanzamientos.ui.SellersFragment
import com.company.lanzamientos.ui.TabsAdapter
import com.company.lanzamientos.ui.TotalFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

// luego de importar
ExcelImporter.importWorkbook(this@MainActivity, uri, repo)
status.text = "Importación OK"
refreshDashboard()
showLastBatch()

// también podés llamar showLastBatch() en onCreate() después de setear la UI

class MainActivity : AppCompatActivity() {

    private lateinit var status: TextView
    private lateinit var repoImpl: RepoImpl
    private val repo: Repo get() = repoImpl

    private val openDoc = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            if (uri != null) importXlsx(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        status = findViewById(R.id.txtStatus)
        repoImpl = RepoImpl.create(this)

        findViewById<Button>(R.id.btnImport).setOnClickListener { pickXlsx() }

        // Tabs
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val fragments = listOf(TotalFragment(), SellersFragment(), ProductsFragment())
        viewPager.adapter = TabsAdapter(this, fragments)
        TabLayoutMediator(tabLayout, viewPager) { tab, pos ->
            tab.text = when (pos) {
                0 -> "Total"
                1 -> "Vendedores"
                else -> "Productos"
            }
        }.attach()
    }

    private fun pickXlsx() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        }
        openDoc.launch(intent)
    }

    private fun importXlsx(uri: Uri) {
        lifecycleScope.launch {
            status.text = "Importando…"
            try {
                ExcelImporter.importWorkbook(this@MainActivity, uri, repo)
                status.text = "Importación OK"
                // Los fragments se refrescan en onResume automáticamente
            } catch (e: Exception) {
                status.text = "Error: ${e.message}"
            }
        }
    }
    private fun showLastBatch() {
    lifecycleScope.launch {
        val b = withContext(Dispatchers.IO) { repoImpl.lastBatch() }
        b?.let {
            // si nombrás el archivo con la semana (ej: lanzamientos_2025-W43.xlsx) queda clarísimo
            val fecha = java.text.SimpleDateFormat("dd/MM").format(java.util.Date(it.importedAt))
            findViewById<TextView>(R.id.txtStatus).text =
                "Última actualización: ${it.filename} · $fecha"
        }
    }
}

}
