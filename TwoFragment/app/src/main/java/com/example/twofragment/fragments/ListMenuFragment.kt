package com.example.twofragment.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.twofragment.R
import com.example.twofragment.adapter.DogCardAdapter
import com.example.twofragment.data.DataSource
import com.example.twofragment.databinding.FragmentListitemsInfoBinding
import com.example.twofragment.model.Dog

class ListMenuFragment : Fragment() {
    private lateinit var binding: FragmentListitemsInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListitemsInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.listItemTest.adapter = DogCardAdapter(context, this)
    }

    fun selectDog(dog: Dog) {
        var detailsFragment =
            fragmentManager?.findFragmentById(R.id.fragmentDetail) as DetailsFragment
        detailsFragment.setDog(dog)
    }
}