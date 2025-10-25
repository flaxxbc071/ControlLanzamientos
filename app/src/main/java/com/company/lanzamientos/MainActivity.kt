package com.company.lanzamientos

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.company.lanzamientos.data.RepoImpl
import com.company.lanzamientos.importer.ExcelImporter
import com.company.lanzamientos.ui.ProductsFragment
import com.company.lanzamientos.ui.SellersFragment
import com.company.lanzamientos.ui.TabsAdapter
import com.company.lanzamientos.ui.TotalFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var status: TextView
    private lateinit var repo: RepoImpl

    private val openDoc = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            if (uri != null) {
                importExcel(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repo = RepoImpl.create(this)
        status = findViewById(R.id.txtStatus)

        // Botón importar
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

        showLastBatch()
    }

    private fun pickXlsx() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        }
        openDoc.launch(intent)
    }

    private fun importExcel(uri: Uri) {
        lifecycleScope.launch {
            status.text = "Importando..."
            try {
                withContext(Dispatchers.IO) {
                    ExcelImporter.importWorkbook(this@MainActivity, uri, repo)
                }
                status.text = "Importación completa ✅"
                refreshDashboard()
                showLastBatch()
            } catch (e: Exception) {
                status.text = "Error: ${e.message}"
            }
        }
    }

    private fun refreshDashboard() {
        lifecycleScope.launch(Dispatchers.IO) {
            val totalPairs = repo.totalPairs()
            val totalIncorporated = repo.totalIncorporated()
            val sellers = repo.perSeller()
            val products = repo.perProduct()

            withContext(Dispatchers.Main) {
                // Fragment Total
                val totalPct = if (totalPairs == 0) 0 else (100 * totalIncorporated / totalPairs)
                val totalFrag = supportFragmentManager.fragments.find { it is TotalFragment } as? TotalFragment
                totalFrag?.update(totalPct, totalIncorporated, totalPairs)

                // Fragment Vendedores
                val sellersFrag = supportFragmentManager.fragments.find { it is SellersFragment } as? SellersFragment
                sellersFrag?.update(sellers)

                // Fragment Productos
                val productsFrag = supportFragmentManager.fragments.find { it is ProductsFragment } as? ProductsFragment
                productsFrag?.update(products)
            }
        }
    }

    private fun showLastBatch() {
        lifecycleScope.launch {
            val b = withContext(Dispatchers.IO) { repo.lastBatch() }
            b?.let {
                val fecha = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(it.importedAt))
                status.text = "Última actualización: ${it.filename} · $fecha"
            }
        }
    }
}

