package com.kitesoft.tpquickplacebykakao.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kitesoft.tpquickplacebykakao.activities.MainActivity
import com.kitesoft.tpquickplacebykakao.adapters.PlaceListRecyclerAdapter
import com.kitesoft.tpquickplacebykakao.databinding.FragmentListBinding

class ListFragment : Fragment() {

    val binding:FragmentListBinding by lazy { FragmentListBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setPlaceListRecyclerAdapter()
    }

    fun setPlaceListRecyclerAdapter(){
        val ma= activity as MainActivity

        //아직 MainActivity의 파싱작업이 완료되지 않았다면 데이터가 없음.
        if(ma.searchPlaceResponse==null) return

        binding.recyclerview.adapter= PlaceListRecyclerAdapter(requireContext(), ma.searchPlaceResponse!!.documents)
    }
}