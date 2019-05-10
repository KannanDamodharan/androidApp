package com.example.myapp123;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    TextView ProximitySensor, data;
    SensorManager mySensorManager;
    Sensor myProximitySensor;
    private TextView txtResult;
    SensorManager sensorManager;
    TextView tv_steps;
    TextView calorie;
    boolean running = false;
    float initialCount;
    int counter =0;
    MediaPlayer mediaPlayer;
    MediaPlayer mediaPlayer1;
    String moodButton="false";
    int moodOncount=0;
    Date initialTime;
    float noOfsteps,latestSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ProximitySensor = (TextView) findViewById(R.id.proximitySensor);
        data = (TextView) findViewById(R.id.data);
        mySensorManager = (SensorManager) getSystemService(
                Context.SENSOR_SERVICE);
        myProximitySensor = mySensorManager.getDefaultSensor(
                Sensor.TYPE_PROXIMITY);
        if (myProximitySensor == null) {
            ProximitySensor.setText("No Proximity Sensor!");
        } else {
            mySensorManager.registerListener(proximitySensorEventListener,
                    myProximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        txtResult = (TextView) findViewById(R.id.txvResult);
        tv_steps = (TextView) findViewById(R.id.tv_steps);
        calorie = (TextView) findViewById(R.id.calorie);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mediaPlayer = MediaPlayer.create(this,R.raw.slow);
        mediaPlayer1 = MediaPlayer.create(this,R.raw.fast);

        Button playSong = this.findViewById(R.id.play);
        playSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
            }
        });
        Button stopSong = findViewById(R.id.pause);
        stopSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.pause();
                mediaPlayer1.pause();
            }
        });

        Button mood = findViewById(R.id.moodOn);
        mood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if(moodButton.equalsIgnoreCase("false")) {
                //    mediaPlayer.start();
                    moodButton = "true";
                //    initialTime = Calendar.getInstance().getTime();
                //}
                //if(moodButton.equalsIgnoreCase("true")){
                  //  mediaPlayer.pause();
                   // moodButton = "false";
                //}
            }
        });

        Button moodOff = findViewById(R.id.moodOff);
        moodOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    moodButton = "false";
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        running = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if(countSensor!=null){
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        }
        else {
            Toast.makeText(this, "Sensore not found! ",Toast.LENGTH_SHORT).show();
        }
    }

    public void getSpeechInput(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 10:
                if(resultCode == RESULT_OK && data != null){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String hello = "stop";

                    if(closeTheApplication("close",result.get(0)) || closeTheApplication("stop",result.get(0))){
                        if(!closeTheApplication("don't",result.get(0))) {
                            if(!closeTheApplication("do not",result.get(0))) {
                                finish();
                                moveTaskToBack(true);
                            }
                        }
                        txtResult.setText(result.get(0));

                    }
                    else {
                        txtResult.setText(result.get(0));
                    }
                }
                break;
        }
    }

    private boolean closeTheApplication(String s1,String s2) {
        int M = s1.length();
        int N = s2.length();

        /* A loop to slide pat[] one by one */
        for (int i = 0; i <= N - M; i++) {
            int j;

            /* For current index i, check for
            pattern match */
            for (j = 0; j < M; j++)
                if (s2.charAt(i + j) != s1.charAt(j))
                    break;

            if (j == M)
                return true;
        }

        return false;
    }

    SensorEventListener proximitySensorEventListener
            = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub
        }
        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                if (event.values[0] == 0) {
                    finish();
                    moveTaskToBack(true);
                }
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        running=false;
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        if(running){
            if(counter==0){
                initialCount = event.values[0];
                counter++;
            }

            float currentStepVal = (float) event.values[0]-initialCount;
            tv_steps.setText(String.valueOf(event.values[0]));
            tv_steps.setText(String.valueOf(testTemp(event.values[0])));
            calorie.setText(String.valueOf(calorieBurned(event.values[0])));
            if(currentStepVal%(float) 5==(float) 0){
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(500);
            }

            //thread start
            Thread t=new Thread(){
                @Override
                public void run(){
                    while(!isInterrupted()){
                        try {
                            Thread.sleep(5000);  //1000ms = 1 sec
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    moodOncount++;
                                    //text1.setText(String.valueOf(count));

                                    //if(testTemp(event.values[0])>(float) 5 && moodOncount%3==0){
                                    if(testTemp(event.values[0])>(float) 2){
                                        latestSteps = noOfsteps;
                                    }
                                    noOfsteps = testTemp(event.values[0]);
                                    if(moodButton.equalsIgnoreCase("true")){
                                        //fast or slow
                                        if(noOfsteps - latestSteps<1){
                                            mediaPlayer.start();
                                        }
                                        if(noOfsteps - latestSteps>=1){
                                            mediaPlayer1.start();
                                        }
                                        /*if(noOfsteps>2){
                                            mediaPlayer.start();
                                        }
                                        if(noOfsteps>5){
                                            mediaPlayer.pause();
                                            mediaPlayer1.start();
                                        }*/

                                    }
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            };
            t.start();
            //THREAD END
        }
    }

    private float calorieBurned(float value) {
        float num = (float) 0.35;
        return (float) testTemp(value)*num;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public float testTemp(float value){
        /*float num = (float) 0;
        if(value==(float) 80){
            return num;
        }
        return value;*/
        //return value % (float) 30;
        return (float) value-initialCount;
    }
}