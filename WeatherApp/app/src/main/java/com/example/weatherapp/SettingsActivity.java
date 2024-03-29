package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    private String newCity;
    private String newUnits;
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

        //Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        cityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                City city = arrayAdapter.getItem(position);
                if(city != null) {
                    // переход к главной activity
                    newCity = city.getName();
                    Log.v("New City: ", newCity);
                    //intent.putExtra("name", city.getName());
                    //startActivity(intent);
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
                    if (item == "⁰C") newUnits = "metric";
                    if (item == "F") newUnits = "imperial";
                    if (item == "K") newUnits = "standard";
                    Log.v("New Unit: ", newUnits + " " + item);
                    //intent.putExtra("temp", newUnits);
                    //startActivity(intent);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        listUnits.setOnItemSelectedListener(itemSelectedListener);

        findViewById(R.id.a_counter_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("temp", newUnits);
        intent.putExtra("name", newCity);
        startActivity(intent);
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

        Intent intent = getIntent();

        newUnits = intent.getStringExtra("temp");
        newCity = intent.getStringExtra("name");
    }
}