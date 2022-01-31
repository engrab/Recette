package com.example.recetteapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
    private final SelectItemListener selectItemListener;

    public ProductArrayAdapter(Context context, List<ProductModel> modelList) {
        this.modelList = modelList;
        this.context = context;
        selectItemListener = (SelectItemListener) context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_menu_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ViewHolder){
            ProductModel productModel = modelList.get(position);

            ((ViewHolder) holder).ivSelected.setVisibility(productModel.isSelected() ? View.VISIBLE : View.INVISIBLE);

            ((ViewHolder) holder).name.setText(modelList.get(position).getName());
            ((ViewHolder) holder).price.setText(" $ "+modelList.get(position).getPrice());

            // one drive image . only put image id on image coulumn
            Glide.with(context).load("https://drive.google.com/uc?export=view&id="+modelList.get(position).getImages()).into(((ViewHolder) holder).image); // for one drive images ....
//        Glide.with(context).load(item.getImages()).into(holder.image);    // for other images
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!productModel.isSelected()) {

                        productModel.setSelected(true);
                        Utils.checkOutList.add(modelList.get(position));
                        selectItemListener.selectItem();
                        ((ViewHolder) holder).ivSelected.setVisibility(productModel.isSelected() ? View.VISIBLE : View.INVISIBLE);

                    }else {
                        productModel.setSelected(false);
                        Utils.checkOutList.remove(modelList.get(position));
                        selectItemListener.selectItem();
                        ((ViewHolder) holder).ivSelected.setVisibility(productModel.isSelected() ? View.VISIBLE : View.INVISIBLE);
                    }
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
        public ImageView ivSelected;
        View view;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;

            name = itemView.findViewById(R.id.tvMenuTitle);
            image = itemView.findViewById(R.id.ivMenu);
            price = itemView.findViewById(R.id.tvMenuPrice);
            ivSelected = itemView.findViewById(R.id.ivSelected);
        }

    }


}
