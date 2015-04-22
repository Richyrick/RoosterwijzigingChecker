package com.android.rickapps.roosterwijzigingchecker;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


public class MainActivity extends ActionBarActivity {
    ArrayList<String> wijzigingenList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Listview beheren waarop wijzigingen komen te staan
        ListView listView = (ListView) findViewById(R.id.wijzigingenList);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                MainActivity.this,
                android.R.layout.simple_list_item_1,
                wijzigingenList);
        listView.setAdapter(arrayAdapter);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            openSettings();
        }
        if (id == R.id.action_refresh) {
            checker(findViewById(R.id.button));
        }


        return super.onOptionsItemSelected(item);
    }

    //Onderstaand puur tijdelijk voor testdoeleinden
    private void openSettings() {
        Intent settingsIntent = new Intent(getApplicationContext(),
                SettingsActivity.class);
        startActivityForResult(settingsIntent, 1874);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1874){
            button_bold();
        }
    }

    private void button_bold() {
        Button button = (Button) findViewById(R.id.button);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("pref_but_bold", false)){
            button.setTypeface(null, Typeface.BOLD);
        } else button.setTypeface(null, Typeface.NORMAL);


    }

    public void checker(View view){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean clusters_enabled = sp.getBoolean("pref_cluster_enabled", false);
        if (clusters_enabled){
        new CheckerClusters().execute();}
        else new CheckerClass().execute();
        }
    //Zoekalgoritme voor klassen
    class CheckerClass extends AsyncTask<Void, Void, ArrayList> {

        @Override
        public ArrayList doInBackground(Void... params) {
            //String halen uit SP
            String klasTextS = PreferenceManager.getDefaultSharedPreferences
                    (getApplicationContext()).getString("pref_klas", "");
            String url = "http://www.rsgtrompmeesters.nl/roosters/roosterwijzigingen/Lijsterbesstraat/subst_001.htm";
            wijzigingenList.clear();
            //Checken of klas niet leeg is
            if (klasTextS.equals("")){
                wijzigingenList.add("Voer een klas in in het instellingenscherm.");
                return wijzigingenList;
            }
            //String opsplitsen in 2 delen, om naar hoofdletters te converteren
            char charcijfer = klasTextS.charAt(0);
            String klascijfer = String.valueOf(charcijfer);
            char charafdeling = klasTextS.charAt(1);
            String klasafdelingBig = String.valueOf(charafdeling).toUpperCase();
            //Sommige klassen hebben 2 delen, andere 3, andere 4
            String klasCorrect= "5Va";
            //Onderstaand bij 3-delige klas, laatste deel moet kleine letter zijn.
            if(klasTextS.length() == 3){
                char klasabc = klasTextS.charAt(2);
                String klasabcSmall = String.valueOf(klasabc).toLowerCase();
                klasCorrect = klascijfer + klasafdelingBig + klasabcSmall;
            }
            if (klasTextS.length() == 4){
                char klasafdeling2 = klasTextS.charAt(2);
                String klasafdeling2Big = String.valueOf(klasafdeling2).toUpperCase();
                char klasabc = klasTextS.charAt(3);
                String klasabcSmall = String.valueOf(klasabc).toLowerCase();

                klasCorrect = klascijfer + klasafdelingBig + klasafdeling2Big + klasabcSmall;
            }
            //Onderstaand bij 2-delige klas
            if (klasTextS.length() == 2){
                klasCorrect = klascijfer + klasafdelingBig;
            }
            //Klas opslaan in SP
            SharedPreferences.Editor SPEditor = getPreferences(Context.MODE_PRIVATE).edit();
            SPEditor.putString("KLAS", klasCorrect);
            SPEditor.apply();

            //Try en catch in het geval dat de internetverbinding mist
            try {
                    Document doc = Jsoup.connect(url).get();
                    Element table = doc.select("table").get(1);
                    Elements rows = table.select("tr");
                    //Loop genereren, voor elke row kijken of het de goede tekst bevat
                    //Beginnen bij 4e, bovenstaande is niet belangrijk
                    for (int i = 2; i < rows.size(); i++) {
                        Element row = rows.get(i);
                        Elements cols = row.select("td");


                        if (cols.get(0).text().contains(klasCorrect)) {
                            //If in geval van uitval, else ingeval van wijziging
                            if (Jsoup.parse(cols.get(6).toString()).text().contains("--")){
                                String wijziging =
                                        Jsoup.parse(cols.get(1).toString()).text() + "e uur " +
                                        Jsoup.parse(cols.get(2).toString()).text() + " valt uit.";
                                wijzigingenList.add(wijziging);
                            }
                            else {

                            String wijziging =
                                    // Voegt alle kolommen samen tot 1 string
                                    // .text() zorgt voor leesbare text
                                    // Spaties voor leesbaarheid
                                    Jsoup.parse(cols.get(1).toString()).text() + "e uur " +
                                    Jsoup.parse(cols.get(2).toString()).text() + " " +
                                    Jsoup.parse(cols.get(3).toString()).text() + " wordt " +
                                    Jsoup.parse(cols.get(4).toString()).text() + " " +
                                    Jsoup.parse(cols.get(5).toString()).text() + " in " +
                                    Jsoup.parse(cols.get(6).toString()).text() + " " +
                                    Jsoup.parse(cols.get(7).toString()).text() + " " +
                                    Jsoup.parse(cols.get(8).toString()).text() + " ";
                            wijzigingenList.add(wijziging);}

                        }
                        //Geen wijzigingen pas bij laatste rij
                        if (i == rows.size() - 1){
                                  //Checken of wijzigingenList leeg is, zo ja 1 ding toevoegen
                                  if (wijzigingenList.isEmpty()){
                                      wijzigingenList.add("Er zijn geen wijzigingen.");
                                  }
                            return wijzigingenList;
                        }


                   }
            }
            catch(java.io.IOException e) {
                    //Error toevoegen aan wijzigingenList, dat wordt weergegeven in messagebox
                    wijzigingenList.clear();
                    wijzigingenList.add("Er was een verbindingsfout");
                    return wijzigingenList;
            }
            //AS wilt graag een return statment: here you go
            return null;
        }
        public void onPostExecute(ArrayList wijzigingenList){
            //ListView updaten om roosterwijzigingen te laten zien
            ListView listView = (ListView) findViewById(R.id.wijzigingenList);
                listView.invalidateViews();
        }

    }
    //Zoekalgoritme voor clusters
    class CheckerClusters extends AsyncTask<Void, Void, ArrayList> {

        @Override
        public ArrayList doInBackground(Void... params) {
            //String van klas halen uit SP
            String klasTextS = PreferenceManager.getDefaultSharedPreferences
                    (getApplicationContext()).getString("pref_klas", "");
            String url = "http://www.rsgtrompmeesters.nl/roosters/roosterwijzigingen/Lijsterbesstraat/subst_001.htm";
            wijzigingenList.clear();
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            //Clusters ophalen uit SP
            ArrayList<String> clusters = new ArrayList<>();
            for (int a = 1; a < 11; a++){
                String cluster = sp.getString("pref_cluster" + a, "");
                clusters.add(cluster);
            }
            //Lege clusters weghalen uit arraylist TODO: Kijken of singleton werkt/wat het is
            clusters.removeAll(Collections.singleton(null));


            //Checken of klas niet leeg is
            if (klasTextS.equals("")){
                wijzigingenList.add("Voer een klas in in het instellingenscherm.");
                return wijzigingenList;
            }
            //String opsplitsen in 2 delen, om naar hoofdletters te converteren
            char charcijfer = klasTextS.charAt(0);
            String klascijfer = String.valueOf(charcijfer);
            char charafdeling = klasTextS.charAt(1);
            String klasafdelingBig = String.valueOf(charafdeling).toUpperCase();
            //Sommige klassen hebben 2 delen, andere 3, andere 4
            String klasCorrect= "5Va";
            //Onderstaand bij 3-delige klas, laatste deel moet kleine letter zijn.
            if(klasTextS.length() == 3){
                char klasabc = klasTextS.charAt(2);
                String klasabcSmall = String.valueOf(klasabc).toLowerCase();
                klasCorrect = klascijfer + klasafdelingBig + klasabcSmall;
            }
            if (klasTextS.length() == 4){
                char klasafdeling2 = klasTextS.charAt(2);
                String klasafdeling2Big = String.valueOf(klasafdeling2).toUpperCase();
                char klasabc = klasTextS.charAt(3);
                String klasabcSmall = String.valueOf(klasabc).toLowerCase();

                klasCorrect = klascijfer + klasafdelingBig + klasafdeling2Big + klasabcSmall;
            }
            //Onderstaand bij 2-delige klas
            if (klasTextS.length() == 2){
                klasCorrect = klascijfer + klasafdelingBig;
            }
            //Klas opslaan in SP
            SharedPreferences.Editor SPEditor = getPreferences(Context.MODE_PRIVATE).edit();
            SPEditor.putString("KLAS", klasCorrect);
            SPEditor.apply();

            //Try en catch in het geval dat de internetverbinding mist
            try {
                Document doc = Jsoup.connect(url).get();
                Element table = doc.select("table").get(1);
                Elements rows = table.select("tr");
                //Loop genereren, voor elke row kijken of het de goede tekst bevat
                //Beginnen bij 4e, bovenstaande is niet belangrijk
                for (int i = 2; i < rows.size(); i++) {
                    Element row = rows.get(i);
                    Elements cols = row.select("td");


                    if (cols.get(0).text().contains(klasCorrect)) {
                        //If in geval van uitval, else ingeval van wijziging
                        if (Jsoup.parse(cols.get(6).toString()).text().contains("--")){
                            String wijziging =
                                    Jsoup.parse(cols.get(1).toString()).text() + "e uur " +
                                            Jsoup.parse(cols.get(2).toString()).text() + " valt uit.";
                            wijzigingenList.add(wijziging);
                        }
                        else {

                            String wijziging =
                                    // Voegt alle kolommen samen tot 1 string
                                    // .text() zorgt voor leesbare text
                                    // Spaties voor leesbaarheid
                                    Jsoup.parse(cols.get(1).toString()).text() + "e uur " +
                                            Jsoup.parse(cols.get(2).toString()).text() + " " +
                                            Jsoup.parse(cols.get(3).toString()).text() + " wordt " +
                                            Jsoup.parse(cols.get(4).toString()).text() + " " +
                                            Jsoup.parse(cols.get(5).toString()).text() + " in " +
                                            Jsoup.parse(cols.get(6).toString()).text() + " " +
                                            Jsoup.parse(cols.get(7).toString()).text() + " " +
                                            Jsoup.parse(cols.get(8).toString()).text() + " ";
                            wijzigingenList.add(wijziging);}

                    }
                    //Geen wijzigingen pas bij laatste rij
                    if (i == rows.size() - 1){
                        //Checken of wijzigingenList leeg is, zo ja 1 ding toevoegen
                        if (wijzigingenList.isEmpty()){
                            wijzigingenList.add("Er zijn geen wijzigingen.");
                        }
                        return wijzigingenList;
                    }


                }
            }
            catch(java.io.IOException e) {
                //Error toevoegen aan wijzigingenList, dat wordt weergegeven in messagebox
                wijzigingenList.clear();
                wijzigingenList.add("Er was een verbindingsfout");
                return wijzigingenList;
            }
            //AS wilt graag een return statment: here you go
            return null;
        }
        public void onPostExecute(ArrayList wijzigingenList){
            //ListView updaten om roosterwijzigingen te laten zien
            ListView listView = (ListView) findViewById(R.id.wijzigingenList);
            listView.invalidateViews();
        }

    }
}
