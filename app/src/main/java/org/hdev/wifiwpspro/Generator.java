package org.hdev.wifiwpspro;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.phearme.macaddressedittext.MacAddressEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Generator extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "TAG" ;
    MacAddressEditText mac_adress;
    Button button;
    TextView textView;
    String mac;
    String[] charSeqence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generator);
        mac_adress = findViewById(R.id.editeur_text);
        button = findViewById(R.id.btn);
        textView = findViewById(R.id.textView2);
        final Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        List<String> categories = new ArrayList<>();
        categories.add("ZhaoChunsheng Algorithm");
        categories.add("Arcadyan Algorithm");
        categories.add("ArrisDG860A Algorithm");
        categories.add("Dlink Algorithm");
        categories.add("Dlinkplus1 Algorithm");
        categories.add("Asus Algorithm");
        categories.add("airocon Algorithm");
        textView.setTextIsSelectable(true);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    mac = mac_adress.getText().toString();
                    Log.d("Mac: ",mac);
                    charSeqence = Extra.calculePINNew(mac);
                    Log.d(TAG, Arrays.toString(charSeqence));
                    int i = spinner.getSelectedItemPosition();
                    switch (i){
                        case 0:
                            textView.setText(charSeqence[0]);
                            break;
                        case 1:
                            textView.setText(charSeqence[1]);
                            break;
                        case 2:
                            textView.setText(charSeqence[2]);
                            break;
                        case 3:
                            textView.setText(charSeqence[3]);
                            break;
                        case 4:
                            textView.setText(charSeqence[4]);
                            break;
                        case 5:
                            textView.setText(charSeqence[5]);
                            break;
                        case 6:
                            textView.setText(charSeqence[6]);
                            break;
                            default:
                                break;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(Generator.this, "Please Enter Mac Address", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        try{
            mac="";
            charSeqence = null;
            switch (i){
                case 0:
                    textView.setText("");
                case 1:
                    textView.setText("");
                case 2:
                    textView.setText("");
                case 3:
                    textView.setText("");
                case 4:
                    textView.setText("");
                case 5:
                    textView.setText("");
                case 6:
                    textView.setText("");
            }
        }catch (Exception e){
            Log.d(TAG,"Enter Mac");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
