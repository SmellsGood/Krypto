package meh.kitastest;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity  implements InterfaceForFragments{

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    private static final String PREFS_NAME = "prefs";
    private static final String PREF_LIGHT_THEME = "dark_theme";
    private FrameLayout MainFrame;
    private  BottomNavigationView toolbar;
    private MoneyFragment moneyFragment;
    private NewsFragment newsFragment;
    private cryptoExpFragment cryptoExpFragment;


    boolean useLightTheme;
    SharedPreferences preferences;
    Button money_button, options_button, graph_button, news_button, info_button;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_trends:
                    setFragment(moneyFragment,  null);
                    return true;
                case R.id.navigation_news:
                    setFragment(newsFragment,  null);
                    return true;
                case R.id.navigation_portfolio:

                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) { //onCreate is used to start an activity

    //  CODE FOR GETTING THE THEME
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        useLightTheme = preferences.getBoolean(PREF_LIGHT_THEME, false);
        enableTheme(useLightTheme);

        SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this); //patestina ar katik ijunge
        boolean isFirstRun = wmbPreference.getBoolean("FIRSTRUN", true);
        if (isFirstRun)
        {
            // Code to run once
            SharedPreferences.Editor editor = wmbPreference.edit();
            editor.putBoolean("FIRSTRUN", false);
            editor.commit();
        }

        super.onCreate(savedInstanceState);    //super is used to call the parent class constructor
        setContentView(R.layout.main_menu);    //setContentView is used to set the xml


        toolbar = (BottomNavigationView) findViewById(R.id.toolbar);    // for bottom bar
        toolbar.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        MainFrame = (FrameLayout) findViewById(R.id.mainFrame);
        moneyFragment = new MoneyFragment();
        moneyFragment.setInterfaceForFragments(this);
        newsFragment = new NewsFragment();
        cryptoExpFragment = new cryptoExpFragment();

        setFragment(newsFragment, null);

        //Atsiuntimas:
        data_download dl = new data_download(new data_download.AsyncCallback() {
            @Override
            public void gavauData(String result) {
                toliau(result);
            }
        });
        dl.webSite = "https://api.coinmarketcap.com/v1/ticker/?limit=10";
        dl.execute();



        if(isNetworkAvailable(getApplicationContext())) Toast.makeText(getApplicationContext(), "App won't work without Internet Connection!!!", Toast.LENGTH_LONG);

        String a = preferences.getString("topCrypto", "");
        public_stuff.sortedTOP = a.split(",");

        public_stuff.visas = parseJSONlocal("viskas.json");
    }
    public boolean usabilityLightTheme() {
        return useLightTheme;
    }

    public void toggleTheme(boolean lightTheme) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(PREF_LIGHT_THEME, lightTheme);
        editor.apply();

        Intent theme = getIntent();
        finish();

        startActivity(theme);
    }
    public void enableTheme(boolean useLightTheme){
        if(useLightTheme) setTheme(R.style.LightAppTheme);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */


    @Override
    public void onRestart(){
        super.onRestart();
        useLightTheme = preferences.getBoolean(PREF_LIGHT_THEME, false);
        enableTheme(useLightTheme);
        setContentView(R.layout.main_menu);

    }

    @Override
    public void onActionInFragment(Bundle bundle) {
                setFragment(cryptoExpFragment, bundle);
    }


    public boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private JSONArray parseJSONlocal(String folderName) {
        try {
            InputStream inputStream = getAssets().open(folderName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String string = new String(buffer, "UTF-8");
            JSONArray jsonArray = new JSONArray(string);
            return jsonArray;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }




    private void setFragment(android.support.v4.app.Fragment fragment, Bundle bundle) {
        if(bundle != null) { // jei egzisuoja bundle
            String mode = bundle.getString("KEY_MODE");
            switch (mode) {
                case "ListClick":
                    Bundle bundle2 = new Bundle();
                    int position = bundle.getInt("KEY_POSITION");
                    bundle2.putInt("KEY_POSITION", position); //is pirmo i antra
                    fragment.setArguments(bundle2);
                    break;
            }
        }
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mainFrame, fragment);
        fragmentTransaction.commit();
    }



    private void toliau(final String result){


        try {
            final JSONArray textas = new JSONArray(result);
            //Log.d("myTag", textas.getJSONObject(0).getString("symbol").toString());
            //Log.d("Log.d", textas.length()+"");
            public_stuff.money = textas;

            if(public_stuff.sortedOnce == false) {
                saveTOP(); //issaugot populiariausius
                public_stuff.sortedOnce = true;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void saveTOP() {
        StringBuilder sb = new StringBuilder();
        String PREFS_NAME = "prefs";
        SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        for(int x = 0; x < public_stuff.valiutuKiekis; x++) {
            String a = null;
            try {
                a = public_stuff.money.getJSONObject(x).getString("name").toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sb.append(a).append(",");
        }
        editor.putString("topCrypto", sb.toString());
        editor.commit();
    }
}
