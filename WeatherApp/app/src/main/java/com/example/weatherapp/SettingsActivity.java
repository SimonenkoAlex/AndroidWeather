package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

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
    private Spinner listUnits;
    final String[] chooseUnits = { "⁰C", "F", "K" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        cityList = (ListView)findViewById(R.id.citiesList);
        cityFilter = (EditText)findViewById(R.id.cityFilter);
        listUnits = (Spinner) findViewById(R.id.spinTemp);

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        cityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                City city = arrayAdapter.getItem(position);
                if(city != null) {
                    // переход к главной activity

                    intent.putExtra("name", city.getName());
                    startActivity(intent);
                }
            }
        });

        cityFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                arrayAdapter.getFilter().filter(s.toString());
            }
        });

        // Создаем адаптер ArrayAdapter с помощью массива строк и стандартной разметки элемета spinner
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter(this, android.R.layout.simple_spinner_item, chooseUnits);
        // Определяем разметку для использования при выборе элемента
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Применяем адаптер к элементу spinner
        listUnits.setAdapter(adapterSpinner);

        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Получаем выбранный объект
                String item = (String)parent.getItemAtPosition(position);
                if(item != null) {
                    if (item == "⁰C") item = "metric";
                    if (item == "F") item = "imperial";
                    if (item == "K") item = "standard";
                    intent.putExtra("temp", item);
                    //startActivity(intent);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        listUnits.setOnItemSelectedListener(itemSelectedListener);
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