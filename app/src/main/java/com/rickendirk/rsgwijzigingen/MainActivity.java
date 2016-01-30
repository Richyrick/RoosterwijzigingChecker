/*
 *    This file is part of RSG-Wijzigingen.
 *
 *     RSG-Wijzigingen is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     RSG-Wijzigingen is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with RSG-Wijzigingen.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.rickendirk.rsgwijzigingen;



import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    ViewPager viewPager;
    int animDuration;
    Tracker tracker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        setupViewPager();

        animDuration = getResources().getInteger(android.R.integer.config_longAnimTime);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        setTitle("Roosterwijzigingen");

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int itemID = viewPager.getCurrentItem();

                if (itemID == 0) {
                    //Persoonlijke pagina, dus checken
                    MainFragment mainFragment = (MainFragment) adapter.getItem(itemID);
                    mainFragment.checker();
                }
                if (itemID == 1) {
                    //Algemeen, dus pagina vernieuwen
                    WebFragment webFragment = (WebFragment) adapter.getItem(itemID);
                    webFragment.refresh();
                }

            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1) {//webview fragment
                    WebFragment webFragment = (WebFragment) adapter.getItem(position);
                    boolean isFinished = webFragment.isFinished();
                    boolean isLoading = webFragment.isLoading();
                    if (!isLoading && !isFinished) {
                        webFragment.refresh();
                    }
                    fab.hide();
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Navigatie")
                            .setAction("naar_webFragment")
                            .build());

                } else {
                    //Gewone fragment, dus toolbar moet weer bovenaan gaan staan
                    fab.show();
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Navigatie")
                            .setAction("naar_mainFragment")
                            .build());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        check1ekeer();
        OwnApplication application = (OwnApplication) getApplication();
        tracker = application.getDefaultTracker();
        tracker.setScreenName("onCreate_MainAcitivity");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());

        Intent ownIntent = getIntent();
        boolean isVanNotificatie = ownIntent.getBooleanExtra("isVanNotificatie", false);
        if (isVanNotificatie){
            NotificationManagerCompat manager = NotificationManagerCompat.from(this);
            manager.cancel(ZoekService.notifID);
        }
    }
    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        FragmentManager fmg = getSupportFragmentManager();
        List<Fragment> list = fmg.getFragments();
        if (list == null){
            //Geen bestaande fragments, dus nieuwe fragments toevoegen
            adapter.addFrag(new MainFragment(), "Persoonlijk");
            adapter.addFrag(new WebFragment(), "Algemeen");
        }
        else {
            //Al wel bestaande fragments, dus die weer aan de adapter koppelen
            adapter.addFrag(list.get(0), "Persoonlijk");
            adapter.addFrag(list.get(1), "Algemeen");
        }
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        tracker.setScreenName("onResume_MainActivity");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) { //MainFragment, app aflsuiten
            super.onBackPressed();
        } else{ //Naar vorig item gaan
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_settings:
                openSettings();
                break;
            case R.id.action_refresh:
                int fragmentID = viewPager.getCurrentItem();
                List<Fragment> list = getSupportFragmentManager().getFragments();
                if (fragmentID == 0){
                    MainFragment mainFragment = (MainFragment) list.get(0);
                    mainFragment.checker();
                } else {
                    WebFragment webFragment = (WebFragment) list.get(1);
                    webFragment.refresh();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openSettings() {
        Intent settingsIntent = new Intent(getApplicationContext(),
                SettingsActivity.class);
        startActivityForResult(settingsIntent, 1874);

    }
    public void check1ekeer(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean is1eKeer = sp.getBoolean("1ekeer", true);
        if(is1eKeer){

            //Wizard starten
            Intent wizardInt = new Intent(getApplicationContext(),
                    WizardActivity.class);
            startActivityForResult(wizardInt, 1903);
            //Mag niet volgende keer weer starten
            is1eKeer = false;
            SharedPreferences.Editor spEditor = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext()).edit();
            spEditor.putBoolean("1ekeer", is1eKeer);
            spEditor.commit();
        }
    }
}
