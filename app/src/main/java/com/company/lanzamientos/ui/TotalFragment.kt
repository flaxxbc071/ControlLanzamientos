package com.company.lanzamientos.ui

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.company.lanzamientos.R

class TotalFragment : Fragment() {

    private var tvPct: TextView? = null
    private var tvDetail: TextView? = null

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        i.inflate(R.layout.fragment_total, c, false).apply {
            tvPct = findViewById(R.id.tvTotalPct)
            tvDetail = findViewById(R.id.tvTotalDetail)
            tvPct?.text = "-- %"
            tvDetail?.text = "0 de 0"
        }

    /** Método que actualiza el contenido desde MainActivity **/
    fun update(pct: Int, ok: Int, total: Int) {
        tvPct?.text = "$pct %"
        tvDetail?.text = "$ok de $total"
    }
}
