package com.example.meteomind

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private val fragments = mutableListOf<Fragment>(
        LoadingFragment(),
        LoadingFragment()
    )

    private val fragmentIds = longArrayOf(0, 1)

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    override fun getItemId(position: Int): Long {
        return fragmentIds[position]
    }

    override fun containsItem(itemId: Long): Boolean {
        return fragmentIds.contains(itemId)
    }

    fun updateFragment(position: Int, fragment: Fragment) {
        fragments[position] = fragment
        fragmentIds[position] = System.currentTimeMillis()
        notifyItemChanged(position)
    }
}