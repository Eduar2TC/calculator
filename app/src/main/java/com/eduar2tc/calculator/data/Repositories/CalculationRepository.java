package com.eduar2tc.calculator.data.Repositories;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.eduar2tc.calculator.data.daos.CalculationDao;
import com.eduar2tc.calculator.data.databases.CalculationDatabase;
import com.eduar2tc.calculator.data.entities.CalculationEntity;
import com.eduar2tc.calculator.models.Calculation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CalculationRepository {
    private final CalculationDao calculationDao;
    private final Executor io = Executors.newSingleThreadExecutor();

    public CalculationRepository(Context context){
        CalculationDatabase db = CalculationDatabase.getDatabaseInstance(context);
        calculationDao = db.calculationDao();
    }

    //return map from database to LiveData
    public LiveData<List<Calculation>> getAll() {
        MediatorLiveData<List<Calculation>> mapped = new androidx.lifecycle.MediatorLiveData<>();
        mapped.addSource(calculationDao.getAll(), input -> {
            List<Calculation> calculations = new ArrayList<>();
            if (input == null) {
                mapped.setValue(calculations);
                return;
            }
            for (CalculationEntity entity : input) {
                calculations.add(new Calculation(entity.expression, entity.result, entity.timestamp));
            }
            mapped.setValue(calculations);
        });
        return mapped;
    }
    public void insert(final Calculation calculation){
        io.execute(() -> {
            try {
                CalculationEntity entity = new CalculationEntity(calculation.getExpression(), calculation.getResult(), calculation.getTimestamp());
                calculationDao.insert(entity);
            } catch (Exception e) {
                // exception intentionally swallowed; caller can observe via repository LiveData or improve error handling if needed
            }
        });
        }
    public void deleteAll() {
        io.execute(() -> {
            try {
                calculationDao.deleteAll();
            } catch (Exception e) {
                // exception intentionally swallowed
            }
        });
    }
}
