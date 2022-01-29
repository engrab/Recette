package com.example.recetteapp;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


public class MainActivity extends AppCompatActivity implements SelectItemListener{

    private static final String TAG = MainActivity.class.getName();

    private String[] titles = new String[]{"Food", "Drink"};
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private TextView tvCheckout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
        tvCheckout = findViewById(R.id.tvCheckout);
        Utils.checkOutList.clear();
        init();

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.checkOutList.size()>0){
                    startActivity(new Intent(MainActivity.this, CheckoutActivity.class));
                }else {
                    Toast.makeText(MainActivity.this, "Please Select Any Product", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private void init() {
        // removing toolbar elevation
        getSupportActionBar().setElevation(0);

        viewPager.setAdapter(new ViewPagerFragmentAdapter(this));

        // attaching tab mediator
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(titles[position])).attach();
    }

    @Override
    public void selectItem() {

        tvCheckout.setText(""+Utils.checkOutList.size());
    }

    private class ViewPagerFragmentAdapter extends FragmentStateAdapter {

        public ViewPagerFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new FoodFragment();
                case 1:
                    return new DrinkFragment();

            }
            return new FoodFragment();
        }

        @Override
        public int getItemCount() {
            return titles.length;
        }
    }

}