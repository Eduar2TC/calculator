package com.eduar2tc.calculator.ui.controllers;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eduar2tc.calculator.R;
import com.eduar2tc.calculator.adapters.HistoryAdapter;
import com.eduar2tc.calculator.models.Calculation;
import com.eduar2tc.calculator.utils.HistoryUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryUIController {
    private static final float TOP_SHEET_EXPANDED_PERCENT = 0.6f;

    private final Context context;
    private final RecyclerView recyclerView;
    private final TextView emptyMessage;
    private final ConstraintLayout parentLayout;
    private final ConstraintLayout topSheet;
    private final HistoryAdapter adapter;
    private final List<Calculation> calculationHistory;

    public HistoryUIController(Context context, RecyclerView recyclerView,
                               TextView emptyMessage, ConstraintLayout parentLayout,
                               ConstraintLayout topSheet) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.emptyMessage = emptyMessage;
        this.parentLayout = parentLayout;
        this.topSheet = topSheet;
        this.calculationHistory = new ArrayList<>();
        this.adapter = new HistoryAdapter(calculationHistory);

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
    }

    public void updateHistory(List<Calculation> calculations) {
        calculationHistory.clear();
        if (calculations != null) {
            calculationHistory.addAll(calculations);
        }
        refreshUI();
    }

    private void refreshUI() {
        if (HistoryUtils.isHistoryEmpty(calculationHistory)) {
            showEmptyState();
        } else {
            showHistoryList();
        }
    }

    private void showEmptyState() {
        emptyMessage.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void showHistoryList() {
        emptyMessage.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        List<com.eduar2tc.calculator.models.HistoryUiItem> uiItems =
                HistoryUtils.flattenFromCalculations(calculationHistory, Locale.getDefault());
        adapter.setUiItems(uiItems);

        recyclerView.post(() -> adjustRecyclerViewHeight(uiItems.size()));
    }

    private void adjustRecyclerViewHeight(int itemCount) {
        ViewGroup.LayoutParams lp = recyclerView.getLayoutParams();
        int parentHeight = parentLayout.getHeight();
        int expandedVisible = (int) (parentHeight * TOP_SHEET_EXPANDED_PERCENT);

        int handleHeight = getHandleHeight();
        int perItemPx = getItemHeight();
        int contentHeight = calculateContentHeight(itemCount, perItemPx);
        int maxListHeight = calculateMaxListHeight(expandedVisible, handleHeight);

        if (contentHeight > maxListHeight) {
            lp.height = maxListHeight;
        } else {
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        recyclerView.setLayoutParams(lp);
        recyclerView.requestLayout();
    }

    private int getHandleHeight() {
        if (topSheet != null) {
            View handle = topSheet.findViewById(R.id.topSheetHandle);
            if (handle != null) return handle.getHeight();
        }
        return 0;
    }

    private int getItemHeight() {
        if (recyclerView.getChildCount() > 0) {
            return recyclerView.getChildAt(0).getHeight();
        }
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                48,
                context.getResources().getDisplayMetrics()
        );
    }

    private int calculateContentHeight(int itemCount, int perItemPx) {
        return perItemPx * itemCount +
                recyclerView.getPaddingTop() +
                recyclerView.getPaddingBottom();
    }

    private int calculateMaxListHeight(int expandedVisible, int handleHeight) {
        int marginPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                16,
                context.getResources().getDisplayMetrics()
        );
        return Math.max(0, expandedVisible - handleHeight - marginPx);
    }
}