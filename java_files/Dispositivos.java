package com.example.take_a_shower;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

public class Dispositivos extends AppCompatActivity {

    ListView lista;
    ArrayAdapter<String> adaptador;
    private BluetoothSocket btSocket = null;
    private BluetoothAdapter BTadaptador;
    String mac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispositivos);

        lista = (ListView) findViewById(R.id.listView);

        adaptador = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        lista.setAdapter(adaptador);   //estructura de la lista

        lista.setOnItemClickListener(listener_adp);
    }
    public void activar_blue(){
        BTadaptador=BluetoothAdapter.getDefaultAdapter();
        if (!BTadaptador.isEnabled()) {
            //si no el adaptador bluetooth no esta disponible entonces preguntar si quiere activarlo
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }
    @Override
    protected void onResume(){
        super.onResume();

        activar_blue();
        adaptador.clear(); //borramos los dispositivos vinculados cada vez que se refresque
        BTadaptador = BluetoothAdapter.getDefaultAdapter(); //adaptador por defecto

        //pasamos los dispositivos vinculados a un arreglo de tipo BluetoothDevice
        Set<BluetoothDevice> dispositivos = BTadaptador.getBondedDevices();

        if(dispositivos.size()>0) {
            for (BluetoothDevice item : dispositivos) {
                adaptador.add(item.getName()+ "\n" + item.getAddress());

            }
        }else{adaptador.add("NO HAY DISPOSITIVOS VINCULADOS");}

    }
    private AdapterView.OnItemClickListener listener_adp = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> av, View view, int arg2, long arg3) {
            String nom_y_mac = ((TextView) view).getText().toString();
            mac = nom_y_mac.substring(nom_y_mac.length()-17); //como solo necesitamos la mac, y la mac tiene 17 digitos, tomamos solo los ultimos 17 digitos
            //regresamos a la clase main con la mac
            Intent data = new Intent();
            // data.setData(Uri.parse("hola"));
            data.putExtra("mac", mac);
            setResult(RESULT_OK, data);
            finish();
        }
    };
}
