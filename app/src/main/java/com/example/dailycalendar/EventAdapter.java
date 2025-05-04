package com.example.dailycalendar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;

    private OnEventActionListener listener;

    public EventAdapter(List<Event> eventList, OnEventActionListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }


    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    public interface OnEventActionListener {
        void onUpdate(Event event);
        void onDelete(Event event);
    }


    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.eventTitle.setText(event.getTitle());
        holder.eventDescription.setText(event.getDescription());
        holder.eventDate.setText(event.getDate());

        holder.btnUpdate.setOnClickListener(v -> listener.onUpdate(event));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(event));
    }





    class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventTitle, eventDescription, eventDate;
        Button btnUpdate, btnDelete;

        public EventViewHolder(View itemView) {
            super(itemView);
            eventTitle = itemView.findViewById(R.id.tvEventTitle);
            eventDescription = itemView.findViewById(R.id.tvEventDescription);
            eventDate = itemView.findViewById(R.id.tvEventDate);
            btnUpdate = itemView.findViewById(R.id.btnUpdate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }


    }
    public int getItemCount() {
        return eventList.size();
    }
}
