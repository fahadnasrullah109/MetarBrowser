package com.metar.browser.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.metar.browser.BuildConfig;
import com.metar.browser.R;
import com.metar.browser.database.MetarEntity;
import com.metar.browser.databinding.ActivityMainBinding;
import com.metar.browser.view_models.MainActivityViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MainActivityViewModel mViewModel;
    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        mViewModel.getAllMessages().observe(this, new Observer<List<MetarEntity>>() {
            @Override
            public void onChanged(@Nullable final List<MetarEntity> messages) {
                for (MetarEntity entity : messages) {
                    //updateUiResults(entity);
                    mBinding.setMessageObj(entity);
                }
            }
        });

        mBinding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        //mViewModel.getAllStationsFromServer(BuildConfig.METAR_LIST_URL);
    }
}
