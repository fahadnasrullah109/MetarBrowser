package com.metar.browser.repository;

import android.app.Application;
import android.os.AsyncTask;
import android.text.TextUtils;

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

import java.util.List;

public class MetarRepository {
    private final String TAG = MetarRepository.class.getSimpleName();
    private MetarDao mMetarDao;
    private RequestQueue mRequestQueue;
    private UpdateMetarTask mTask;
    private GetStationsTask mGetTask;
    private GetMessageTask mMessageTask;
    private boolean mDecodedRequestFinished, mRawRequestFinished;

    public MetarRepository(Application application) {
        MetarMessagesDatabase db = MetarMessagesDatabase.getDatabase(application);
        mMetarDao = db.metarDao();
        mRequestQueue = Volley.newRequestQueue(application);
    }

    public void getAllStations(final MutableLiveData<List<MetarEntity>> liveData) {
        mGetTask = new GetStationsTask(liveData);
        mGetTask.execute();
    }

    public void getDetail(String station, final MutableLiveData<MetarEntity> liveData) {
        if (NetworkHelper.isOnline()) {
            mDecodedRequestFinished = false;
            mRawRequestFinished = false;
            MetarEntity entity = new MetarEntity();
            entity.setStation(station);
            StringRequest decodedStringRequest = new StringRequest(Request.Method.GET, Utility.getDecodedMessageUrl(station),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            mDecodedRequestFinished = true;
                            if (!TextUtils.isEmpty(response)) {
                                entity.setDecodedMessage(response);
                                saveMessageInDatabase(entity, liveData);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mDecodedRequestFinished = true;
                    getMessageFromDatabase(station, liveData);
                }
            });
            decodedStringRequest.setTag(TAG);
            decodedStringRequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mRequestQueue.add(decodedStringRequest);

            StringRequest rawStringRequest = new StringRequest(Request.Method.GET, Utility.getRawMessageUrl(station),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            mRawRequestFinished = true;
                            if (!TextUtils.isEmpty(response)) {
                                entity.setRawMessage(response);
                                saveMessageInDatabase(entity, liveData);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mRawRequestFinished = true;
                    getMessageFromDatabase(station, liveData);
                }
            });
            rawStringRequest.setTag(TAG);
            rawStringRequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mRequestQueue.add(rawStringRequest);
        } else {
            getMessageFromDatabase(station, liveData);
        }
    }

    private void saveMessageInDatabase(MetarEntity entity, MutableLiveData<MetarEntity> liveData) {
        if (mRawRequestFinished && mDecodedRequestFinished) {
            mTask = new MetarRepository.UpdateMetarTask(entity, liveData);
            mTask.execute();
        }
    }

    private void getMessageFromDatabase(String stationName, MutableLiveData<MetarEntity> liveData) {
        mMessageTask = new GetMessageTask(stationName, liveData);
        mMessageTask.execute();
    }

    private class UpdateMetarTask extends AsyncTask<String, String, Void> {
        private MetarEntity mEntity;
        private MutableLiveData<MetarEntity> mLiveData;

        public UpdateMetarTask(MetarEntity entity, MutableLiveData<MetarEntity> liveData) {
            this.mEntity = entity;
            this.mLiveData = liveData;
        }

        @Override
        protected Void doInBackground(String... params) {
            mMetarDao.updateMetarMessage(this.mEntity.getStation(), this.mEntity.getRawMessage(), this.mEntity.getDecodedMessage());
            mLiveData.postValue(mMetarDao.getMessage(mEntity.getStation()));
            return null;
        }
    }

    private class GetMessageTask extends AsyncTask<String, String, Void> {
        private String mStationName;
        private MutableLiveData<MetarEntity> mLiveData;

        public GetMessageTask(String stationName, MutableLiveData<MetarEntity> liveData) {
            this.mStationName = stationName;
            this.mLiveData = liveData;
        }

        @Override
        protected Void doInBackground(String... params) {
            mLiveData.postValue(mMetarDao.getMessage(mStationName));
            return null;
        }
    }

    private class GetStationsTask extends AsyncTask<String, String, List<MetarEntity>> {
        private MutableLiveData<List<MetarEntity>> mLiveData;

        public GetStationsTask(MutableLiveData<List<MetarEntity>> liveData) {
            this.mLiveData = liveData;
        }

        @Override
        protected List<MetarEntity> doInBackground(String... params) {
            return mMetarDao.getAllMessages();
        }

        @Override
        protected void onPostExecute(List<MetarEntity> stations) {
            mLiveData.postValue(stations);
        }
    }

    public void cancelAllRequests() {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(TAG);
        }
        if (mTask != null) {
            mTask.cancel(true);
        }
        if (mGetTask != null) {
            mGetTask.cancel(true);
        }
        if (mMessageTask != null) {
            mMessageTask.cancel(true);
        }
    }
}