package com.example.twofragment.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.twofragment.R
import com.example.twofragment.databinding.FragmentDetailsInfoBinding
import com.example.twofragment.model.Dog

class DetailsFragment : Fragment() {
    private lateinit var binding: FragmentDetailsInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailsInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun setDog(dog: Dog) {
        binding.apply {
            imageView.setImageResource(dog.imageResourceId)
            tvName.text = dog.name
            tvAge.text = getString(R.string.dog_age, dog.age)
            tvHobbies.text = getString(R.string.dog_hobbies, dog.hobbies)
        }
    }
}