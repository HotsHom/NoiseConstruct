package com.example.noiseconstruct.module;

import static com.example.noiseconstruct.module.NotesList.Notes;
import static java.lang.Math.random;

public class GeneticAlgorithm {

    private Float[] pitches;
    private Integer[] amplitudes;
    private int AVERAGE = 0;
    private int[] DURATION_ARRAY = new int[100];

    public GeneticAlgorithm(Float[] _pitches, Integer[] _amplitudes){
        pitches = _pitches;
        amplitudes = _amplitudes;
    }

    public int[][] createPopulation(){
        int[][] populations = new int[60][100];

        int countGeneratedIndividual;

        for (int i = 0; i < 100; i++){
            countGeneratedIndividual = 0;
            int DURATION = 0;
            for (int j = 0; j < 60; ){

                int summDuration = 20000;
                populations[j][i] = generateNote(pitches[countGeneratedIndividual]); //Нота
                populations[j+1][i] = generatePower(amplitudes[countGeneratedIndividual]); //Громкость
                populations[j+2][i] = generateDuration();
                DURATION += populations[j+2][i];

                countGeneratedIndividual++;
                j += 3;
            }
            //евклидов
            int[] ArrayDuration = generateEvcklid(20, (int)DURATION/250);
            for (int j = 2; j < 60; ) {
                DURATION += populations[j][i];
                j += 3;
            }

        }
        getAverage(populations);
        return populations;
    }

    private int[] generateEvcklid(int i, int duration) {
        int[] durrations = new int[duration];

        String binar = "";
        int ost = -1;
        while(ost != 0){
            ost = duration % i;
        }

        return durrations;
    }

    private int generateDuration(){
        int duration = 1;
        while (duration % 250 != 0) {
            duration = (int) (random() * 2000 + 1);
        }
        return duration;
    }

    private int generatePower(int amplitude) {
        float relativeValue = (float)Math.abs(amplitude) / (float)110;
        int scaledValue = (int)(10 + (127 - 10) * relativeValue);


        int randomDeviation = (int) (random() * 3);
        randomDeviation -= 3;
        if (scaledValue > 3){
            scaledValue += randomDeviation;
        }
        return scaledValue;
    }

    private int generateNote(float pitch) {
        int nearest = -1;
        float bestDistanceFoundYet = Integer.MAX_VALUE;

        for (int i = 0; i < Notes.length; i++) {
            // if we found the desired number, we return it.
            if (Notes[i] == pitch) {
                return i;
            } else {
                float d = (float) Math.abs(pitch - Notes[i]);
                if (d < bestDistanceFoundYet) {
                    bestDistanceFoundYet = d;
                    nearest = i;
                }
            }
        }
        int randomDeviation = (int) (random() * 3);
        randomDeviation -= 3;
        if (nearest > randomDeviation){
            nearest += randomDeviation;
        }
        nearest += 12;
        return nearest;
    }

