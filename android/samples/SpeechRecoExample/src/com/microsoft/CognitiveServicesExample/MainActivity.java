/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license.
 * //
 * Project Oxford: http://ProjectOxford.ai
 * //
 * ProjectOxford SDK GitHub:
 * https://github.com/Microsoft/ProjectOxford-ClientSDK
 * //
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 * //
 * MIT License:
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * //
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * //
 * THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.microsoft.CognitiveServicesExample;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.microsoft.bing.speech.SpeechClientStatus;
import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionResult;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionStatus;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionMode;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;


public class MainActivity extends Activity implements ISpeechRecognitionServerEvents, TextToSpeech.OnInitListener
{
    int m_waitSeconds = 0;
    MicrophoneRecognitionClient micClient = null;
    FinalResponseStatus isReceivedResponse = FinalResponseStatus.NotReceived;
    TextView _logText;
    Button _startButton;
    TextToSpeech tts;

    private CommandAnalyser analyzer;

    @Override
    public void onInit(int status) {

        //tts = new TextToSpeech(this, this);
        if(status == TextToSpeech.SUCCESS){
            //tts.setLanguage(Locale.GERMANY);
        }
        analyzer = CommandAnalyser.getCommandAnalyser(this);

    }


    public enum FinalResponseStatus { NotReceived, OK, Timeout }

    /**
     * Gets the primary subscription key
     */
    public String getPrimaryKey() {
        return this.getString(R.string.primaryKey);
    }

    /**
     * Gets the LUIS application identifier.
     * @return The LUIS application identifier.
     */
    private String getLuisAppId() {
        return this.getString(R.string.luisAppID);
    }

    /**
     * Gets the LUIS subscription identifier.
     * @return The LUIS subscription identifier.
     */
    private String getLuisSubscriptionID() {
        return this.getString(R.string.luisSubscriptionID);
    }

    /**
     * Gets a value indicating whether or not to use the microphone.
     * @return true to use microphone
     */
    private Boolean getUseMicrophone() {
        //int id = this._radioGroup.getCheckedRadioButtonId();
        //return id == R.id.micIntentRadioButton ||
        //        id == R.id.micDictationRadioButton ||
        //        id == (R.id.micRadioButton - 1);
        return true;
    }

    /**
     * Gets a value indicating whether LUIS results are desired.
     * @return true if LUIS results are to be returned otherwise, false.
     */
    private Boolean getWantIntent() {
        //int id = this._radioGroup.getCheckedRadioButtonId();
        //return id == R.id.dataShortIntentRadioButton ||
        //        id == R.id.micIntentRadioButton;
        return true;
    }

    /**
     * Gets the current speech recognition mode.
     * @return The speech recognition mode.
     */
    private SpeechRecognitionMode getMode() {
        return SpeechRecognitionMode.LongDictation;
        /*int id = this._radioGroup.getCheckedRadioButtonId();
        if (id == R.id.micDictationRadioButton ||
                id == R.id.dataLongRadioButton) {
            return SpeechRecognitionMode.LongDictation;
        }

        return SpeechRecognitionMode.ShortPhrase;*/
    }

    /**
     * Gets the default locale.
     * @return The default locale.
     */
    private String getDefaultLocale() {
        return "de-DE";
    }


    public Synthesizer m_syn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(this, this);
        analyzer = CommandAnalyser.getCommandAnalyser(this);
        this._logText = (TextView) findViewById(R.id.textViewLog);
        this._startButton = (Button) findViewById(R.id.button1);

