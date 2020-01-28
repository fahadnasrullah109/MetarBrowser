package com.metar.browser.repository;

import android.app.Application;
import android.database.Cursor;
import android.os.AsyncTask;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.metar.browser.database.MetarDao;
import com.metar.browser.database.MetarEntity;
import com.metar.browser.database.MetarMessagesDatabase;
import com.metar.browser.utils.NetworkHelper;
import com.metar.browser.utils.Utility;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MetarRepository {
    private final String TAG = MetarRepository.class.getSimpleName();
    private MetarDao mMetarDao;
    private MutableLiveData<List<MetarEntity>> mAllMessages = new MutableLiveData<>();
    private RequestQueue mRequestQueue;
    private ParsingAndSavingAsyncTaskRunner mTask;

    public MetarRepository(Application application) {
        MetarMessagesDatabase db = MetarMessagesDatabase.getDatabase(application);
        mMetarDao = db.metarDao();
        mRequestQueue = Volley.newRequestQueue(application);
    }

    public Cursor getDealsCursor() {
        return mMetarDao.getMessagesCursor();
    }

    public LiveData<List<MetarEntity>> getAllMessages() {
        return mAllMessages;
    }

    public void getMessagesListFromNetwork(final String url, final MutableLiveData<List<MetarEntity>> liveData) {
        if (NetworkHelper.isOnline()) {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (!TextUtils.isEmpty(response)) {
                                response = Utility.normalizeResponse(response);
                                mTask = new ParsingAndSavingAsyncTaskRunner(response, liveData);
                                mTask.execute();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mAllMessages.postValue(mMetarDao.getAllMessages());
                }
            });
            stringRequest.setTag(TAG);
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mRequestQueue.add(stringRequest);
        } else {
            mAllMessages.postValue(mMetarDao.getAllMessages());
        }
    }

    private List<MetarEntity> parseXML(XmlPullParser parser) throws XmlPullParserException, IOException {
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
                        Objects.requireNonNull(stations).add(new MetarEntity(station, null, null));
                    }
                    station = null;
                    break;
            }
            eventType = parser.next();
        }
        return stations;
    }

    public void insert(MetarEntity word) {
        MetarMessagesDatabase.databaseWriteExecutor.execute(() -> {
            mMetarDao.insert(word);
        });
    }

    private class ParsingAndSavingAsyncTaskRunner extends AsyncTask<String, String, List<MetarEntity>> {
        private String mData;
        private MutableLiveData<List<MetarEntity>> mLiveData;

        public ParsingAndSavingAsyncTaskRunner(String data, MutableLiveData<List<MetarEntity>> liveData) {
            this.mData = data;
            this.mLiveData = liveData;
        }

        @Override
        protected List<MetarEntity> doInBackground(String... params) {
            XmlPullParserFactory pullParserFactory;
            try {
                pullParserFactory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = pullParserFactory.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(mData.getBytes()))));
                List<MetarEntity> stations = parseXML(parser);
                mMetarDao.insertList(stations);
                return stations;
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(List<MetarEntity> entities) {
            mLiveData.postValue(entities);
        }
    }

    public void cancelAllRequests() {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(TAG);
        }
        if (mTask != null) {
            mTask.cancel(true);
        }
    }
}