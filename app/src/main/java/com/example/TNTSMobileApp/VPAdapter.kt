package com.example.TNTSMobileApp

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class VPAdapter(fa: Modules): FragmentStateAdapter(fa) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> Grade8Fragment()
            1 -> Grade9Fragment()
            2 -> Grade10Fragment()
            else -> Grade8Fragment()
        }
    }
}
