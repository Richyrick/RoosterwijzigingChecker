package com.android.rickapps.roosterwijzigingchecker;


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
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MainActivity extends ActionBarActivity {
    @SuppressWarnings("unchecked")
    public ArrayList<String> wijzigingenList = new ArrayList<>();
    public Toast toast;

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
        //listView updaten met oude wijzigingen: eerst Set ophalen van SP
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Set<String> wijzigingenSet = sp.getStringSet("last_wijzigingenList", null);
        //nullpointer voorkomen
        if (wijzigingenSet != null){
            List<String> wijzigingenList_old = new ArrayList<>(wijzigingenSet);
            //Loop om wijzigingen van ene arrayList naar andere over te zetten
            for (int i = 0; i < wijzigingenList_old.size(); i++){
                wijzigingenList.add(wijzigingenList_old.get(i));
            }
        }
        //listView updaten om eventuele wijzigingen te laten zien
        listView.invalidateViews();


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
        else new CheckerKlas().execute();
        }
    //Zoekalgoritme voor klassen
    class CheckerKlas extends AsyncTask<Void, Void, ArrayList> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Toast om gebruiker gerust te stellen
            toast = Toast.makeText(getApplicationContext(), "Zoeken is gestart", Toast.LENGTH_SHORT);
            toast.show();
        }
        @Override
        public ArrayList doInBackground(Void... params) {
            //String halen uit SP
            String klasTextS = PreferenceManager.getDefaultSharedPreferences
                    (getApplicationContext()).getString("pref_klas", "");
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
            String klasCorrect = "";
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

            //Try en catch in het geval dat de internetverbinding mist
            try {
                String url = "http://www.rsgtrompmeesters.nl/roosters/roosterwijzigingen/Lijsterbesstraat/subst_001.htm";
                Document doc = Jsoup.connect(url).get();
                //publishProgress maakt Toast om gebruiker op hoogte te houden
                publishProgress();
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
            return wijzigingenList;
        }
        public void onPostExecute(ArrayList wijzigingenList){
            //ListView updaten om roosterwijzigingen te laten zien
            ListView listView = (ListView) findViewById(R.id.wijzigingenList);
                listView.invalidateViews();
            //Toast om te laten weten dat er is geupdatet, mag niet als er een verbindingsfout was
            if (!wijzigingenList.get(0).equals("Er was een verbindingsfout")) {
                Toast.makeText(getApplicationContext(), "Vernieuwd", Toast.LENGTH_SHORT).show();
            }
            //List opslaan in SP
            Set<String> wijzigingenSet = new HashSet<>();
            wijzigingenSet.addAll(wijzigingenList);
            SharedPreferences.Editor spEditor = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext()).edit();
            spEditor.putStringSet("last_wijzigingenList", wijzigingenSet);
            spEditor.commit();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            //Als het snel gaat wordt toast al weergegeven
            if (toast.getView().getWindowVisibility() == View.VISIBLE){
                toast.setText("Tabel wordt doorzocht");
            } else{
            Toast.makeText(getApplicationContext(), "Tabel wordt doorzocht"
                    , Toast.LENGTH_SHORT).show();
            }
        }

    }
    //Zoekalgoritme voor clusters
    class CheckerClusters extends AsyncTask<Void, Void, ArrayList> {

        protected void onPreExecute() {
            super.onPreExecute();
            //Toast om gebruiker gerust te stellen
             toast = Toast.makeText(getApplicationContext(), "Zoeken met clusterfiltering is gestart"
                    , Toast.LENGTH_SHORT);
             toast.show();
        }

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
            for (int a = 1; a < 15; a++){
                //TODO: Eerste letter kleine letter maken
                String cluster = sp.getString("pref_cluster" + a, "");
                clusters.add(cluster);
            }
            //Lege clusters weghalen uit arraylist TODO: Kijken of singleton werkt/wat het is
            clusters.removeAll(Collections.singleton(""));


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
            String klasCorrect = null;
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
            //Try en catch in het geval dat de internetverbinding mist
            try {
                Document doc = Jsoup.connect(url).get();
                //publishProgress maakt Toast om gebruiker op hoogte te houden
                publishProgress();
                Element table = doc.select("table").get(1);
                Elements rows = table.select("tr");
                //Eerste loop is om 2e loop te herhalen voor iedere cluster, tweede loop
                //doorzoekt dan op zowel klas als cluster
                for (int b = 0; b < clusters.size(); b++){
                for (int i = 2; i < rows.size(); i++) {
                    Element row = rows.get(i);
                    Elements cols = row.select("td");


                    if (cols.get(0).text().contains(klasCorrect)
                            && cols.get(2).text().contains(clusters.get(b))) {
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
                    //Geen wijzigingen pas bij laatste rij en de laatste cluster
                    if (i == rows.size() - 1 && b == clusters.size() - 1){
                        //Checken of wijzigingenList leeg is, zo ja 1 ding toevoegen
                        if (wijzigingenList.isEmpty()){
                            wijzigingenList.add("Er zijn geen wijzigingen.");
                        }
                        return wijzigingenList;
                    }


                } }
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
            //Toast om te laten weten dat er is geupdatet, mag niet als er verbindingsfout was
            if (!wijzigingenList.get(0).equals("Er was een verbindingsfout")) {
                Toast.makeText(getApplicationContext(), "Vernieuwd", Toast.LENGTH_SHORT).show();
            }
            //List opslaan in SP
            Set<String> wijzigingenSet = new HashSet<>();
            wijzigingenSet.addAll(wijzigingenList);
            SharedPreferences.Editor spEditor = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext()).edit();
            spEditor.putStringSet("last_wijzigingenList", wijzigingenSet);
            spEditor.commit();



        }
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            if (toast.getView().getWindowVisibility() == View.VISIBLE){
                toast.setText("Tabel wordt doorzocht");
            } else{
                Toast.makeText(getApplicationContext(), "Tabel wordt doorzocht"
                        , Toast.LENGTH_SHORT).show();}
        }

    }

}
