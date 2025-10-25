package com.company.lanzamientos.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.company.lanzamientos.R
import com.company.lanzamientos.data.RepoImpl
import kotlinx.coroutines.*

class TotalFragment : Fragment() {
    private val scope = MainScope()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_total, container, false)
    }

    override fun onResume() {
        super.onResume()
        val ctx = requireContext()
        val db = RepoImpl.create(ctx)
        val pctView = requireView().findViewById<TextView>(R.id.tvTotalPct)
        val detail = requireView().findViewById<TextView>(R.id.tvTotalDetail)
        val card = requireView().findViewById<View>(R.id.cardTotal)

        scope.launch(Dispatchers.IO) {
            val total = db.totalPairs()
            val inc = db.totalIncorporated()
            val pct = if (total == 0) 0.0 else (inc * 100.0 / total)

            withContext(Dispatchers.Main) {
                pctView.text = String.format("%.1f%%", pct)
                detail.text = "$inc de $total incorporados"
                card.setBackgroundColor(ContextCompat.getColor(ctx, colorForPct(pct)))
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
