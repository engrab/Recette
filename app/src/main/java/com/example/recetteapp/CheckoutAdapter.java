package com.example.recetteapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CheckoutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<ProductModel> modelList;
    Context context;

    public CheckoutAdapter(Context context, List<ProductModel> modelList) {
        this.modelList = modelList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_checkout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ViewHolder){
            ((ViewHolder) holder).name.setText(modelList.get(position).getName());
            ((ViewHolder) holder).price.setText(" $ "+modelList.get(position).getPrice());

            // one drive image . only put image id on image coulumn
            Glide.with(context).load("https://drive.google.com/uc?export=view&id="+modelList.get(position).getImages()).into(((ViewHolder) holder).image); // for one drive images ....
//        Glide.with(context).load(item.getImages()).into(holder.image);    // for other images

            ((ViewHolder) holder).ivRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    modelList.remove(position);
                    notifyDataSetChanged();
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
        public  CircleImageView ivRemove;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.tvMenuTitle);
            image = itemView.findViewById(R.id.ivMenu);
            price = itemView.findViewById(R.id.tvMenuPrice);
            ivRemove = itemView.findViewById(R.id.ivRemove);
        }

    }
}