    public int[][] identificationOfFitness(int[][] population) {
        int[][] adaptability = new int[2][100];

        //Переменные
        int AVERAGE_VOLUME = 0;
        int RATING_DISSON = 0;
        int RATING_REPLAY = 0;

        //Вычисление

        AVERAGE_VOLUME = AVERAGE;

        for (int i = 0; i < 100; i++){
            adaptability[0][i] += i;
            for (int j = 1; j < 60; ){
                if (population[j][i] >= AVERAGE_VOLUME){
                    adaptability[1][i] += 1;
                }
                j += 3;
            }
        }

        int COUNT_DISS;
        for (int i = 0; i < 100; i++){
            COUNT_DISS = 0;
            for (int j = 3; j < 60;){
                int result = Math.abs(population[j][i] - population[j-3][i]);
                if (result == 1 || result == 2){
                    COUNT_DISS += 1;
                }
                int result_two = 0;
                int check = 13;
                while(population[j][i] - check >= 36 || population[j][i] + check <= 78){
                    if (Math.abs(population[j][i] - population[j-3][i]) == check || Math.abs(population[j][i] - population[j-3][i]) == check+1 || Math.abs(population[j][i] - population[j-3][i]) == check-1){
                        COUNT_DISS += 1;
                    }
                    check *= 2;
                }
                j += 3;
            }
            RATING_DISSON = 20 - COUNT_DISS;
            adaptability[1][i] += RATING_DISSON;
        }


        int COUNT_REPLAY;
        int Note;
        for (int i = 0; i < 100; i++){
            COUNT_REPLAY = 0;
            for (int j = 0; j < 57;){
                if (population[j][i] == population[j+3][i]){
                    COUNT_REPLAY += 1;
                }
                j += 3;
            }
            RATING_REPLAY = 20 - COUNT_REPLAY;
            adaptability[1][i] += RATING_REPLAY;
        }

        for (int i = 0; i < 100; i++){
            int RATING_CULMINATION = 0;
            for (int j = 45; j < 60;){

                if (population[j][i] > population[j-3][i] && j == 45){
                    RATING_CULMINATION++;
                }
                if (population[j][i] >= population[j-3][i] && j > 45 && j < 54){
                    RATING_CULMINATION++;
                }
                if (population[j][i] < population[j-3][i] && j >= 54){
                    RATING_CULMINATION++;
                }else{
                    RATING_CULMINATION--;
                }

                j += 3;
            }
            adaptability[1][i] += RATING_CULMINATION;
        }

//        for (int i = 0; i < 100; i++){
//            int LOW_RITM = 0;
//            for (int j = 2; j < 60;){
//                if (population[j][i] < 250){
//                    adaptability[1][i]--;
//                }
//                j += 3;
//            }
//        }

        for (int i = 0; i < 100; i++){
            int COUNT_LOW_HIGHT_NOTE = 0;
            for (int j = 0; j < 60;){
                if (population[j][i] < 60 || population[j][i] > 78){
                    COUNT_LOW_HIGHT_NOTE++;
                }
                j += 3;
            }
            if (COUNT_LOW_HIGHT_NOTE > 0){
                adaptability[1][i] -= COUNT_LOW_HIGHT_NOTE;
            }
        }

        for (int i = 0; i < 100; i++){
            int COUNT_DIFFERENCE = 0;
            for (int j = 3; j < 60;){
                if (Math.abs(population[j][i] - population[j-3][i]) > 4){
                    COUNT_DIFFERENCE++;
                }
                j += 3;
            }
            adaptability[1][i] -= COUNT_DIFFERENCE;
        }

        int count = 0;
        for (int i = 0; i < 100; i++){
            int COUNT_DURATION = 0;
            for (int j = 2; j < 60;){
                    COUNT_DURATION += population[j][i];
                j += 3;
            }
            if (COUNT_DURATION > 9000 || COUNT_DURATION < 5500){
                adaptability[1][i] -= 10;
            }
            DURATION_ARRAY[count] = COUNT_DURATION;
            count++;
        }


        return adaptability;
    }

    public int[][] quicksort(int[][] arrayIn, int start, int end) {
        int[][] cloneArray = arrayIn;
        //массив, который будем сортировать
        int[] array = cloneArray[1];

        if (start < end) {
            int dp = partition(cloneArray, start, end);
            quicksort(cloneArray, start, dp-1);
            quicksort(cloneArray, dp+1, end);
        }

        return cloneArray;
    }

    private int partition(int[][] numbers, int low, int high) {
        int pivot = numbers[1][low];
        int i = low;
        for (int j = low + 1; j <= high; j++)
            if (numbers[1][j] < pivot) {
                ++i;
                swap(numbers, i, j);
            }
        //end for
        swap(numbers, low, i);
        return i;
    }

    private void swap(int[][] _list, int i, int j) {
        int temp = _list[1][i];
        _list[1][i] = _list[1][j];
        _list[1][j] = temp;
        temp = _list[0][i];
        _list[0][i] = _list[0][j];
        _list[0][j] = temp;
    }

    private int getAverage(int[][] population) {
        int SUM = 0;
        AVERAGE = 0;
        int count = 0;

        for (int i = 0; i < 100; i++){
            for (int j = 1; j < 60;){
                SUM += population[j][i];
                count++;
                j += 3;
            }
        }

        AVERAGE = SUM / count;
        return AVERAGE + 20;
    }


    public int[][] getParents(int[] fitnes, int[][] population) {
        int[][] parents = new int[60][50];


        int i = 0;
        for (int k = 99; k > 73; k--){
            for (int j = 0; j < 60; j++){
                parents[j][i] = population[j][fitnes[k]];
            }
            i++;
        }

        int countRandom = (int) (random() * 100);

        if (countRandom < 20){
            for (int k = 24; k > 0; k--){
                for (int j = 0; j < 60; j++){
                    parents[j][i] = population[j][fitnes[k]];
                }
                i++;
            }
        }else{
            for (int k = 70; k > 46; k--){
                for (int j = 0; j < 60; j++){
                    parents[j][i] = population[j][fitnes[k]];
                }
                i++;
            }
        }



        return parents;
    }

    public int[][] crossParent(int[][] parentPopulation) {
        int[][] children = new int[60][50];

        int cChild = 0;
        for (int i = 0; i < 50;){
            int flag = 0;
            int y = (int) (random() * 7) + 1;
            for (int j = 0; j < 60; j++){
                if (flag < y){
                    children[j][cChild] = parentPopulation[j][i];
                    children[j][cChild+1] = parentPopulation[j][i+1];
                    flag++;
                }else{
                    children[j][cChild] = parentPopulation[j][i+1];
                    children[j][cChild+1] = parentPopulation[j][i];
                    flag++;
                    if (flag == y*2){
                        flag = 0;
                    }
                }

//                if (j < 30){
//                    children[j][cChild] = parentPopulation[j][i];
//                    children[j][cChild+1] = parentPopulation[j][i+1];
//                }else{
//                    children[j][cChild] = parentPopulation[j][i+1];
//                    children[j][cChild+1] = parentPopulation[j][i];
//                }
            }
            cChild += 2;
            i += 2;
        }

        return children;
    }

