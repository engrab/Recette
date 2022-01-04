package com.example.recetteapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ProductArrayAdapter extends ArrayAdapter<ProductModel> {

    List<ProductModel> modelList;
    Context context;
    private final LayoutInflater mInflater;

    // Constructors
    public ProductArrayAdapter(Context context, List<ProductModel> objects) {
        super(context, 0, objects);
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        modelList = objects;
    }

    @Override
    public ProductModel getItem(int position) {
        return modelList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder vh;
        if (convertView == null) {
            View view = mInflater.inflate(R.layout.layout_row_view, parent, false);
            vh = ViewHolder.create((RelativeLayout) view);
            view.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        ProductModel item = getItem(position);


        vh.id.setText(item.getId());
        vh.name.setText(item.getName());
        // one drive image . only put image id on image coulumn
        Glide.with(context).load("https://drive.google.com/uc?export=view&id="+item.getImages()).into(vh.image); // for one drive images ....
//        Glide.with(context).load(item.getImages()).into(vh.image);    // for other images

        vh.price.setText(String.valueOf(item.getPrice()));
        vh.quantity.setText(String.valueOf(item.getQuantity()));



        return vh.rootView;
    }


    private static class ViewHolder {
        public final RelativeLayout rootView;

        public final TextView id;
        public final TextView name;
        public final ImageView image;
        public final TextView price;
        public final TextView quantity;


        private ViewHolder(RelativeLayout rootView, TextView name, ImageView image,
                           TextView id, TextView price, TextView quantity) {
            this.rootView = rootView;

            this.id = id;
            this.name = name;
            this.image = image;
            this.price = price;
            this.quantity = quantity;



        }

        public static ViewHolder create(RelativeLayout rootView) {

            TextView itemName = rootView.findViewById(R.id.tvName);
            ImageView itemImage = rootView.findViewById(R.id.ivImage);

            TextView idtemQuantity = rootView.findViewById(R.id.tvQuantity);
            TextView itemPrice = rootView.findViewById(R.id.tvPrice);
            TextView itemId = rootView.findViewById(R.id.tvId);

            return new ViewHolder(rootView, itemName, itemImage, itemId, itemPrice, idtemQuantity);
        }
    }
}
