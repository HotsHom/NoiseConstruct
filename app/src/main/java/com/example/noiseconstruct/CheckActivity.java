package com.example.noiseconstruct;

import androidx.appcompat.app.AppCompatActivity;
import org.billthefarmer.mididriver.MidiDriver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.midi.MidiDevice;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.noiseconstruct.module.Gene;
import com.example.noiseconstruct.module.GeneticAlgorithm;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;


import be.tarsos.dsp.io.TarsosDSPAudioFloatConverter;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.pitch.FFTPitch;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.util.fft.FFT;

public class CheckActivity extends AppCompatActivity implements MidiDriver.OnMidiStartListener {

    private ArrayList<Float> pitches;
    private ArrayList<Integer> amplitudes;
    private int[][] population;
    private int[][] adaptability;
    private int[][] parentPopulation;
    private int[][] childrenPopulation;
    private GeneticAlgorithm geneticAlgorithm;
    private double status = 99.000001;
    private Handler h;
    private int[] successMelody = new int[60];

    private MidiDriver midiDriver;
    private byte[] event;
    private int[] config;
    private Button buttonPlayNote;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        Bundle arguments = getIntent().getExtras();
        pitches = (ArrayList<Float>) arguments.get("data_Pitch");
        amplitudes = (ArrayList<Integer>) arguments.get("data_Amplitudes");

