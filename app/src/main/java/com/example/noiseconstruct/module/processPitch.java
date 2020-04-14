package com.example.noiseconstruct.module;

import android.view.View;
import android.widget.TextView;

import com.example.noiseconstruct.R;

public class processPitch {
    public static void processPitch(float pitchInHz, View view) {
        TextView textMain = view.findViewById(R.id.textMain);

        if(pitchInHz >= 110 && pitchInHz < 123.47) {
            //A
            textMain.setText("A");
        }
        else if(pitchInHz >= 123.47 && pitchInHz < 130.81) {
            //B
            textMain.setText("B");
        }
        else if(pitchInHz >= 130.81 && pitchInHz < 146.83) {
            //C
            textMain.setText("C");
        }
        else if(pitchInHz >= 146.83 && pitchInHz < 164.81) {
            //D
            textMain.setText("D");
        }
        else if(pitchInHz >= 164.81 && pitchInHz <= 174.61) {
            //E
            textMain.setText("E");
        }
        else if(pitchInHz >= 174.61 && pitchInHz < 185) {
            //F
            textMain.setText("F");
        }
        else if(pitchInHz >= 185 && pitchInHz < 196) {
            //G
            textMain.setText("G");
        }else if (pitchInHz != -1){
            String st = String.valueOf(pitchInHz);
            textMain.setText(st);
        }

    }
}
