package com.metar.browser.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.metar.browser.R;
import com.metar.browser.adapters.SuggestionRecyclerAdapter;
import com.metar.browser.database.MetarEntity;
import com.metar.browser.databinding.ActivityMainBinding;
import com.metar.browser.interfaces.OnItemClickListener;
import com.metar.browser.utils.Utility;
import com.metar.browser.view_models.MainActivityViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnItemClickListener {
    private final String TAG = MainActivity.class.getSimpleName();
    private MainActivityViewModel mViewModel;
    private ActivityMainBinding mBinding;
    private List<MetarEntity> mStation = new ArrayList<>();
    private SuggestionRecyclerAdapter mAdapter;
    private String mQueryString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        mViewModel.getAllStations().observe(this, new Observer<List<MetarEntity>>() {
            @Override
            public void onChanged(List<MetarEntity> entities) {
                mStation = entities;
                mAdapter.setList(mStation);
            }
        });

        mViewModel.getMetarMessage().observe(this, new Observer<MetarEntity>() {
            @Override
            public void onChanged(MetarEntity entity) {
                if (isEmptyEntity(entity)) {
                    mBinding.noDataContainer.setVisibility(View.VISIBLE);
                    mBinding.contentContainer.setVisibility(View.GONE);
                } else {
                    mBinding.noDataContainer.setVisibility(View.GONE);
                    mBinding.contentContainer.setVisibility(View.VISIBLE);
                }
                mBinding.setMessageObj(entity);
            }
        });

        mViewModel.getIsLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    showLoading();
                } else {
                    hideLoading();
                }
            }
        });

        if (savedInstanceState != null) {
            mQueryString = savedInstanceState.getString(Utility.QUERY_STRING_EXTRA, "");
            //mBinding.searchView.setQuery(mQueryString, true);
        }

        // SearchView
        mBinding.searchView.setActivated(true);
        mBinding.searchView.setQueryHint(getString(R.string.station_search_hint));
        mBinding.searchView.onActionViewExpanded();
        mBinding.searchView.setIconified(false);
        mBinding.searchView.clearFocus();
        mBinding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //getDetails(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                if (newText.length() != 0 && mQueryString == null) {
                    showSuggestions();
                } else {
                    hideSuggestions();
                }
                // Using this QueryString to check either Activity Re-Created then don't show suggestions until user type in search
                mQueryString = null;
                return false;
            }
        });

        // RecyclerView
        mAdapter = new SuggestionRecyclerAdapter(mStation, this);
        mBinding.suggestionRecyclerView.setHasFixedSize(true);
        mBinding.suggestionRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mBinding.suggestionRecyclerView.setAdapter(mAdapter);

        // Getting Stations from Database
        mViewModel.getAllStationsFromDatabase();
    }

    private boolean isEmptyEntity(MetarEntity entity) {
        if (entity == null || entity.getRawMessage() == null || entity.getDecodedMessage() == null) {
            return true;
        }
        return false;
    }

    private void showSuggestions() {
        mBinding.suggestionRecyclerView.setVisibility(View.VISIBLE);
    }

    private void hideSuggestions() {
        mBinding.suggestionRecyclerView.setVisibility(View.GONE);
    }

    private void updateTextInSearchView(String station) {
        mBinding.searchView.setQuery(station, false);
    }

    private void getDetails(String queryString) {
        if (!TextUtils.isEmpty(queryString)) {
            Utility.hideKeyboard(MainActivity.this);
            mViewModel.getStationDetailFromServer(queryString);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Utility.QUERY_STRING_EXTRA, mBinding.searchView.getQuery().toString());
    }

    private void showLoading() {
        mBinding.contentContainer.setVisibility(View.GONE);
        mBinding.noDataContainer.setVisibility(View.GONE);
        mBinding.loadingContainer.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        mBinding.loadingContainer.setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(int pos) {
        updateTextInSearchView(mStation.get(pos).getStation());
        hideSuggestions();
        getDetails(mStation.get(pos).getStation());
    }
}