package com.example.ecr_intent_android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;


public class MainActivity extends AppCompatActivity {

    private Button mButton;
    private TextView mStatus;
    private TextView mAmount;

    MyModel myModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myModel = new ViewModelProvider(this).get(MyModel.class);

        setContentView(R.layout.activity_main);

        mStatus = findViewById(R.id.statusTransakcji);
        mButton = findViewById(R.id.button1);
        mAmount = findViewById(R.id.amount);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPayment();
            }
        });
        mStatus.setText(myModel.text);
        processExtraData();

        IntentFilter filter = new IntentFilter("ELECTRONIC_CASH_REGISTER");
        getApplicationContext().registerReceiver(mReceiver, filter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("A", "onResume text = " + myModel.text);
        mStatus.setText(myModel.text);
    }

    public void startPayment(){
        Log.i("A", "startPayment");
        mStatus.setText("Transakcja");
        myModel.text = "Transakcja";

        Intent intent = new Intent("android.intent.action.MCX_EFT");
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("Command", "PAYMENT");
        int amount = (int) (Float.parseFloat(mAmount.getText().toString())*100);

        // Amount can be sended as integer or as string
        // Unit is 1/100 PLN
        intent.putExtra("Amount", amount);   // integer gr
        //intent.putExtra("Amount", ""+amount);   // String gr

        // Curency in uppercase or lowercase
        // The terminal only supports PLN
        intent.putExtra("Currency", "PLN");

        // Any name used in manifest for intent filter
        // Terminal use this name for answer
        intent.putExtra("Sender", "ELECTRONIC_CASH_REGISTER");

        startActivity(intent);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("A", "BroadcastReceiver");
            String res = intent.getStringExtra("Result");
            mStatus.setText(res);
            myModel.text=res;
        }
    };

    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        processExtraData();
    }

    private void processExtraData(){
        Log.i("A", "process Intent");
        Intent intent = getIntent();

        if(intent.getAction().equals("android.intent.action.ELECTRONIC_CASH_REGISTER")) {
            int res = intent.getIntExtra("Result", 0);

            String strRes = intent.getStringExtra("strResult");
            if(strRes!=null && strRes.length()>0) {
                Log.i("A", "process Intent set text");
                String s = strRes+" (result no: "+res+")";
                mStatus.setText(s);
                myModel.text = s;
            }

        }
    }

}

/**
 * legend response:
 *    0 -> Transakcja zaakceptowana.
 *    1 -> Transakcja odrzucona.
 *    2 -> Brak połączenia.
 *    3 -> Transakcja odrzucona. Błąd PIN.
 *    4 -> Transakcja odrzucona. Karta nieobsługiwana.
 *    5 -> Transakcja odrzucona. Brak środków.
 *    6 -> Transakcja odrzucona. Karta jest nieważna.
 *    7 -> Transakcja anulowana.
 *    8 -> Transakcja odrzucona. Przekroczony limit czasu.
 *    9 -> Transakcja odrzucona. Kwota poniżej limitu.
 *   10 -> Transakcja odrzucona. Kwota powyżej limitu.
 *   11 -> Nieobsługiwana waluta
 *  255 -> Transakcja odrzucona. Inny powód.
 */

