package com.metar.browser.repository;

import android.app.Application;
import android.os.AsyncTask;

import com.metar.browser.database.MetarDao;
import com.metar.browser.database.MetarEntity;
import com.metar.browser.database.MetarMessagesDatabase;
import com.metar.browser.interfaces.MetarNetworkCallback;
import com.metar.browser.utils.NetworkHelper;
import com.metar.browser.utils.Utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MetarRepository {
    private final String TAG = MetarRepository.class.getSimpleName();
    private static MetarDao mMetarDao;
    private GetStationsTask mGetTask;
    private GetMessageTask mMessageTask;
    private static MetarNetworkCallback mListener;

    public MetarRepository(Application application, MetarNetworkCallback listener) {
        this.mListener = listener;
        MetarMessagesDatabase db = MetarMessagesDatabase.getDatabase(application);
        mMetarDao = db.metarDao();
    }

    public void getAllStations() {
        mGetTask = new GetStationsTask();
        mGetTask.execute();
    }

    public void getDetail(String station) {
        mMessageTask = new GetMessageTask(station);
        mMessageTask.execute();
    }

    private static String getMessageDetailOfStation(String url) {
        String inputLine;
        try {
            //Create a URL object holding our url
            URL myUrl = new URL(url);
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
            return stringBuilder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static class GetMessageTask extends AsyncTask<String, String, Void> {
        private String mStationName;

        public GetMessageTask(String stationName) {
            this.mStationName = stationName;
        }

        @Override
        protected Void doInBackground(String... params) {
            MetarEntity entity = new MetarEntity();
            if (NetworkHelper.isOnline()) {
                entity.setStation(this.mStationName);
                entity.setRawMessage(getMessageDetailOfStation(Utility.getRawMessageUrl(this.mStationName)));
                entity.setDecodedMessage(getMessageDetailOfStation(Utility.getDecodedMessageUrl(this.mStationName)));
                mMetarDao.updateMetarMessage(entity.getStation(), entity.getRawMessage(), entity.getDecodedMessage());
            } else {
                entity = mMetarDao.getMessage(this.mStationName);
            }
            mListener.onMessageSuccess(entity);
            return null;
        }
    }

    private static class GetStationsTask extends AsyncTask<String, String, Void> {

        @Override
        protected Void doInBackground(String... params) {
            mListener.onStationsSuccess(mMetarDao.getAllMessages());
            return null;
        }
    }

    public void cancelAllRequests() {
        if (mGetTask != null) {
            mGetTask.cancel(true);
        }
        if (mMessageTask != null) {
            mMessageTask.cancel(true);
        }
    }
}