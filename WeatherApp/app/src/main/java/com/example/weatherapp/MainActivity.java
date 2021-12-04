package com.example.weatherapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.util.Log;
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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;

import data.CityPreference;
import data.GPSTracker;
import data.JSONWeatherParser;
import data.WeatherHttpClient;
import model.Weather;

public class MainActivity extends AppCompatActivity {

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

    private  static final String TAG = "Main";

    private LocationManager locationManager;
    String longitude = new String();
    String latitude = new String();

    private String newUnits = "metric";
    Weather weather = new Weather();
    GPSTracker gps;

    @Override
    protected void onStart() {
        super.onStart();
        // вызов разрешения на использование геолокации
        if (isReadPermissionGranted()) {
            // Create class object
            gps = new GPSTracker(MainActivity.this);

            // Check if GPS enabled
            if (gps.canGetLocation()) {
                longitude = String.format("%2$.2f", gps.getLongitude());
                latitude = String.format("%2$.2f", gps.getLatitude());

                renderWeatherData(longitude, latitude, newUnits);
                Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

            } else {
                // Can't get location.
                // GPS or network is not enabled.
                // Ask user to enable GPS/network in settings.
                gps.showSettingsAlert();
            }
        } else {
            CityPreference cityPreference = new CityPreference(MainActivity.this);
            renderWeatherData(cityPreference.getCity(), newUnits);
        }
    }

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

        //CityPreference cityPreference = new CityPreference(MainActivity.this);
        //renderWeatherData(cityPreference.getCity(), newUnits);
    }

    // мы можем также дать другой Permission
    public boolean isReadPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            }
            else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 3);
                return false;
            }
        }
        else {
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 3: Log.d(TAG, "External storagel");
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "Permission: " + permissions[0] + " was " + grantResults[0]);
                    // Добавьте сюда, что делать после предоставления разрешения
                    //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 10, 10, locationListener);
                    //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 10, 10, locationListener);
                    //checkEnabled();
                }
                else {
                    // Оставить пустым
                }
        }
    }

    public void renderWeatherData (String city, String units) {
        WeatherTask weatherTask = new WeatherTask();
        weatherTask.execute(new String[]{"q=" + city + "&units=" + units + "&lang=ru&APPID=ae00dc4b6dd00a9863e5e712e68387bf"});
    }

    public void renderWeatherData (String lon, String lat, String units) {
        WeatherTask weatherTask = new WeatherTask();
        weatherTask.execute(new String[]{"lon=" + lon + "&lat=" + lat + "&units=" + units + "&lang=ru&APPID=ae00dc4b6dd00a9863e5e712e68387bf"});
    }

    private class DownloadImageAsyncTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            iconView.setImageBitmap(bitmap);
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            return downloadImage(strings[0]);
        }

        private Bitmap downloadImage (String code) {
            final DefaultHttpClient client = new DefaultHttpClient();
            HttpGet getRequest = new HttpGet(Util.Utils.ICON_URL + code + ".png");
            //HttpGet getRequest = new HttpGet(Util.Utils.ICON_URL + "04d.png");
            //final HttpGet getRequest = new HttpGet("https://www.clipartmax.com/png/full/156-1563320_local-grocery-store-comments-cart-icon-png.png");
            try {
                HttpResponse response = client.execute(getRequest);
                final int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK) {
                    Log.v("DownloadImage ", "Error: " + statusCode);
                    return null;
                }
                final HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream inputStream = null;
                    inputStream = entity.getContent();
                    // decode contents from the stream
                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    return bitmap;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
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
            if (newUnits == "metric")
                temp.setText("" + tempFormat + " ⁰C");
            if (newUnits == "imperial")
                temp.setText("" + tempFormat + " F");
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
            Log.v("Data: ", weather.place.getCity());
            new DownloadImageAsyncTask().execute(new String [] { weather.iconData});
            return weather;
        }
    }

    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Единицы измерения").setCancelable(true);
        final String[] chooseUnits = { "⁰C", "F" };
        // добавляем одну кнопку для закрытия диалога
        builder.setNeutralButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }) // добавляем переключатели
        .setSingleChoiceItems(chooseUnits, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (chooseUnits[which] == "⁰C") newUnits = "metric";
                if (chooseUnits[which] == "F") newUnits = "imperial";
                renderWeatherData(cityName.getText().toString(), newUnits);
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
        CityPreference cityPreference = new CityPreference(MainActivity.this);

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.menu_moscow: cityPreference.setCity("Москва"); break;
            case R.id.menu_saint_petersburg: cityPreference.setCity("Санкт-Петербург"); break;
            case R.id.menu_novosibirsk: cityPreference.setCity("Новосибирск"); break;
            case R.id.menu_samara: cityPreference.setCity("Самара"); break;
            case R.id.menu_kazan: cityPreference.setCity("Казань"); break;
            case R.id.Rostov_on_Don: cityPreference.setCity("Ростов-на-Дону"); break;
            case R.id.menu_omsk: cityPreference.setCity("Омск"); break;
            case R.id.menu_ufa: cityPreference.setCity("Уфа"); break;
            case R.id.menu_volgograd: cityPreference.setCity("Волгоград"); break;
            case R.id.Permian: cityPreference.setCity("Пермь"); break;
            case R.id.Krasnoyarsk: cityPreference.setCity("Красноярск"); break;
            case R.id.Saratov: cityPreference.setCity("Саратов"); break;
            case R.id.Voronezh: cityPreference.setCity("Воронеж"); break;
            case R.id.Krasnodar: cityPreference.setCity("Краснодар"); break;
        }
        if (id == R.id.action_settings) {
            showInputDialog();
        }
        String newCity = cityPreference.getCity();
        renderWeatherData(newCity, newUnits);
        return super.onOptionsItemSelected(item);
    }
}