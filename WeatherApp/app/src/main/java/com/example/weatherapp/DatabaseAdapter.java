package com.example.weatherapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import model.City;

public class DatabaseAdapter {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public DatabaseAdapter(Context context){
        dbHelper = new DatabaseHelper(context.getApplicationContext());
    }

    public DatabaseAdapter open(){
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        dbHelper.close();
    }

    private Cursor getAllEntries(){
        String[] columns = new String[] {DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_NAME, DatabaseHelper.COLUMN_TEMP};
        return  database.query(DatabaseHelper.TABLE, columns, null, null, null, null, null);
    }

    public List<City> getCities(){
        ArrayList<City> cities = new ArrayList<>();
        Cursor cursor = getAllEntries();
        while (cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
            String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
            int avgTemp = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_TEMP));
            cities.add(new City(id, name, avgTemp));
        }
        cursor.close();
        return  cities;
    }

    public long getCount(){
        return DatabaseUtils.queryNumEntries(database, DatabaseHelper.TABLE);
    }

    public City getCity(long id){
        City city = null;
        String query = String.format("SELECT * FROM %s WHERE %s=?",DatabaseHelper.TABLE, DatabaseHelper.COLUMN_ID);
        Cursor cursor = database.rawQuery(query, new String[]{ String.valueOf(id)});
        if(cursor.moveToFirst()){
            String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
            int avgTemp = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_TEMP));
            city = new City(id, name, avgTemp);
        }
        cursor.close();
        return  city;
    }

    public long insert(City city){

        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_NAME, city.getName());
        cv.put(DatabaseHelper.COLUMN_TEMP, city.getAvgTemp());

        return  database.insert(DatabaseHelper.TABLE, null, cv);
    }

    public long delete(long cityId){

        String whereClause = "_id = ?";
        String[] whereArgs = new String[]{String.valueOf(cityId)};
        return database.delete(DatabaseHelper.TABLE, whereClause, whereArgs);
    }

    public long update(City city){

        String whereClause = DatabaseHelper.COLUMN_ID + "=" + city.getId();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_NAME, city.getName());
        cv.put(DatabaseHelper.COLUMN_TEMP, city.getAvgTemp());
        return database.update(DatabaseHelper.TABLE, cv, whereClause, null);
    }
}
