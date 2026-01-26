package com.eduar2tc.calculator.data.databases;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import com.eduar2tc.calculator.data.daos.CalculationDao;
import com.eduar2tc.calculator.data.entities.CalculationEntity;

@Database(entities = {CalculationEntity.class}, version = 1, exportSchema = true)
public abstract class CalculationDatabase extends RoomDatabase {
    public abstract CalculationDao calculationDao();
    private static volatile CalculationDatabase INSTANCE;
    public static CalculationDatabase getDatabaseInstance(final Context context) {
        if( INSTANCE == null){
            synchronized (CalculationDatabase.class){
                if(INSTANCE == null){
                 INSTANCE = Room.databaseBuilder(
                             context.getApplicationContext(),
                            CalculationDatabase.class,
                         "calculation_database").build();
                }
            }
        }
        return INSTANCE;
    }
}
