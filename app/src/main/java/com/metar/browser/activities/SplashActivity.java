package com.metar.browser.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.snackbar.Snackbar;
import com.metar.browser.R;
import com.metar.browser.database.MetarEntity;
import com.metar.browser.databinding.ActivitySplashBinding;
import com.metar.browser.utils.Utility;
import com.metar.browser.view_models.SplashActivityViewModel;

import java.util.List;

public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding mBinding;
    private SplashActivityViewModel mViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        mViewModel = ViewModelProviders.of(this).get(SplashActivityViewModel.class);

        mViewModel.getStations().observe(this, new Observer<List<MetarEntity>>() {
            @Override
            public void onChanged(List<MetarEntity> metarEntities) {
                if (metarEntities != null && metarEntities.size() > 0) {
                    Utility.launchActivity(SplashActivity.this, MainActivity.class, true);
                } else {
                    showGenericError();
                }
            }
        });

        mViewModel.isLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                updateProgressBar(aBoolean);
            }
        });

        mViewModel.getNetworkError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    showNetworkError();
                }
            }
        });
    }

    private void showGenericError() {
        Snackbar snackbar = Snackbar
                .make(findViewById(android.R.id.content), getString(R.string.offline_data_error), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });
        snackbar.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewModel.getAllStationsFromServer();
    }

    private void showNetworkError() {
        Snackbar snackbar = Snackbar
                .make(findViewById(android.R.id.content), getString(R.string.network_error_label), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.wifi_on), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                });
        snackbar.show();
    }

    private void updateProgressBar(boolean show) {
        mBinding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}