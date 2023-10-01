package com.example.mad_a2b;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ImagesViewHolder> {

    private ArrayList<Picture> images;
    private UploadImageViewModel uploadImageViewModel;
    private int numColumns; // Number of columns in the grid
    public ImagesAdapter(ArrayList<Picture> images, int numColumns, UploadImageViewModel uploadImageViewModel){

        this.images = images;
        this.numColumns = numColumns;
        this.uploadImageViewModel = uploadImageViewModel;
    }

    @NonNull
    @Override
    public ImagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        System.out.printf("WIdth is %d length is %d%n ", parent.getMeasuredHeight() ,numColumns);
        int size = (parent.getMeasuredHeight() );

        if (numColumns == 2) {

            // Set the height of the ImageView to half of the available height
            size = size/2;
        }
        return new ImagesViewHolder(view, size);
    }

    @Override
    public void onBindViewHolder(@NonNull ImagesViewHolder holder, int position) {
        Bitmap image = images.get(position).image;
        holder.imageView.setImageBitmap(image);
        System.out.printf("does this run");
//        if (numColumns == 2) {
//            // Set the height of the ImageView to half of the available height
//            ViewGroup.LayoutParams layoutParams = holder.imageView.getLayoutParams();
//            layoutParams.height = holder.itemView.getHeight() / 2;
//            holder.imageView.setLayoutParams(layoutParams);
//        }

    }

    @Override
    public int getItemCount() {
        return images.size();
    }
    public void setImages(ArrayList<Picture> newImages) {
        images.clear();
        images.addAll(newImages);
        this.notifyDataSetChanged(); // Notify the adapter that the data has changed
    }
    public void clearImages() {
        images.clear();
        this.notifyDataSetChanged(); // Notify the adapter that the data has changed
    }
    public void setNumColumns(int numColumns,ArrayList<Picture> newImages) {
        this.numColumns = numColumns;
        System.out.println("does this run");
        images.clear();
        images.addAll(newImages);
        this.notifyDataSetChanged(); // Notify the adapter that the data has changed
    }
    public class ImagesViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        Button button;

        public ImagesViewHolder(@NonNull View itemView, int size) {
            super(itemView);
            ViewGroup.LayoutParams lp = itemView.getLayoutParams();
            System.out.println(size);
            lp.height = size;
            imageView = itemView.findViewById(R.id.imageView); // Assuming you have an ImageView in your item layout
            button = itemView.findViewById(R.id.button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    uploadImageViewModel.setImage(images.get(getAdapterPosition()));

                }
            });
        }

    }

}