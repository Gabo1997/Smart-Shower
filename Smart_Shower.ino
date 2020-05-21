#include <SoftwareSerial.h>
#include <Servo.h>

//------------variables para caudalimetro
volatile int NumPulsos; //cantidad de pulsos recibidos
int PinSensor = 2; //sensor concetado en el pin 2
float factor = 4; //para convertir la frecuencia a caudal
float volumen = 0;
long dt = 0;  //variacion de tiempo por cada bucle
long t0 = 0;  //miliseg del bucle anterior
int t = 0;
//---------------variables para servos y BT
Servo servo1;
Servo servo2;
SoftwareSerial miBT(10, 11); // TXD / RXD
  int PIN_SERVO1 = 6;
  int PIN_SERVO2 = 7;
  char DATO;
  String aux = "";
  String angulo1 = "";
  String angulo2 = "";
   
  //---------------------------------
  
void setup() {

  Serial.begin(9600);   //iniciamos la comunicacion serie
   miBT.begin(9600);
  // ----- mandamos el pin donde se conecta el servo a el metodo attach  
 servo1.attach(PIN_SERVO1);  
 servo2.attach(PIN_SERVO2);

 // --------- para el caudalimetro:
  attachInterrupt(0,ContarPulsos,RISING);   //Interrupcion 0 (Pin2), funcion,Flanco de subida
  t0 = millis();

}

void loop() {
  
  /////////////////////////////////////////// caudal
   float frecuencia = getFrecuencia();  //Obtenemos la frecuencia de los pulsos en Hz

  float caudal = frecuencia / factor;
  dt = millis() - t0;
  t0 = millis();
  volumen = volumen + (caudal/60) * (dt / 1000);  // volumen (L) = caudal(L/s) * tiempo(s)

  if(caudal != 0){
    t = t+1;
    }
    //enviamos por el BT
    String vol;
    vol = String(volumen);

   
    miBT.print(vol+"V");
    //enviamos por el puerto serie
    Serial.println(vol);
    //Serial.print(",");
    //Serial.print(caudal);
    //Serial.print(",");
    //Serial.println(t);

 //limite   
    if(volumen >= 3){
      volumen = 0;
      }
}

void ContarPulsos(){
  NumPulsos++;     //incrementamos la variable de pulsos
}
int getFrecuencia(){
  
  int frecuencia;
  NumPulsos = 0;   //Ponemos a 0 el numero de pulsos
  interrupts();   //habilitamos las interrupciones
  
   delay(1000);   //muestra de 1 segundo
   /////////////////////////////////////////
   while(miBT.available()){
     DATO = miBT.read(); 
      Serial.print(DATO);
    if(DATO == ';'){    // si es el servo 1
      angulo1 = aux;
       aux = "";
      break;  
     
   }else{ 
    if(DATO == '%'){  //si es el servo 2
      angulo2 = aux;
      aux = "";
      break;
      }else{
      
    aux += DATO;
        
      }
      }
   
    }
    //servo 1
    if(angulo1 != ""){
     
        servo1.write( angulo1.toInt());
        delay(50);
         miBT.print(angulo1+"#");
         
         Serial.println("--> "+angulo1);  
      }
      
       //servo 2
        if(angulo2 != ""){
     
        servo2.write( angulo2.toInt());
        delay(50);
         miBT.print(angulo2+"~");
         
         Serial.println("--> "+angulo2);  
      }
      
       angulo1 = "";
       angulo2 = "";
  ////////////////////////////////////////////// 
  noInterrupts(); //Desabilitamos las interrupciones
  frecuencia = NumPulsos;  //Hz (pulsos por segundo)
  return frecuencia;
}
