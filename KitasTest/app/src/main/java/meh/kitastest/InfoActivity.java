package meh.kitastest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class InfoActivity extends AppCompatActivity {



    ImageView img;
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        img = (ImageView) findViewById(R.id.infoImage);
        text = (TextView) findViewById(R.id.info_text);

        final Spinner spinner = (Spinner) findViewById(R.id.infoSpinner);
        ArrayAdapter<String > adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,  public_stuff.sortedTOP);
// Specify the layout to use when the list of choices appears

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

// Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                //Toast.makeText(getApplicationContext(), position, Toast.LENGTH_LONG);
                //Log.d("D", position+"");
               // img.setImageResource(imgid[position]);
                text.setText(getResources().getStringArray(R.array.Info)[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        Bundle bundle = getIntent().getExtras();
        if(getIntent().hasExtra("kelintas")) {
            int lol = bundle.getInt("kelintas");
            spinner.setSelection(lol);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(InfoActivity.this, MainActivity.class));
    }
}
