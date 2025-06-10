package com.example.fitnessup.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessup.R;
import com.example.fitnessup.data.model.WeightProgress;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying weight history entries in a RecyclerView.
 */
public class WeightHistoryAdapter extends RecyclerView.Adapter<WeightHistoryAdapter.ViewHolder> {
    private List<WeightProgress> weightProgressList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault());
    private final DecimalFormat weightFormat = new DecimalFormat("#.#");

    public WeightHistoryAdapter(List<WeightProgress> weightProgressList) {
        this.weightProgressList = weightProgressList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weight_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeightProgress progress = weightProgressList.get(position);
        
        // Format and display weight
        String weightText = weightFormat.format(progress.getWeight()) + " kg";
        holder.weightTextView.setText(weightText);
        
        // Format and display date
        String dateText = dateFormat.format(progress.getDate());
        holder.dateTextView.setText(dateText);
        
        // Calculate and display difference from previous entry
        if (position < weightProgressList.size() - 1) {
            WeightProgress previousProgress = weightProgressList.get(position + 1);
            double difference = progress.getWeight() - previousProgress.getWeight();
            
            String differenceText;
            if (Math.abs(difference) < 0.1) {
                differenceText = "Tetap";
            } else if (difference > 0) {
                differenceText = "+" + weightFormat.format(difference) + " kg";
                holder.differenceTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.colorWeightGain));
            } else {
                differenceText = weightFormat.format(difference) + " kg";
                holder.differenceTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.colorWeightLoss));
            }
            
            holder.differenceTextView.setText(differenceText);
            holder.differenceTextView.setVisibility(View.VISIBLE);
        } else {
            // This is the first entry (oldest), no difference to show
            holder.differenceTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return weightProgressList.size();
    }
    
    /**
     * Update the adapter with new data
     * @param newData New list of weight progress entries
     */
    public void updateData(List<WeightProgress> newData) {
        this.weightProgressList = newData;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for weight history items
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView weightTextView;
        TextView dateTextView;
        TextView differenceTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            weightTextView = itemView.findViewById(R.id.weightTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            differenceTextView = itemView.findViewById(R.id.differenceTextView);
        }
    }
}