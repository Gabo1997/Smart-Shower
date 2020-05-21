package com.example.take_a_shower;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class arduino extends AppCompatActivity {

    private BluetoothSocket btSocket = null;
    Handler bluetoothIn;
    int request_code = 1;
    boolean ban = false;
    private BluetoothAdapter BTadaptador;
    private static String address = null;
    private ConnectedThread MyConexionBT;
    final int handlerState = 0;
    private StringBuilder DataStringIN = new StringBuilder();
    TextView eti_blue;
    //--------------var para motores
    TextView ANGULO_1, texto, ANGULO_2, texto2;
    SeekBar B_HOT;
    SeekBar B_COLD;
    String dato;
    String aux = "";
    String aux2 = "";
    //----------------------var para caudal
    TextView text_volumen;
    //identificador universal de el bluetooth
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arduino);

        ANGULO_1 = (TextView) findViewById(R.id.angulo);
        ANGULO_2 = (TextView) findViewById(R.id.angulo2);
        B_HOT = (SeekBar) findViewById(R.id.barra_caliente);
        B_COLD = (SeekBar) findViewById(R.id.barra_fria);
        texto = (TextView) findViewById(R.id.tex);
        texto2 = (TextView) findViewById(R.id.tex2);
        eti_blue = findViewById(R.id.eti_BT);
        text_volumen = findViewById(R.id.volumen);

        BTadaptador = BluetoothAdapter.getDefaultAdapter();
        activar_blue();

        Intent intent = new Intent( this,Dispositivos.class);
        startActivityForResult(intent, request_code);
    //LO QUE RECIBES DE ARDUINO
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                    dato += readMessage;

                    int final_servo1 =dato.indexOf("#");
                    int final_servo2 = dato.indexOf("~");
                    int final_caudal = dato.indexOf("V");
                    // Servo 1
                    if (final_servo1 > 0) {
                        dato = dato.substring(0, final_servo1);
                        texto.setText("Desde arduino : " + dato);//<-<- PARTE A MODIFICAR >->->
                        dato = "";
                    }else if(final_servo1 == 0){ dato = "";}
                    //servo 2
                    if (final_servo2 > 0) {
                        dato = dato.substring(0, final_servo2);
                        texto2.setText("Desde arduino : " + dato);//<-<- PARTE A MODIFICAR >->->
                        dato = "";
                    }else if(final_servo2 == 0){ dato = "";}
                    //caudal
                    if (final_caudal > 0){
                        try {
                            dato = dato.substring(0, final_caudal);
                          String arre[]= dato.split("\\.");
                          try {
                              int limite = Integer.parseInt(arre[0]);
                              if (limite >= 3){
                                  MyConexionBT.write(90+";");
                                  MyConexionBT.write(90+"%");
                                  Toast.makeText(arduino.this, "has llegado al limite ", Toast.LENGTH_SHORT).show();
                              }
                          }catch(NumberFormatException e){}

                        }catch (StringIndexOutOfBoundsException e){}
                        text_volumen.setText("VOLUMEN : " + dato + " litros");//<-<- PARTE A MODIFICAR >->->
                        dato = "";
                    }else if(final_caudal == 0){ dato = "";}

                }
            }
        };

        B_HOT.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //   ANGULO_1.setText(progress+" grados");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                if(!aux.equals(seekBar.getProgress()+"")){
                    // Toast.makeText(MainActivity.this, "Termina "+seekBar.getProgress(), Toast.LENGTH_SHORT).show();

                    int  giro = 90 - seekBar.getProgress();  //esta resta es para confundir al usuario porque el servo gira al lado contrario
                    MyConexionBT.write(giro+";");
                }
                aux = seekBar.getProgress()+"";

            }
        });
        B_COLD.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
               // ANGULO_2.setText(progress+" grados");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(!aux2.equals(seekBar.getProgress()+"")){
                    //  Toast.makeText(MainActivity.this, "Termina "+seekBar.getProgress(), Toast.LENGTH_SHORT).show();

                    int  giro = 90 - seekBar.getProgress();  //esta resta es para confundir al usuario porque el servo gira al lado contrario
                    MyConexionBT.write(giro+"%");
                }
                aux2 = seekBar.getProgress()+"";
            }
        });

    }
    //METODO QUE SE EJECUTARA UNA VEZ FINALIZE LA CLASE lista_dispositivos, (con esto obtengo la MAC)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(request_code, resultCode, data);
        if ((requestCode == request_code) && (resultCode == RESULT_OK)){
            ban = true;
            try {
                address = data.getStringExtra("mac");
            }catch (NullPointerException e){}
        }
    }
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        //crea un conexion de salida segura para el dispositivo
        //usando el servicio UUID
        return device.createRfcommSocketToServiceRecord(MY_UUID);
    }
    protected void onResume(){
        super.onResume();
        if (ban){ //si ya extraimos la mac del dispositivo
            BluetoothDevice device = BTadaptador.getRemoteDevice(address);
            try
            {
                btSocket = createBluetoothSocket(device);
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
            }
            // Establece la conexión con el socket Bluetooth.
            try
            {
                btSocket.connect();
            } catch (IOException e) {
                try {
                    btSocket.close();
                } catch (IOException e2) {}
            }
            MyConexionBT = new ConnectedThread(btSocket);
            MyConexionBT.start();

        }
    }
    public void activar_blue() {
        BTadaptador = BluetoothAdapter.getDefaultAdapter();
        if (!BTadaptador.isEnabled()) {
            //si no el adaptador bluetooth no esta disponible entonces preguntar si quiere activarlo
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }
    @Override
    public void onPause()
    {
        super.onPause();
        if(ban) {
            try { // Cuando se sale de la aplicación esta parte permite
                // que no se deje abierto el socket
                btSocket.close();
            } catch (IOException e2) {
            }
        }
    }

    //Crea la clase que permite crear el evento de conexion
    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    // Envia los datos obtenidos hacia el evento via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //Envio de trama
        public void write(String input)
        {
            try {
                mmOutStream.write(input.getBytes());
            }
            catch (IOException e)
            {
                //si no es posible enviar datos se cierra la conexión
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    public void checar_bt(View v){
        try
        {
            if (btSocket.isConnected()){
                eti_blue.setText("CONECTADO");
                eti_blue.setTextColor(Color.BLUE);
            }else{
                eti_blue.setText("DESCONECTADO");
                eti_blue.setTextColor(Color.RED);
            }
        } catch (Exception e) {
            eti_blue.setText("error");
        }

    }
    public void terminar(View v){
        Intent data = new Intent();
        setResult(RESULT_OK, data);
        finish();
    }
}
