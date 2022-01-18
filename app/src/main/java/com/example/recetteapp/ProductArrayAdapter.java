package com.example.recetteapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProductArrayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<ProductModel> modelList;
    Context context;

    public ProductArrayAdapter(Context context, List<ProductModel> modelList) {
        this.modelList = modelList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_menu_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ViewHolder){
            ((ViewHolder) holder).name.setText(modelList.get(position).getName());
            ((ViewHolder) holder).price.setText(" $ "+modelList.get(position).getPrice());
            ((ViewHolder) holder).quantity.setText(modelList.get(position).getQuantity()+" ITEMS");

            // one drive image . only put image id on image coulumn
            Glide.with(context).load("https://drive.google.com/uc?export=view&id="+modelList.get(position).getImages()).into(((ViewHolder) holder).image); // for one drive images ....
//        Glide.with(context).load(item.getImages()).into(holder.image);    // for other images
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(context, ScannerActivity.class);
                    intent.putExtra("pos", position);
                    context.startActivity(intent);
                }
            });
        }


    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder{


        public  TextView name;
        public CircleImageView image;
        public  TextView price;
        public  TextView quantity;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.tvMenuTitle);
            image = itemView.findViewById(R.id.ivMenu);
            quantity = itemView.findViewById(R.id.tvMenuItems);
            price = itemView.findViewById(R.id.tvMenuPrice);
        }

    }
}
