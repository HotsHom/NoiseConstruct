package com.example.noiseconstruct;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Telephony;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.noiseconstruct.module.defaultMicro;
import com.example.noiseconstruct.module.modulus;
import com.example.noiseconstruct.module.processPitch;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.TarsosDSPAudioInputStream;
import be.tarsos.dsp.io.android.AndroidAudioInputStream;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.util.fft.FFT;

import static android.media.AudioRecord.READ_BLOCKING;
import static android.media.AudioRecord.READ_NON_BLOCKING;

public class MainActivity extends AppCompatActivity {


    /**
     * Переменные
     */
    private int myBufferSize = 8182;
    private defaultMicro defMicro = new defaultMicro();
    private AudioDispatcher dispatcher = defMicro.fromDefaultMicrophone(100000,myBufferSize,0);
    private ArrayList<Float> pitches = new ArrayList<Float>();
    private ArrayList<Integer> amplitudes = new ArrayList<Integer>();
    private int check = 0;
    private boolean flag;
    private int u = 0;
    private Handler h;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @SuppressLint("HandlerLeak")
    private void updateStatus() {
        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                // обновляем TextView
                final Button finish = (Button) findViewById(R.id.btnFinishRecord);
                final Button startBtn = (Button) findViewById(R.id.btnStartRecord);
                startBtn.setVisibility(View.GONE);
                finish.setVisibility(View.VISIBLE);
                finish.setText("Выполнение: " + msg.what + "%");
                if (msg.what >= 100){
                    finish.setText("Далее..");
                }
            }

            ;
        };
    }

    @Override
    public void onBackPressed() {

    }

    /**
     * Обработчик нажатия на кнопку Start.
     *
     * 1. Записываем частоту в отдельном потоке.
     * 2. Записываем звук для определения амплитуды.
     */
    public void recordStart(View v) {
        Button startBtn = (Button) findViewById(R.id.btnStartRecord);
        FrameLayout record = (FrameLayout) findViewById(R.id.textRecord);
        Button finish = (Button) findViewById(R.id.btnFinishRecord);

        updateStatus();
        uiChange();
        RecordTask recordTask = new RecordTask();
        recordTask.execute();


        finish.setText("Обработка");
    }

    private void goW(){
        Button finish = (Button) findViewById(R.id.btnFinishRecord);
        finish.setText("Далее");
    }

    private void uiChange() {
//        final Button startBtn = (Button) findViewById(R.id.btnStartRecord);
//        final Button finish = (Button) findViewById(R.id.btnFinishRecord);
//        startBtn.setVisibility(View.GONE);
//        finish.setVisibility(View.VISIBLE);
//        finish.setText("Идёт обработка");
    }


    /**
     * Метод асинхронной записи частот и амплитуд с микрофона.
     */
    private void recordPitch() {
        PitchDetectionHandler pdh = new PitchDetectionHandler() {

            @Override
            public void handlePitch(PitchDetectionResult res, AudioEvent e) {
                final float pitchInHz = res.getPitch();
                final double grz = e.getRMS();
                final float[] buff = e.getFloatBuffer();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (pitchInHz != -1) {
                            pitches.add(pitchInHz);
                            float[] data = buff;
                            float[] amplit = new float[buff.length / 2];
                            modulus mod = new modulus();
                            FFT fft = new FFT(buff.length);
                            fft.forwardTransform(data);
                            mod.modulus(data, amplit);

                            int amp = (int) (10.0 * Math.log10(grz));

                            amplitudes.add(amp);

                            check++;
                            int res = (int) (check*100)/20;
                            h.sendEmptyMessage(res);
                        }
                        if (check == 20) {
                            defMicro.stopRecord();
                            Thread.currentThread().interrupt();
                        }
                    }
                });
            }
        };
        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.DYNAMIC_WAVELET, 100000, myBufferSize, pdh);
        dispatcher.addAudioProcessor(pitchProcessor);

        Thread audioThread = new Thread(dispatcher, "Audio Thread");
        audioThread.start();
    }


    public void doWork(View view) {
        defMicro.stopRecord();
        Intent intent = new Intent(this, CheckActivity.class);
        intent.putExtra("data_Pitch", pitches);
        intent.putExtra("data_Amplitudes", amplitudes);
        startActivity(intent);
    }

    class RecordTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            h.sendEmptyMessage(0);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            recordPitch();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (check >= 20){
                h.sendEmptyMessage(100);
            }
        }

    }
}
