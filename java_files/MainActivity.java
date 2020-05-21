package com.example.take_a_shower;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    int request_code = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
    public void Empezar (View v){
        Intent intent = new Intent( v.getContext(),arduino.class);
        startActivityForResult(intent, request_code);
    }
    //METODO QUE SE EJECUTARA UNA VEZ FINALIZE LA CLASE lista_dispositivos, (con esto obtengo la MAC)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(request_code, resultCode, data);
        if ((requestCode == request_code) && (resultCode == RESULT_OK)){

        }
    }

}
