package com.example.groots;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import javax.annotation.Nullable;

public class HistoryFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private HistoryAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.history_fragment_layout, container, false);
        super.onCreate(savedInstanceState);

        tabLayout = (TabLayout) view.findViewById(R.id.tabLayoutId);
        viewPager = (ViewPager) view.findViewById(R.id.viewPagerId);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());

        adapter.addFragment(new RadiationExposureHistoryFragment(), "Radiation exposure");
        adapter.addFragment(new WorkedHoursHistoryFragment(), "Worked hours");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

}
