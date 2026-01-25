package com.eduar2tc.calculator.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eduar2tc.calculator.R;
import com.eduar2tc.calculator.models.Calculation;
import com.eduar2tc.calculator.models.HistoryUiItem;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<HistoryUiItem> items = new ArrayList<>();

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ROW = 1;

    public HistoryAdapter(List<Calculation> calculations) {
        // Keep old constructor for compatibility: convert to simple items
        // This can be replaced by setUiItems from the Activity/Fragment
        this.items = new ArrayList<>();
        for (Calculation c : calculations) {
            items.add(HistoryUiItem.createRow(c));
        }
    }

    public void setUiItems(List<HistoryUiItem> uiItems) {
        this.items = uiItems != null ? uiItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).isHeader() ? VIEW_TYPE_HEADER : VIEW_TYPE_ROW;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_section_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
            return new RowViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        HistoryUiItem uiItem = items.get(position);
        if (uiItem.isHeader()) {
            ((HeaderViewHolder) holder).title.setText(uiItem.getHeaderTitle());
        } else {
            Calculation calculation = uiItem.getCalculation();
            RowViewHolder vh = (RowViewHolder) holder;
            vh.expression.setText(calculation.getExpression());
            vh.result.setText(calculation.getResult());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class RowViewHolder extends RecyclerView.ViewHolder {
        TextView expression;
        TextView result;

        public RowViewHolder(@NonNull View itemView) {
            super(itemView);
            expression = itemView.findViewById(R.id.expression);
            result = itemView.findViewById(R.id.result);
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.sectionHeaderText);
        }
    }
}
