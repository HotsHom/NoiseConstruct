package com.example.noiseconstruct;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.billthefarmer.mididriver.MidiConstants;
import org.billthefarmer.mididriver.GeneralMidiConstants;
import org.billthefarmer.mididriver.BuildConfig;

import androidx.appcompat.app.AppCompatActivity;

import com.example.noiseconstruct.module.GeneticAlgorithm;

import org.billthefarmer.mididriver.MidiDriver;

import java.util.ArrayList;

import static org.billthefarmer.mididriver.GeneralMidiConstants.*;
import static org.billthefarmer.mididriver.MidiConstants.NOTE_OFF;
import static org.billthefarmer.mididriver.MidiConstants.NOTE_ON;
import static org.billthefarmer.mididriver.MidiConstants.PROGRAM_CHANGE;

public class CheckActivity extends AppCompatActivity implements MidiDriver.OnMidiStartListener {

    private ArrayList<Float> pitches;
    private ArrayList<Integer> amplitudes;
    private int[][] population;
    private int[][] population_backup;
    private int[][] adaptability;
    private int[][] parentPopulation;
    private int[][] childrenPopulation;
    private GeneticAlgorithm geneticAlgorithm;
    private double status = 99.000001;
    private Handler h;
    private int[] successMelody = new int[60];
    private Thread t;
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
    }

    @SuppressLint("SetTextI18n")
    public void makeMusic(View view) throws InterruptedException {
        updateStatus();
        t = new Thread(new Runnable() {
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
        population_backup = population;
        h.sendEmptyMessage(10);
        sleep(3000);
        adaptability = geneticAlgorithm.identificationOfFitness(population);
        h.sendEmptyMessage(20);
        sleep(3000);
        adaptability = geneticAlgorithm.quicksort(adaptability, 0, adaptability[0].length - 1);
        h.sendEmptyMessage(22);
        adaptability = geneticAlgorithm.quicksort(adaptability, 0, adaptability[0].length - 1);
        h.sendEmptyMessage(25);
        sleep(3000);
        parentPopulation = geneticAlgorithm.getParents(adaptability[0], population);
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

    private void basicGenAlgorithm() {
        int COUNT_SUCCESS = 0;
        while (COUNT_SUCCESS == 0) {
            COUNT_SUCCESS = geneticAlgorithm.GetSaccessIndividual(adaptability[1]);
            adaptability = geneticAlgorithm.identificationOfFitness(population);
            adaptability = geneticAlgorithm.quicksort(adaptability, 0, adaptability[0].length - 1);
            adaptability = geneticAlgorithm.quicksort(adaptability, 0, adaptability[0].length - 1);
            parentPopulation = geneticAlgorithm.getParents(adaptability[0], population);
            childrenPopulation = geneticAlgorithm.crossParent(parentPopulation);
            childrenPopulation = geneticAlgorithm.Mutation(childrenPopulation, true);
            population = geneticAlgorithm.combiningPopulation(parentPopulation, childrenPopulation);
            if (geneticAlgorithm.triggerSaccessIndividual(adaptability[1]) > 40) {
                population = geneticAlgorithm.Mutation(population, false);
            }
            h.sendEmptyMessage(999);
        }
        h.sendEmptyMessage(100);
        for (int i = 0; i < 60; i++) {
            successMelody[i] = population[i][adaptability[0][99]];
        }
        t.interrupt();
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
                        status += (double) 0.000001;
                        break;
                }
                if (msg.what != 999) {
                    tvStatus.setText("Выполнение: " + msg.what + "% \n" + mes);
                } else {
                    tvStatus.setText("Выполнение: " + status + "% \n" + "Мы решили дать им время. Проследим как они будут развиваться");
                }

                if (msg.what == 100) {
                    Button play = findViewById(R.id.btnPlayMusic);
                    play.setVisibility(View.VISIBLE);
                }

            }

            ;
        };
    }

    public void playMusic(View view) {
        midiDriver.start();
        config = midiDriver.config();
        Log.d(this.getClass().getName(), "maxVoices: " + config[0]);
        Log.d(this.getClass().getName(), "numChannels: " + config[1]);
        Log.d(this.getClass().getName(), "sampleRate: " + config[2]);
        Log.d(this.getClass().getName(), "mixBufferSize: " + config[3]);
        programChange(0, BRIGHT_ACOUSTIC_PIANO );
        programChange(1, ACOUSTIC_BASS);
        for (int i = 0; i < 60; i += 3) {
            Play(0, successMelody[i], successMelody[i + 1]);
            if (i % 12 == 0){
                Play(1, successMelody[i]-12, successMelody[i + 1]+20);
            }
            if (successMelody[i + 2] < 150){
                sleep(successMelody[i + 2] + 250);
            }else{
                sleep(successMelody[i + 2]);
            }

            StopPlay(0, successMelody[i], successMelody[i + 1]);
        }
        Button restart = findViewById(R.id.GoBack);
        restart.setVisibility(View.VISIBLE);
        sleep(250);
        midiDriver.stop();
    }

    void programChange(int c, int n)
    {
        sendMidiInstrument(PROGRAM_CHANGE + c, n);
    }

    private void sendMidiInstrument(int m, int n) {
        byte msg[] = new byte[2];

        msg[0] = (byte) m;
        msg[1] = (byte) n;

        midiDriver.write(msg);
    }

    void Play(int c, int n, int v)
    {
        noteOn(c, n, v);
    }

    void StopPlay(int c, int n, int v)
    {
        noteOff(c, n, v);
    }

    // Note on
    void noteOn(int c, int n, int v)
    {
        sendMidi(NOTE_ON + c, n, v);
    }

    void noteOff(int c, int n, int v)
    {
        sendMidi(NOTE_OFF + c, n, v);
    }

    // Send a midi message, 3 bytes
    void sendMidi(int m, int n, int v)
    {
        byte msg[] = new byte[3];

        msg[0] = (byte) m;
        msg[1] = (byte) n;
        msg[2] = (byte) v;

        midiDriver.write(msg);
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

    public void Restart(View view) throws InterruptedException {
        population = population_backup;
        Button b1 = findViewById(R.id.btnPlayMusic);
        b1.setVisibility(View.GONE);
        Button b2 = findViewById(R.id.GoBack);
        b2.setVisibility(View.GONE);
        Button b3 = findViewById(R.id.btnMakeMusic);
        b3.setVisibility(View.VISIBLE);
        status = 99.000001;
        makeMusic(view);
    }
}
