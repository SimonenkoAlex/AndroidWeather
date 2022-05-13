package com.example.weatherapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.SubMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;

import data.CityPreference;
import data.GPSTracker;
import data.JSONWeatherParser;
import data.TemperaturePreference;
import data.WeatherHttpClient;
import model.Weather;

import static Util.Utils.ICON_URL;

public class MainActivity extends AppCompatActivity {
    // объявление переменных - текстовых полей
    private TextView cityName;
    private TextView temp;
    private ImageView iconView;
    private TextView description;
    private TextView humidity;
    private TextView pressure;
    private TextView wind;
    private TextView sunrise;
    private TextView sunset;
    private TextView updated;
    // тег для определения главного окна приложения
    private  static final String TAG = "Main";

    //private LocationManager locationManager;
    //String longitude = new String();
    //String latitude = new String();
    TemperaturePreference tempPref;
    CityPreference cityPreference;
    Weather weather = new Weather();
    //GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cityName = (TextView) findViewById(R.id.cityText);
        iconView = (ImageView) findViewById(R.id.thumbnailIcon);
        temp = (TextView) findViewById(R.id.tempText);
        description = (TextView) findViewById(R.id.cloudText);
        humidity = (TextView) findViewById(R.id.humidText);
        pressure = (TextView) findViewById(R.id.pressureText);
        wind = (TextView) findViewById(R.id.windText);
        sunrise = (TextView) findViewById(R.id.riseText);
        sunset = (TextView) findViewById(R.id.setText);
        updated = (TextView) findViewById(R.id.updateText);

        cityPreference = new CityPreference(MainActivity.this);
        tempPref = new TemperaturePreference(MainActivity.this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            cityPreference.setCity(extras.getString("name"));
            tempPref.setTemperatureUnits(extras.getString("temp"));
        }

        renderWeatherData(cityPreference.getCity(), tempPref.getTemperatureUnits());
    }

    public void renderWeatherData (String city, String units) {
        WeatherTask weatherTask = new WeatherTask();
        weatherTask.execute(new String[]{"q=" + city + "&units=" + units + "&lang=ru&APPID=ae00dc4b6dd00a9863e5e712e68387bf"});
    }

    //public void renderWeatherData (String lon, String lat, String units) {
    //    WeatherTask weatherTask = new WeatherTask();
    //    weatherTask.execute(new String[]{"lon=" + lon + "&lat=" + lat + "&units=" + units + "&lang=ru&APPID=ae00dc4b6dd00a9863e5e712e68387bf"});
    //}

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    private class WeatherTask extends AsyncTask<String, Void, Weather> {
        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);

            DateFormat df = DateFormat.getTimeInstance();
            String sunriseDate = df.format(new Date(weather.place.getSunrise()));
            String sunsetDate = df.format(new Date(weather.place.getSunset()));
            String updateDate = df.format(new Date(weather.place.getLastupdate()));

            DecimalFormat decimalFormat = new DecimalFormat("#.#");
            String tempFormat = decimalFormat.format(weather.currentCondition.getTemperature());
            String pressureFormat = decimalFormat.format(weather.currentCondition.getPressure());

            cityName.setText(weather.place.getCity());
            if (tempPref.getTemperatureUnits() == "metric")
                temp.setText("" + tempFormat + " ⁰C");
            if (tempPref.getTemperatureUnits() == "imperial")
                temp.setText("" + tempFormat + " F");
            if (tempPref.getTemperatureUnits() == "standard")
                temp.setText("" + tempFormat + " K");
            humidity.setText("Влажность: " + weather.currentCondition.getHumidity() + " %");
            pressure.setText("Давление: " + pressureFormat + " мм. рт. ст.");
            wind.setText("Скорость ветра: " + weather.wind.getSpeed() + " м/с");
            sunrise.setText("Восход: " + sunriseDate);
            sunset.setText("Закат: " + sunsetDate);
            updated.setText("Обновленно: " + updateDate);
            description.setText("Состояние: " + weather.currentCondition.getCondition() + "(" + weather.currentCondition.getDescription() + ")");
        }

        @SuppressLint("WrongThread")
        @Override
        protected Weather doInBackground(String... strings) {
            String data = ((new WeatherHttpClient()).getWeatherData(strings[0]));
            weather = JSONWeatherParser.getWeather(data);
            weather.iconData = weather.currentCondition.getIcon();
            Log.v("Data: ", weather.place.getCity() + weather.iconData);

            new DownloadImageTask(iconView).execute(ICON_URL + weather.iconData);

            return weather;
        }
    }

    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Единицы измерения").setCancelable(true);
        final String[] chooseUnits = { "⁰C", "F", "K" };
        // добавляем одну кнопку для закрытия диалога
        builder.setNeutralButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }) // добавляем переключатели
        .setSingleChoiceItems(chooseUnits, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle extras = getIntent().getExtras();
                if (chooseUnits[which] == "⁰C") tempPref.setTemperatureUnits("metric");
                if (chooseUnits[which] == "F") tempPref.setTemperatureUnits("imperial");
                if (chooseUnits[which] == "K") tempPref.setTemperatureUnits("standard");
                renderWeatherData(cityName.getText().toString(), tempPref.getTemperatureUnits());
            }
        });
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_units: showInputDialog(); break;
            case R.id.action_settings: startActivity(new Intent(this, SettingsActivity.class)); break;
            case R.id.russian_local: item.setChecked(true); break;
            case R.id.english_local: item.setChecked(true); break;
        }

        return super.onOptionsItemSelected(item);
    }
}