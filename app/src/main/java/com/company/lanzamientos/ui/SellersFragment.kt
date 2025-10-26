package com.company.lanzamientos.ui

import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.company.lanzamientos.R
import com.company.lanzamientos.data.KpiSeller


class SellersFragment : Fragment() {
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: android.os.Bundle?): View {
        val v = i.inflate(R.layout.fragment_list, c, false)
        val list = v.findViewById<LinearLayout>(R.id.listContainer)
        // placeholder
        val card = i.inflate(R.layout.item_kpi_card, list, false)
        card.findViewById<TextView>(R.id.tvTitle).text = "Vendedor ..."
        card.findViewById<TextView>(R.id.tvPct).text = "-- %"
        card.findViewById<TextView>(R.id.tvSub).text = "0 de 0"
        list.addView(card)
        return v
    }
    fun update(data: List<KpiSeller>) {
    val list = view?.findViewById<LinearLayout>(R.id.listContainer) ?: return
    list.removeAllViews()
    val inflater = LayoutInflater.from(context)
    data.forEach {
        val card = inflater.inflate(R.layout.item_kpi_card, list, false)
        val pct = if (it.inc + it.pend == 0) 0 else 100 * it.inc / (it.inc + it.pend)
        card.findViewById<TextView>(R.id.tvTitle).text = it.seller
        card.findViewById<TextView>(R.id.tvPct).text = "$pct %"
        card.findViewById<TextView>(R.id.tvSub).text = "${it.inc} / ${it.inc + it.pend}"
        list.addView(card)
    }
}

}
