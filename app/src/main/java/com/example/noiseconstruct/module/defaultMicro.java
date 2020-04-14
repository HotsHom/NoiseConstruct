package com.example.noiseconstruct.module;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.TarsosDSPAudioInputStream;
import be.tarsos.dsp.io.android.AndroidAudioInputStream;

public class defaultMicro {
    private AudioRecord audioInputStream;

    public AudioDispatcher fromDefaultMicrophone(final int sampleRate,
                                                        final int audioBufferSize, final int bufferOverlap) {
        int minAudioBufferSize = AudioRecord.getMinBufferSize(sampleRate,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);
        int minAudioBufferSizeInSamples =  minAudioBufferSize/2;
        if(minAudioBufferSizeInSamples <= audioBufferSize ){
            audioInputStream = new AudioRecord(
                    MediaRecorder.AudioSource.VOICE_COMMUNICATION, sampleRate,
                    android.media.AudioFormat.CHANNEL_IN_STEREO,
                    android.media.AudioFormat.ENCODING_PCM_16BIT,
                    audioBufferSize * 2);

            TarsosDSPAudioFormat format = new TarsosDSPAudioFormat(sampleRate, 16,2, true, false);

            TarsosDSPAudioInputStream audioStream = new AndroidAudioInputStream(audioInputStream, format);
            //start recording ! Opens the stream.
            audioInputStream.startRecording();
            return new AudioDispatcher(audioStream,audioBufferSize,bufferOverlap);
        }else{
            throw new IllegalArgumentException("Buffer size too small should be at least " + (minAudioBufferSize *2));
        }
    }

    public void stopRecord(){
        audioInputStream.stop();
    }
}
