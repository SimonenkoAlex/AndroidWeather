package data;

import android.app.Activity;
import android.content.SharedPreferences;

public class TemperaturePreference {
    SharedPreferences prefs;

    public TemperaturePreference(Activity activity) {
        prefs = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    public String getTemperatureUnits() {
        return prefs.getString("temp", "metric");
    }

    public void setTemperatureUnits(String tempUnits){
        prefs.edit().putString("temp", tempUnits).commit();
    }
}
