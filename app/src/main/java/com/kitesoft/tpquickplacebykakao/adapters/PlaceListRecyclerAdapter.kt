package com.kitesoft.tpquickplacebykakao.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.kitesoft.tpquickplacebykakao.R
import com.kitesoft.tpquickplacebykakao.activities.PlaceUrlActivity
import com.kitesoft.tpquickplacebykakao.databinding.RecyclerItemListFragmentBinding
import com.kitesoft.tpquickplacebykakao.model.Place

class PlaceListRecyclerAdapter(val context:Context, var documents:MutableList<Place>) : RecyclerView.Adapter<PlaceListRecyclerAdapter.VH>() {

    inner class VH(itemView:View) : RecyclerView.ViewHolder(itemView){
        val binding:RecyclerItemListFragmentBinding= RecyclerItemListFragmentBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView:View= LayoutInflater.from(context).inflate(R.layout.recycler_item_list_fragment, parent, false)
        return VH(itemView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val place:Place= documents[position]

        holder.binding.tvPlaceName.text= place.place_name
        if(place.road_address_name=="") holder.binding.tvAddress.text= place.address_name
        else holder.binding.tvAddress.text= place.road_address_name
        holder.binding.tvDistance.text= "${place.distance}m"

        holder.itemView.setOnClickListener {
            //Toast.makeText(context, "${place.place_url}", Toast.LENGTH_SHORT).show()
            val intent: Intent = Intent(context, PlaceUrlActivity::class.java)
            intent.putExtra("place_url", place.place_url)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = documents.size
}