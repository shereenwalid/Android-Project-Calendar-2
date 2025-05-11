package com.example.dailycalendar;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.carousel.MaskableFrameLayout;

import java.util.List;

public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.ViewHolder> {

    private final List<CarouselItem> carouselItems; // Changed from int[] to List<CarouselItem>

    public CarouselAdapter(List<CarouselItem> carouselItems) {
        this.carouselItems = carouselItems;
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView imageView;

        public ViewHolder(MaskableFrameLayout view) {
            super(view);
            imageView = view.findViewById(R.id.carousel_image_view);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MaskableFrameLayout view = (MaskableFrameLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.carousel_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CarouselItem item = carouselItems.get(position);
        holder.imageView.setImageResource(item.getImageResId());
        // If you have a TextView for titles in carousel_item.xml, you can add:
        // TextView titleTextView = holder.itemView.findViewById(R.id.event_title);
        // titleTextView.setText(item.getTitle());
    }

    @Override
    public int getItemCount() {
        return carouselItems.size();
    }
}