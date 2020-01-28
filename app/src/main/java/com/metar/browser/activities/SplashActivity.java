package com.metar.browser.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.metar.browser.BuildConfig;
import com.metar.browser.R;
import com.metar.browser.database.MetarEntity;
import com.metar.browser.databinding.ActivitySplashBinding;
import com.metar.browser.utils.NetworkHelper;
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
                    if (NetworkHelper.isOnline()) {
                        mViewModel.getAllStationsFromServer(BuildConfig.METAR_LIST_URL);
                    } else {
                        Toast.makeText(SplashActivity.this, getString(R.string.network_error_label), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        mViewModel.isLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                updateProgressBar(aBoolean);
            }
        });
    }

    private void updateProgressBar(boolean show) {
        mBinding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
