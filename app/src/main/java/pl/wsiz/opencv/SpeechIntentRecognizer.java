package pl.wsiz.opencv;

import java.util.ArrayList;

/**
 * Created by jakub on 26.11.2019.
 */

class SpeechIntentRecognizer {

    int getIntentFromResult(ArrayList<String> results) {
        for (String str : results) {
            if (getIntNumberFromText(str) != -1) {
                return getIntNumberFromText(str);
            }
        }
        return -1;
    }

    private int getIntNumberFromText(String strNum) {
        switch (strNum) {
            case "Take photo":
                return 0;
            case "take photo":
                return 0;
            case "Zrób zdjęcie":
                return 0;
            case "zrób zdjęcie":
                return 0;
        }
        return -1;
    }

}
