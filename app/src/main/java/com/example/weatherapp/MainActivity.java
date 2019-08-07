package com.example.weatherapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

/*
 * The main controller class. Contains the main functionality of the program,
 *  may need to break it into internal classes to match the SRS.
 *
 *  1.  Need to add location based updates
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = com.example.weatherapp.MainActivity.class.getSimpleName();

    private ArrayList<Weather> weatherArrayList = new ArrayList<>();
    //private ArrayList<Weather> dailyArrayList = new ArrayList<>();
    private ArrayList<Weather> hourlyArrayList = new ArrayList<>();
    private ArrayList<Weather> currentArrayList = new ArrayList<>();
    private ListView listView;
    private ListView dailyView;
    private ListView hourlyView;

    private int key;
    private boolean metric = false;

    public int getKey() {
        return key;
    }
    public void setKey(int key) {
        this.key = key;
    }

    public boolean isMetric() {
        return metric;
    }
    public void setMetric(boolean metric) {
        this.metric = metric;
    }

    /*
     * Creates API URL and call, as well as initial view
     * 1. Need to tie the initial view into current location
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_layout);
        listView = findViewById(R.id.idListView);
        hourlyView = findViewById(R.id.hourlyList);
        dailyView = findViewById(R.id.weatherList);
        for(int x=2; x>=0; x--){
            setKey(x);
            URL weatherUrl = NetworkUtils.buildUrlForWeather(getKey());
            new com.example.weatherapp.MainActivity.FetchWeatherDetails().execute(weatherUrl);
            Log.i(TAG, "onCreate: weatherUrl: " + weatherUrl);
        }
    }

    /*
     * API fetch, handles exception if no return. Also builds weather list from return.
     * Now: handles hourly, daily and current
     */
    private class FetchWeatherDetails extends AsyncTask<URL, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /*
         *  Checks response from URL
         */
        @Override
        protected String doInBackground(URL... urls) {
            URL weatherUrl = urls[0];
            String weatherSearchResults = null;
            try {
                weatherSearchResults = NetworkUtils.getResponseFromHttpUrl(weatherUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "doInBackground: weatherSearchResults: " + weatherSearchResults);
            return weatherSearchResults;
        }

        /*
         * Fills weatherArrayLists and displays the values in the log for troubleshooting
         * 1. Need to adjust based on the information we will be utilizing from API
         */
        @Override
        protected void onPostExecute(String weatherSearchResults) {
            if(weatherSearchResults != null && !weatherSearchResults.equals("")) {
                weatherArrayList = parseJSON(weatherSearchResults);
            }
            super.onPostExecute(weatherSearchResults);
        }
    }

    /*
     *
     * After checking the return is not null and emptying previous array call:
     * fills a Weather object and adds it to weatherArrayList. Then calls weather adapter
     * to convert it into view object.
     *
     */
    private ArrayList<Weather> parseJSON(String weatherSearchResults) {
        if(weatherSearchResults != null) {

            switch(getKey()) {
                //daily
                case 0:{
                    if(weatherArrayList != null) {
                        weatherArrayList.clear();
                    }
                    try {
                        JSONObject rootObject = new JSONObject(weatherSearchResults);
                        JSONArray results = rootObject.getJSONArray("DailyForecasts");

                        for (int i = 0; i < results.length(); i++) {
                            Weather weather = new Weather();
                            JSONObject resultsObj = results.getJSONObject(i);
                            String date = resultsObj.getString("Date");
                            weather.setDate(date);
                            JSONObject temperatureObj = resultsObj.getJSONObject("Temperature");
                            int minTemperature = temperatureObj.getJSONObject("Minimum").getInt("Value");
                            weather.setMinTemp(Integer.toString(minTemperature));
                            String unit= temperatureObj.getJSONObject("Minimum").getString("Unit");
                            weather.setUnit(unit);
                            int maxTemperature = temperatureObj.getJSONObject("Maximum").getInt("Value");
                            weather.setMaxTemp(Integer.toString(maxTemperature));
                            JSONObject dayObj = resultsObj.getJSONObject("Day");
                            int icon = dayObj.getInt("Icon");
                            weather.setIcon(Integer.toString(icon));
                            String iconPhrase = dayObj.getString("IconPhrase");
                            weather.setIconPhrase(iconPhrase);
                            Boolean precip = dayObj.getBoolean("HasPrecipitation");
                            weather.setPrecipitation(precip);
                            weather.setKey(key);

                            weatherArrayList.add(weather);

                            Log.i(TAG, "onPostExecuteDAILY: Date: " + weather.getDate()+
                                    " Min: " + weather.getMinTemp() +
                                    " Max: " + weather.getMaxTemp() +
                                    " Unit: " + weather.getUnit() +
                                    "Icon: " + weather.getIcon() +
                                    "IconPhrase: " + weather.getIconPhrase() +
                                    "HasPrecip: " + weather.getPrecipitation());


                        }
                        if(weatherArrayList != null) {
                            ListAdapter weatherAdapter = new ListAdapter(this, weatherArrayList);
                            dailyView.setAdapter(weatherAdapter);
                        }

                        return weatherArrayList;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //hourly forecast
                case 1: {
                    if(hourlyArrayList != null) {
                        hourlyArrayList.clear();
                    }
                    try {
                        //JSONArray rootObject = new JSONArray(weatherSearchResults);
                        JSONArray results = new JSONArray(weatherSearchResults);

                        for (int i = 0; i < results.length(); i++) {
                            Weather weather = new Weather();

                            JSONObject resultsObj = results.getJSONObject(i);
                            String date = resultsObj.getString("DateTime");
                            weather.setDate(date);
                            int icon = resultsObj.getInt("WeatherIcon");
                            weather.setIcon(Integer.toString(icon));
                            String iconPhrase = resultsObj.getString("IconPhrase");
                            weather.setIconPhrase(iconPhrase);
                            Boolean precip = resultsObj.getBoolean("HasPrecipitation");
                            weather.setPrecipitation(precip);
                            Boolean day = resultsObj.getBoolean("IsDaylight");
                            weather.setDay(day);
                            JSONObject temperatureObj = resultsObj.getJSONObject("Temperature");
                            int temperature = temperatureObj.getInt("Value");
                            weather.setTemp(Integer.toString(temperature));
                            String unit = temperatureObj.getString("Unit");
                            weather.setUnit(unit);
                            int precipProb = resultsObj.getInt("PrecipitationProbability");
                            weather.setPrecipitationProb(precipProb);


                            weather.setKey(key);

                            hourlyArrayList.add(weather);

                            Log.i(TAG, "onPostExecuteHOURLY: Date: " + weather.getDate()+
                                    " Temp: " + weather.getTemp() +
                                    " Unit: " + weather.getUnit() +
                                    "Icon: " + weather.getIcon() +
                                    "IconPhrase: " + weather.getIconPhrase() +
                                    "HasPrecip: " + weather.getPrecipitation());

                        }
                        if(hourlyArrayList != null) {
                            ListAdapter hourlyAdapter = new ListAdapter(this, hourlyArrayList);
                            hourlyView.setAdapter(hourlyAdapter);
                        }

                        return hourlyArrayList;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //current forecast
                case 2: {
                    if(currentArrayList != null) {
                        currentArrayList.clear();
                    }
                    try {
                        JSONArray rootObject = new JSONArray(weatherSearchResults);
                        JSONObject result = rootObject.getJSONObject(0);
                            Weather weather = new Weather();

                            String date = result.getString("LocalObservationDateTime");
                            weather.setDate(date);
                            String weatherText = result.getString("WeatherText");
                            weather.setIconPhrase(weatherText);
                            int icon = result.getInt("WeatherIcon");
                            weather.setIcon(Integer.toString(icon));
                            Boolean precip = result.getBoolean("HasPrecipitation");
                            weather.setPrecipitation(precip);

                            JSONObject temperatureObj = result.getJSONObject("Temperature");

                            JSONObject imperialObj = temperatureObj.getJSONObject("Imperial");
                            int temp = imperialObj.getInt("Value");
                            weather.setTemp(Integer.toString(temp));
                            String unit = imperialObj.getString("Unit");
                            weather.setUnit(unit);


                            weather.setKey(key);

                            currentArrayList.add(weather);
                            currentArrayList.add(weather);

                        Log.i(TAG, "onPostExecuteCurrent: Date: " + weather.getDate()+
                                " Temp: " + weather.getTemp() +
                                " Unit: " + weather.getUnit() +
                                " Icon: " + weather.getIcon() +
                                " IconPhrase: " + weather.getIconPhrase() +
                                " HasPrecip: " + weather.getPrecipitation());

                        /*if (currentArrayList!= null) {
                            ListAdapter currentAdapter = new ListAdapter(this, currentArrayList);
                            listView.setAdapter(currentAdapter);
                        }*/

                        return currentArrayList;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
}

