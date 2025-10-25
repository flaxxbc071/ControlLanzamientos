package com.company.lanzamientos.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.company.lanzamientos.R
import com.company.lanzamientos.data.RepoImpl
import kotlinx.coroutines.*

class SellersFragment : Fragment() {
    private val scope = MainScope()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: android.os.Bundle?): View =
        i.inflate(R.layout.fragment_list, c, false)

    override fun onResume() {
        super.onResume()
        val ctx = requireContext()
        val db = RepoImpl.create(ctx)
        val container = requireView().findViewById<LinearLayout>(R.id.listContainer)
        container.removeAllViews()

        scope.launch(Dispatchers.IO) {
            val sellers = db.perSeller()
            withContext(Dispatchers.Main) {
                val inflater = LayoutInflater.from(ctx)
                sellers.forEach {
                    val v = inflater.inflate(R.layout.item_kpi_card, container, false)
                    val total = it.inc + it.pend
                    val pct = if (total == 0) 0.0 else (it.inc * 100.0 / total)

                    (v.findViewById<TextView>(R.id.tvTitle)).text = it.seller
                    (v.findViewById<TextView>(R.id.tvPct)).text = String.format("%.1f%%", pct)
                    (v.findViewById<TextView>(R.id.tvSub)).text = "${it.inc} de $total incorporados"
                    v.setBackgroundColor(ContextCompat.getColor(ctx, colorForPct(pct)))
                    container.addView(v)
                }
            }
        }
    }

    private fun colorForPct(p: Double): Int =
        when {
            p >= 80 -> R.color.kpiGreen
            p >= 50 -> R.color.kpiYellow
            else -> R.color.kpiRed
        }

    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
    }
}
