package com.eduar2tc.calculator.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eduar2tc.calculator.R;
import com.eduar2tc.calculator.model.Calculation;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<Calculation> calculations;

    public HistoryAdapter(List<Calculation> calculations) {
        this.calculations = calculations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Calculation calculation = calculations.get(position);
        holder.expression.setText(calculation.getExpression());
        holder.result.setText(calculation.getResult());
    }

    @Override
    public int getItemCount() {
        return calculations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView expression;
        TextView result;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            expression = itemView.findViewById(R.id.expression);
            result = itemView.findViewById(R.id.result);
        }
    }
}