        if (getString(R.string.primaryKey).startsWith("Please")) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.add_subscription_key_tip_title))
                    .setMessage(getString(R.string.add_subscription_key_tip))
                    .setCancelable(false)
                    .show();
        }

        // setup the buttons
        final MainActivity This = this;
        this._startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                This.StartButton_Click(arg0);
            }
        });


        if (m_syn == null) {
            // Create Text To Speech Synthesizer.
            System.out.println("Synthesizer");
            m_syn = new Synthesizer(getLuisAppId());
        }

        m_syn.SetServiceStrategy(Synthesizer.ServiceStrategy.AlwaysService);

        Voice v = new Voice("de-DE", "Microsoft Server Speech Text to Speech Voice (en-US, ZiraRUS)", Voice.Gender.Female, true);
        //Voice v = new Voice("zh-CN", "Microsoft Server Speech Text to Speech Voice (zh-CN, HuihuiRUS)", Voice.Gender.Female, true);
        m_syn.SetVoice(v, null);


    }

    public void SpeakText(String Text)
    {
        m_syn.SpeakToAudio(Text);
    }

    /**
     * Handles the Click event of the _startButton control.
     */
    private void StartButton_Click(View arg0) {
        this._startButton.setEnabled(false);

        this.m_waitSeconds = this.getMode() == SpeechRecognitionMode.ShortPhrase ? 20 : 200;


        this.LogRecognitionStart();

        System.out.println("starting recording");

            if (this.micClient == null) {
                this.clearInfoText();
                this.WriteLine("--- Creating microphone client ----");

                this.micClient =
                        SpeechRecognitionServiceFactory.createMicrophoneClientWithIntent(
                                this,
                                this.getDefaultLocale(),
                                this,
                                this.getPrimaryKey(),
                                this.getLuisAppId(),
                                this.getLuisSubscriptionID());
                System.out.println("starting micClient without Intend recognition");
            }

            this.micClient.startMicAndRecognition();

    }

    /**
     * Logs the recognition start.
     */
    private void LogRecognitionStart() {
        clearInfoText();
        this.WriteLine("\n--- Start speech recognition using microphone with " + this.getMode() + " mode in " + this.getDefaultLocale() + " language ----\n\n");
    }


    public void onFinalResponseReceived(final RecognitionResult response) {
        boolean isFinalDicationMessage = this.getMode() == SpeechRecognitionMode.LongDictation &&
                (response.RecognitionStatus == RecognitionStatus.EndOfDictation ||
                        response.RecognitionStatus == RecognitionStatus.DictationEndSilenceTimeout);
        if (null != this.micClient && this.getUseMicrophone() && ((this.getMode() == SpeechRecognitionMode.ShortPhrase) || isFinalDicationMessage)) {
            // we got the final result, so it we can end the mic reco.  No need to do this
            // for dataReco, since we already called endAudio() on it as soon as we were done
            // sending all the data.
            this.micClient.endMicAndRecognition();
        }

        if (isFinalDicationMessage) {
            this._startButton.setEnabled(true);
            this.isReceivedResponse = FinalResponseStatus.OK;
        }
    }

    /**
     * Called when a final response is received and its Intent is parsed
     */
    public void onIntentReceived(final String payload) {
        analyzer.analyseResult(payload);

    }

    public void onPartialResponseReceived(final String response) {
        // ignore
    }

    public void onError(final int errorCode, final String response) {
        this._startButton.setEnabled(true);
        clearInfoText();
        this.WriteLine("--- Error received by onError() ---");
        this.WriteLine("Error code: " + SpeechClientStatus.fromInt(errorCode) + " " + errorCode);
        this.WriteLine("Error text: " + response);
    }

    /**
     * Called when the microphone status has changed.
     * @param recording The current recording state
     */
    public void onAudioEvent(boolean recording) {
        clearInfoText();
        if (recording) {
            //tts.speak("Ich höre!", TextToSpeech.QUEUE_FLUSH, null);
            this.WriteLine("Please start speaking.");
            //SpeakText("Sprachsteuerung aktiv");
        }

        if (!recording) {
            this.micClient.endMicAndRecognition();
            this._startButton.setEnabled(true);
        }
    }

    /**
     * Writes the line.
     */
    public void clearInfoText() {
        this._logText.setText("");
    }

    /**
     * Writes the line.
     * @param text The line to write.
     */
    public void WriteLine(String text) {
        this._logText.append(text + "\n");
    }

    public void updateCurrentSelectedRobot(String robot){
        TextView currentRobotView = (TextView) findViewById(R.id.textViewCurrentRobot);
        currentRobotView.setText(robot);
    }


}
