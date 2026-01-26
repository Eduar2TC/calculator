package com.eduar2tc.calculator.data.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.eduar2tc.calculator.data.entities.CalculationEntity;

import java.util.List;
@Dao
public interface CalculationDao {
    @Query("SELECT * FROM calculations ORDER BY timestamp DESC")
    LiveData<List<CalculationEntity>> getAll();
    @Insert
    long insert(CalculationEntity calculationEntity);
    @Query("DELETE FROM calculations")
    void deleteAll();
}
