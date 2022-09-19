package com.stylishfonts.stylishtext.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.BannerListener;
import com.ironsource.mediationsdk.sdk.InterstitialListener;
import com.stylishfonts.stylishtext.MyGlob;
import com.stylishfonts.stylishtext.R;
import com.stylishfonts.stylishtext.fragments.BubbleFragment;
import com.stylishfonts.stylishtext.fragments.FavouriteFragment;
import com.stylishfonts.stylishtext.fragments.HomeFragment;
import com.stylishfonts.stylishtext.fragments.SettingFragment;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.Objects;


public class HomeActivity extends AppCompatActivity implements ChipNavigationBar.OnItemSelectedListener, NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;
    ChipNavigationBar navBar;
    MaterialToolbar toolbar;
    FirebaseAnalytics mFirebaseAnalytics;
    private AppUpdateManager appUpdateManager;
    private static final int FLEXIBLE_APP_UPDATE_REQ_CODE = 100;

    IronSourceBannerLayout mIronSourceBannerLayout;
    String appkey;
    String TAG = "_HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        appkey = getString(R.string.APPKEY);
        IronSource.init(this, appkey, IronSource.AD_UNIT.INTERSTITIAL);
        IronSource.init(this, appkey, IronSource.AD_UNIT.BANNER);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        loadBannerAds();
        loadIronBanner();
        //Firebase Analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "main_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        //Bottom Navigation
        navBar = findViewById(R.id.chipnav);
        appUpdateManager = AppUpdateManagerFactory.create(this);
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(result -> {
            if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && result.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(result, AppUpdateType.FLEXIBLE, HomeActivity.this, FLEXIBLE_APP_UPDATE_REQ_CODE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }

            }

        });
        appUpdateManager.registerListener(installStateUpdatedListener);
        navBar.setOnItemSelectedListener(this);
        navBar.setItemSelected(R.id.home, true);
        loadFragment(new HomeFragment());
        //Drawer Layout
        drawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        navigationView.getMenu().getItem(0).setChecked(true);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

    }

    protected void onResume() {
        super.onResume();
        IronSource.onResume(this);
        loadIronBanner();

    }

    protected void onPause() {
        super.onPause();
        IronSource.onPause(this);
    }

    private void loadIronBanner() {
        appkey = getString(R.string.APPKEY);
        IronSource.init(this, appkey, IronSource.AD_UNIT.BANNER);

        final FrameLayout bannerContainer = findViewById(R.id.banner1);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);

        mIronSourceBannerLayout = IronSource.createBanner(this, new ISBannerSize(320, 50));

        bannerContainer.addView(mIronSourceBannerLayout, 0, layoutParams);
        mIronSourceBannerLayout.setBannerListener(new BannerListener() {
            @Override
            public void onBannerAdLoaded() {
                // Called after a banner ad has been successfully loaded
                Log.e(TAG, "onBannerAdLoaded: ");
                bannerContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onBannerAdLoadFailed(IronSourceError error) {
                // Called after a banner has attempted to load an ad but failed.
                Log.e(TAG, "onBannerAdLoadFailed: " + error.getErrorMessage());
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        bannerContainer.removeAllViews();
//                    }
//                });
            }

            @Override
            public void onBannerAdClicked() {
                // Called after a banner has been clicked.
            }

            @Override
            public void onBannerAdScreenPresented() {
                // Called when a banner is about to present a full screen content.
            }

            @Override
            public void onBannerAdScreenDismissed() {
                // Called after a full screen content has been dismissed
                Log.e(TAG, "onBannerAdScreenDismissed: ");
            }

            @Override
            public void onBannerAdLeftApplication() {
                // Called when a user would be taken out of the application context.
            }
        });
        IronSource.loadBanner(mIronSourceBannerLayout);

    }

    InstallStateUpdatedListener installStateUpdatedListener = state -> {
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            showCompletedUpdate();
        }
    };


    private void showCompletedUpdate() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "New app is ready!", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Install", view -> appUpdateManager.completeUpdate());
        snackbar.show();
    }

    @Override
    protected void onStop() {
        if (appUpdateManager != null)
            appUpdateManager.unregisterListener(installStateUpdatedListener);
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if (requestCode == FLEXIBLE_APP_UPDATE_REQ_CODE && resultCode != RESULT_OK) {
            Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //Fragment Container
    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }

    @Override
    public void onItemSelected(int i) {
        Fragment fragment = null;
        if (i == R.id.home) {
            if (navigationView != null) {
                navigationView.getMenu().getItem(0).setChecked(true);
            }
            Objects.requireNonNull(getSupportActionBar()).setTitle("Home");
            fragment = new HomeFragment();
        } else if (i == R.id.favourite) {
            navigationView.getMenu().getItem(1).setChecked(true);
            Objects.requireNonNull(getSupportActionBar()).setTitle("Favourite");
            fragment = new FavouriteFragment();
        } else if (i == R.id.floatmenu) {
            navigationView.getMenu().getItem(2).setChecked(true);
            Objects.requireNonNull(getSupportActionBar()).setTitle("Floating Bubble");
            fragment = new BubbleFragment();
        } else if (i == R.id.settingmenu) {
            navigationView.getMenu().getItem(3).setChecked(true);
            Objects.requireNonNull(getSupportActionBar()).setTitle("Settings");
            fragment = new SettingFragment();
        }
        loadFragment(fragment);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(!item.isChecked());
        drawerLayout.closeDrawers();

        int id = item.getItemId();
        //home menu
        if (id == R.id.nav_home) {
            navigationView.getMenu().getItem(0).setChecked(true);
            navBar.setItemSelected(R.id.home, true);
            new HomeActivity();
            return true;
            //favourite list
        } else if (id == R.id.nav_fav) {
            navigationView.getMenu().getItem(1).setChecked(true);
            navBar.setItemSelected(R.id.favourite, true);
            new FavouriteFragment();
            return true;
            //floating menu
        } else if (id == R.id.nav_float) {
            navigationView.getMenu().getItem(2).setChecked(true);
            navBar.setItemSelected(R.id.floatmenu, true);
            new BubbleFragment();
            return true;
            //Setting menu
        } else if (id == R.id.nav_settings) {
            navigationView.getMenu().getItem(3).setChecked(true);
            navBar.setItemSelected(R.id.settingmenu, true);
            new FavouriteFragment();
            return true;
            //Share APP
        } else if (id == R.id.nav_share) {
            share();
            return true;
            //Send Email
        } else if (id == R.id.nav_send) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/html");
            intent.putExtra(Intent.EXTRA_EMAIL, getString(R.string.email));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
            intent.putExtra(Intent.EXTRA_TEXT, "I'm email body.");
            startActivity(Intent.createChooser(intent, "Send Email"));
            return true;
        }
        return true;


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    //Share APP Link
    private void share() {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            String sAux = "\n" + getResources().getString(R.string.Let_me_recommend_you_this_application) + "\n\n";
            sAux = sAux + "https://play.google.com/store/apps/details?id=" + getApplication().getPackageName();
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(i, "choose one"));
        } catch (Exception e) {
            //e.toString();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            showExit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.info) {
            Intent intent = new Intent(this, HelpActivity.class);
            LoadHomeIronInterstital(intent);

        }

        return true;
    }

    //Exit Dialog
    private void showExit() {
        final Dialog customDialog;
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") View customView = inflater.inflate(R.layout.layout_exit, null);
        customDialog = new Dialog(this, R.style.DialogCustomTheme);
        customDialog.setContentView(customView);
        Button no = customDialog.findViewById(R.id.tv_no);
        Button yes = customDialog.findViewById(R.id.tv_yes);

        no.setOnClickListener(v -> customDialog.dismiss());

        yes.setOnClickListener(v -> finish());

        customDialog.show();
    }

    public void LoadHomeIronInterstital(Intent intent) {
        MyGlob.adsclick++;

        if (MyGlob.isOnline(HomeActivity.this)) {
            if (MyGlob.adsclick >= MyGlob.adShowAfter) {
                Dialog progressDialog = new Dialog(HomeActivity.this);
                progressDialog.setContentView(R.layout.ad_loading);
                progressDialog.setCancelable(false);
                progressDialog.show();
                progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                IronSource.loadInterstitial();
                IronSource.setInterstitialListener(new InterstitialListener() {
                    @Override
                    public void onInterstitialAdReady() {
                        Log.e(TAG, "onInterstitialAdReady: Interstitial first is ready");
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        IronSource.showInterstitial();
                    }

                    @Override
                    public void onInterstitialAdLoadFailed(IronSourceError ironSourceError) {
                        Log.e(TAG, "onInterstitialAdLoadFailed: Interstitial first is failed");
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        startActivity(intent);
                    }

                    @Override
                    public void onInterstitialAdOpened() {

                    }

                    @Override
                    public void onInterstitialAdClosed() {
                        Log.e(TAG, "onInterstitialAdClosed: ");
                        MyGlob.adsclick = 0;
                        startActivity(intent);
                    }

                    @Override
                    public void onInterstitialAdShowSucceeded() {
                        Log.e(TAG, "onInterstitialAdShowSucceeded first is showed on fullscreen");
                    }

                    @Override
                    public void onInterstitialAdShowFailed(IronSourceError ironSourceError) {
                        Log.e(TAG, "onInterstitialAdShowFailed: " + ironSourceError.getErrorMessage());
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        startActivity(intent);
                    }

                    @Override
                    public void onInterstitialAdClicked() {

                    }
                });

            } else {
                Log.e(TAG, "loadIronInterSwap: directly else block");
                startActivity(intent);
            }
        } else {
            startActivity(intent);
        }

    }

}