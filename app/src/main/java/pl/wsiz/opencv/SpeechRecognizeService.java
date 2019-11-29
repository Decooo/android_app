package pl.wsiz.opencv;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by jakub on 29.11.2019.
 */

public class SpeechRecognizeService extends Service implements RecognitionListener {

    private SpeechRecognizer speechRecognizer;
    private Intent intent;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "Active speech recognition", Toast.LENGTH_SHORT).show();
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        speechRecognizer.setRecognitionListener(this);

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);

        speechRecognizer.startListening(intent);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Disabled speech recognition", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d("Speech", "onBeginningOfSpeech");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.d("Speech", "onBufferReceived");
    }

    @Override
    public void onEndOfSpeech() {
        Log.d("Speech", "onEndOfSpeech");
    }

    @Override
    public void onError(int error) {
        Log.d("Speech", "onError");
        speechRecognizer.startListening(intent);
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        Log.d("Speech", "onEvent");
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.d("Speech", "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.d("Speech", "onReadyForSpeech");
    }

    @Override
    public void onResults(Bundle results) {
        Log.d("Speech", "onResults");
        ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        SpeechIntentRecognizer speechIntentRecognizer = new SpeechIntentRecognizer();

        int intFound = speechIntentRecognizer.getIntentFromResult(data);
        if (intFound == 0)
            Toast.makeText(this, "Rozpoznałem!!!", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, "Sorry, I didn't catch that! Please try again", Toast.LENGTH_LONG).show();

        speechRecognizer.startListening(intent);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.d("Rms", "onRmsChanged");
    }
}