        midiDriver = new MidiDriver();
        midiDriver.setOnMidiStartListener(this);
        midiDriver.start();
    }

    @SuppressLint("SetTextI18n")
    public void makeMusic(View view) throws InterruptedException {
        updateStatus();
        Thread.sleep(2000);
        Thread t = new Thread(new Runnable() {
            public void run() {
                runGeneticAlgorithm();
            }
        });
        t.start();
    }

    private void runGeneticAlgorithm() {
        h.sendEmptyMessage(0);
        geneticAlgorithm = new GeneticAlgorithm(pitches.toArray(new Float[pitches.size()]), amplitudes.toArray(new Integer[amplitudes.size()]));
        population = geneticAlgorithm.createPopulation();
        h.sendEmptyMessage(10);
        sleep(3000);
        adaptability = geneticAlgorithm.identificationOfFitness(population);
        h.sendEmptyMessage(20);
        sleep(3000);
        adaptability = geneticAlgorithm.quicksort(adaptability, 0, adaptability[0].length-1);
        h.sendEmptyMessage(22);
        adaptability = geneticAlgorithm.quicksort(adaptability, 0, adaptability[0].length-1);
        h.sendEmptyMessage(25);
        sleep(3000);
        parentPopulation = geneticAlgorithm.getParents(adaptability[0],population);
        h.sendEmptyMessage(30);
        sleep(3000);
        childrenPopulation = geneticAlgorithm.crossParent(parentPopulation);
        h.sendEmptyMessage(40);
        sleep(1000);
        childrenPopulation = geneticAlgorithm.Mutation(childrenPopulation, true);
        h.sendEmptyMessage(50);
        sleep(3000);
        population = geneticAlgorithm.combiningPopulation(parentPopulation, childrenPopulation);
        h.sendEmptyMessage(99);
        sleep(3000);
        basicGenAlgorithm();
    }

    private void basicGenAlgorithm(){
        int COUNT_SUCCESS = 0;
        while (COUNT_SUCCESS == 0){
            COUNT_SUCCESS = geneticAlgorithm.GetSaccessIndividual(adaptability[1]);
            adaptability = geneticAlgorithm.identificationOfFitness(population);
            adaptability = geneticAlgorithm.quicksort(adaptability, 0, adaptability[0].length-1);
            adaptability = geneticAlgorithm.quicksort(adaptability, 0, adaptability[0].length-1);
            parentPopulation = geneticAlgorithm.getParents(adaptability[0],population);
            childrenPopulation = geneticAlgorithm.crossParent(parentPopulation);
            childrenPopulation = geneticAlgorithm.Mutation(childrenPopulation,true);
            population = geneticAlgorithm.combiningPopulation(parentPopulation, childrenPopulation);
            if (geneticAlgorithm.triggerSaccessIndividual(adaptability[1]) > 40){
                population = geneticAlgorithm.Mutation(population, false);
            }
            sleep(50);
            h.sendEmptyMessage(999);
        }
        h.sendEmptyMessage(100);
        for (int i = 0; i < 60; i++){
            successMelody[i] = population[i][adaptability[0][99]];
        }
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("HandlerLeak")
    private void updateStatus() {
        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                // обновляем TextView
                final TextView tvStatus = findViewById(R.id.tvStatus);
                final Button but = findViewById(R.id.btnMakeMusic);
                but.setVisibility(View.GONE);
                tvStatus.setVisibility(View.VISIBLE);
                String mes = "";
                switch (msg.what) {
                    case (0):
                        mes = "Пора почувствовать себя богом и создать жизнь";
                        break;
                    case (10):
                        mes = "Рождаем на свет множество замечательных мелодий";
                        break;
                    case (20):
                        mes = "Смотрим на них. Кхм... некоторые выглядят не приспособленными к этому жестокому миру";
                        break;
                    case (25):
                        mes = "Мы решили коварно избавится от слабых особей. Мы сделаем это быстро и безболезненно.";
                        break;
                    case (30):
                        mes = "У нас осталась половина популяции. Остальная уже не с нами...";
                        break;
                    case (40):
                        mes = "Мы только отвернулись, а у них уже появились дети...";
                        break;
                    case (50):
                        mes = "Кажется дети мутировали, интересно, что из этого получится";
                        break;
                    case (99):
                        mes = "Мы решили дать им время. Проследим как они будут развиваться";
                        break;
                    case (100):
                        mes = "Ухты! Кажется мы готовы представить вам творение искусства";
                        break;
                    case (999):
                        status += 0.000001;
                        break;
                }
                if (msg.what != 999){
                    tvStatus.setText("Выполнение: " + msg.what + "% \n" + mes);
                }else{
                    tvStatus.setText("Выполнение: " + status + "% \n" + "Мы решили дать им время. Проследим как они будут развиваться");
                }

                if (msg.what == 100){
                    Button play = findViewById(R.id.btnPlayMusic);
                    play.setVisibility(View.VISIBLE);
                }

            }

            ;
        };
    }

    public void playMusic(View view) {

        config = midiDriver.config();
        Log.d(this.getClass().getName(), "maxVoices: " + config[0]);
        Log.d(this.getClass().getName(), "numChannels: " + config[1]);
        Log.d(this.getClass().getName(), "sampleRate: " + config[2]);
        Log.d(this.getClass().getName(), "mixBufferSize: " + config[3]);
        for (int i = 0; i < 60; i += 3){
            playNote(successMelody[i], successMelody[i+1],successMelody[i+2]);
        }
        Button restart = findViewById(R.id.GoBack);
        restart.setVisibility(View.VISIBLE);
    }

    private void playNote(int note, int velocity, int time) {

        // Construct a note ON message for the middle C at maximum velocity on channel 1:
        event = new byte[3];
        event[0] = (byte) (0x90 | 0x00);  // 0x90 = note On, 0x00 = channel 1
        event[1] = (byte) note;  // 0x3C = middle C
        event[2] = (byte) velocity;  // 0x7F = the maximum velocity (127)



        // Internally this just calls write() and can be considered obsoleted:
        //midiDriver.queueEvent(event);

        // Send the MIDI event to the synthesizer.
        midiDriver.write(event);
        sleep(time);
        stopNote(note, velocity);
    }

    private void stopNote(int note, int velocity) {

        // Construct a note OFF message for the middle C at minimum velocity on channel 1:
        event = new byte[3];
        event[0] = (byte) (0x80 | 0x00);  // 0x80 = note Off, 0x00 = channel 1
        event[1] = (byte) note;  // 0x3C = middle C
        event[2] = (byte) velocity;  // 0x00 = the minimum velocity (0)

        // Send the MIDI event to the synthesizer.
        midiDriver.write(event);

    }

    @Override
    public void onMidiStart() {
        Log.d(this.getClass().getName(), "onMidiStart()");
    }


    @Override
    public void onBackPressed() {

    }

    public void Restart(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
