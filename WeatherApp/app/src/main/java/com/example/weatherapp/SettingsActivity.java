package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.List;

import data.CityPreference;
import model.City;

public class SettingsActivity extends AppCompatActivity {
    private DatabaseHelper sqlHelper;
    private SQLiteDatabase db;
    private Cursor cityCursor;
    private SimpleCursorAdapter cityAdapter;
    private ArrayAdapter<City> arrayAdapter;
    private ListView cityList;
    private EditText cityFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        cityList = (ListView)findViewById(R.id.citiesList);
        cityFilter = (EditText)findViewById(R.id.cityFilter);

        cityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                City city = arrayAdapter.getItem(position);
                if(city != null) {
                    // переход к главной activity
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("name", city.getName());
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        DatabaseAdapter adapter = new DatabaseAdapter(this);
        adapter.open();

        List<City> cities = adapter.getCities();

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cities);
        cityList.setAdapter(arrayAdapter);
        adapter.close();
    }
}