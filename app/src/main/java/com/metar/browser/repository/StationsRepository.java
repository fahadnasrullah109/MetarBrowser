package com.metar.browser.repository;

import android.app.Application;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.metar.browser.BuildConfig;
import com.metar.browser.database.MetarDao;
import com.metar.browser.database.MetarEntity;
import com.metar.browser.database.MetarMessagesDatabase;
import com.metar.browser.interfaces.StationsNetworkCallback;
import com.metar.browser.utils.NetworkHelper;
import com.metar.browser.utils.Utility;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StationsRepository {
    private static final String TAG = StationsRepository.class.getSimpleName();
    private static MetarDao mMetarDao;
    private static StationsNetworkCallback mListener;
    private StationsRepository.StationsAsyncTask mGetStationsTask;

    public StationsRepository(Application application, StationsNetworkCallback listener) {
        this.mListener = listener;
        MetarMessagesDatabase db = MetarMessagesDatabase.getDatabase(application);
        mMetarDao = db.metarDao();
    }

    public void getStationsListFromNetwork() {
        mGetStationsTask = new StationsAsyncTask();
        mGetStationsTask.execute();
    }

    private static class StationsAsyncTask extends AsyncTask<String, String, Void> {

        @Override
        protected Void doInBackground(String... params) {
            List<MetarEntity> stations = mMetarDao.getAllMessages();
            if (stations == null || stations.size() == 0) {
                stations = getStations();
                mMetarDao.insertList(stations);
            }
            mListener.onStationsSuccess(stations);
            return null;
        }
    }

    private static List<MetarEntity> getStations() {
        if (NetworkHelper.isOnline()) {
            String inputLine;
            try {
                //Create a URL object holding our url
                URL myUrl = new URL(BuildConfig.METAR_LIST_URL);
                //Create a connection
                HttpURLConnection connection = (HttpURLConnection)
                        myUrl.openConnection();
                //Set methods and timeouts
                connection.setRequestMethod(Utility.REQUEST_TYPE_GET);
                connection.setReadTimeout(Utility.CONNECTION_READ_TIMEOUT);
                connection.setConnectTimeout(Utility.CONNECTION_TIMOUT);

                //Connect to our url
                connection.connect();
                //Create a new InputStreamReader
                InputStreamReader streamReader = new
                        InputStreamReader(connection.getInputStream());
                //Create a new buffered reader and String Builder
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();
                //Check if the line we are reading is not null
                while ((inputLine = reader.readLine()) != null) {
                    stringBuilder.append(inputLine);
                }
                //Close our InputStream and Buffered reader
                reader.close();
                streamReader.close();
                return parseStationsList(stringBuilder.toString());

            } catch (IOException e) {
                e.printStackTrace();
                mListener.onStationsError();
            }
        } else {
            mListener.onNetworkError();
        }
        return new ArrayList<>();
    }

    private static List<MetarEntity> parseStationsList(String result) {
        if (TextUtils.isEmpty(result)) {
            return new ArrayList<>();
        }
        result = Utility.normalizeResponse(result);
        XmlPullParserFactory pullParserFactory;
        try {
            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(result.getBytes()))));
            return parseXML(parser);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            mListener.onStationsError();
        } catch (IOException e) {
            e.printStackTrace();
            mListener.onStationsError();
        }
        return new ArrayList<>();
    }

    private static List<MetarEntity> parseXML(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<MetarEntity> stations = null;
        String station = null;
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name;
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    stations = new ArrayList<>();
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if (name.equals("a")) {
                        station = parser.getAttributeValue(null, "href");
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equals("a") && (station != null && station.contains(".TXT") && station.startsWith("ED"))) {
                        station = station.replaceAll(".TXT", "");
                        Log.e(TAG, station);
                        Objects.requireNonNull(stations).add(new MetarEntity(station, null, null));
                    }
                    station = null;
                    break;
            }
            eventType = parser.next();
        }
        return stations;
    }

    public void cancelAllRequests() {
        if (mGetStationsTask != null) {
            mGetStationsTask.cancel(true);
        }
    }
}