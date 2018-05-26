package com.example.kisha.travelandentertainmentsearch;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private SectionsPageAdapter sectionsPageAdapter;

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        viewPager = (ViewPager)findViewById(R.id.container);
        setUpViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        int[] tabIcons = new int[]{R.drawable.search,R.drawable.heart_fill_white};
        for(int i =0 ;i <2;++i){
            LinearLayout tabLinearLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
            TextView tabContent = (TextView) tabLinearLayout.findViewById(R.id.tabContent);
            Log.d(TAG,tabContent.toString());
            tabContent.setText(sectionsPageAdapter.getPageTitle(i));
            tabContent.setCompoundDrawablesWithIntrinsicBounds(tabIcons[i], 0, 0, 0);
            tabLayout.getTabAt(i).setCustomView(tabContent);
        }
    }

    private void setUpViewPager(ViewPager viewPager){
        sectionsPageAdapter.addFragment(new SearchFragment()," SEARCH");
        sectionsPageAdapter.addFragment(new FavoritesFragment()," FAVORITES");
        viewPager.setAdapter(sectionsPageAdapter);
    }
}