    public int[][] Mutation(int[][] childrenPopulation, boolean flag) {
        int[][] mutatedPopukation = childrenPopulation;


        if (flag){
            int countRandomMutation;
            int countMutated;

            for (int i = 0; i < childrenPopulation[0].length; i++){
                countMutated = 0;
                for (int j = 0; j < 60; j++){
                    if (j % 3 == 0){
                        countRandomMutation = (int) (random() * 20);
                        if (countRandomMutation > 5){
                            int change = (int) (random()*2);
                            int y = (int) (random() * 30);
                            if (y > 50 && mutatedPopukation[j][i] - change >= 36){
                                change = -change;
                                mutatedPopukation[j][i] += change;
                                countMutated++;
                            }else if(y > 50 && mutatedPopukation[j][i] + change <= 78){
                                mutatedPopukation[j][i] += change;
                                countMutated++;
                            }

                        }
                    }else{
                        countRandomMutation = (int) (random() * 50);
                        if (countRandomMutation > 30){
                            int change = (int) (random()*10);
                            int y = (int) (random() * 30);
                            if (y > 27){
                                change = -change;
                            }
                            if (AVERAGE + 20 > change + mutatedPopukation[j][i] && AVERAGE  < change + mutatedPopukation[j][i] && (j - 1) % 3 == 0){
                                mutatedPopukation[j][i] += change;
                            }
                            y = (int) (random() * 30);
                            if((j - 2) % 3 == 0 && mutatedPopukation[j][i] + change < 250 && y > 15){
                                mutatedPopukation[j][i] += Math.abs(change);
                            }
                            if((j - 2) % 3 == 0 && mutatedPopukation[j][i] + change > 800 && y > 15){
                                mutatedPopukation[j][i] -=  Math.abs(change);
                            }
                            countMutated++;
                        }
                    }

                    if (countMutated >= 10){
                        break;
                    }
                }
            }
        }else{
            for (int i = 0; i < childrenPopulation[0].length; i++) {
                for (int j = 0; j < 60; j++) {
                    int change = (int) (random() * 20);
                    int y = (int) (random() * 100);
                    if (y % 2 == 0){
                        change = -change;
                    }
                    if (j > 38 && y > 10 && j % 3 == 0){
                        if (mutatedPopukation[j][i] - change >= 36){
                            change = -change;
                            mutatedPopukation[j][i] += change;
                        }else if(mutatedPopukation[j][i] + change <= 78){
                            mutatedPopukation[j][i] += change;
                        }
                    }else if(y > 50 && j % 3 == 0){
                        if (mutatedPopukation[j][i] - change >= 36){
                            change = -change;
                            mutatedPopukation[j][i] += change;
                        }else if(mutatedPopukation[j][i] + change <= 78){
                            mutatedPopukation[j][i] += change;
                        }
                    }
                    if (AVERAGE + 20 > change + mutatedPopukation[j][i] && AVERAGE  < change + mutatedPopukation[j][i] && (j - 1) % 3 == 0){
                        mutatedPopukation[j][i] += change;
                    }
                    if((j - 2) % 3 == 0 && DURATION_ARRAY[i] < 5500){
                        mutatedPopukation[j][i] += Math.abs(change);
                    }
                    if((j - 2) % 3 == 0 && DURATION_ARRAY[i] > 9000){
                        mutatedPopukation[j][i] -= Math.abs(change);
                    }
                    if (AVERAGE  >= mutatedPopukation[j][i] && (j - 1) % 3 == 0){
                        change = (int) (random() * 30);
                        mutatedPopukation[j][i] += change;
                    }
                }
            }
        }


        return mutatedPopukation;
    }

    public int[][] combiningPopulation(int[][] parentPopulation, int[][] childrenPopulation) {
        int[][] population = new int[60][100];

        for (int i = 0; i < 100; i++){
            for (int j = 0; j < 60; j++){
                if (i < 50){
                    population[j][i] = parentPopulation[j][i];
                }else {
                    population[j][i] = childrenPopulation[j][i-50];
                }
            }
        }
        return population;
    }

    public int GetSaccessIndividual(int[] fitnes){
        int countIndividual = 0;

        for (int k = 99; k > 49; k--){
            if (fitnes[k] >= 61){
                countIndividual++;
            }
        }

        return countIndividual;
    }

    public int triggerSaccessIndividual(int[] fitnes){
        int count = 0;

        for (int k = 98; k > 49; k--){
            if (fitnes[k] == fitnes[99]){
                count++;
            }
        }

        return count;
    }
}
