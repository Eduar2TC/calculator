package com.eduar2tc.calculator.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.eduar2tc.calculator.data.Repositories.CalculationRepository;
import com.eduar2tc.calculator.models.Calculation;

import java.util.List;

public class HistoryViewModel extends AndroidViewModel {
    private final CalculationRepository repository;
    private final LiveData<List<Calculation>> history;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        repository = new CalculationRepository(application);
        history = repository.getAll();
    }
    public LiveData<List<Calculation>> getHistory() {
        return history;
    }
    public void addCalculation(String expression, String result) {
        repository.insert(new Calculation(expression, result, System.currentTimeMillis()));
    }
    // Overload that accepts a timestamp so callers can insert with a known timestamp (prevents duplicates / enables optimistic UI)
    public void addCalculation(String expression, String result, long timestamp) {
        repository.insert(new Calculation(expression, result, timestamp));
    }
    public void deleteAll() {
        repository.deleteAll();
    }

}