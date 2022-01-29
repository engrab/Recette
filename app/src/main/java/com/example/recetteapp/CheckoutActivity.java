package com.example.recetteapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class CheckoutActivity extends AppCompatActivity {
    private RecyclerView rv;
    private CheckoutAdapter adapter;
    private Button btnCheckout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        rv = findViewById(R.id.rv);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CheckoutActivity.this, ScannerActivity.class));
            }
        });

        adapter = new CheckoutAdapter(this, Utils.checkOutList);


        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            rv.setLayoutManager(new GridLayoutManager(this, 1));
            rv.setAdapter(adapter);

        } else {
            rv.setLayoutManager(new GridLayoutManager(this, 2));
            rv.setAdapter(adapter);

        }
    }


}