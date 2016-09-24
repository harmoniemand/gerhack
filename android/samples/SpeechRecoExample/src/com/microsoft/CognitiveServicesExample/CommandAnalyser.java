package com.microsoft.CognitiveServicesExample;

import android.speech.tts.TextToSpeech;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by alki on 22.09.16.
 *
 * Singleton!
 */
public class CommandAnalyser {

    private static CommandAnalyser analyser = null;



    public static final String AXIS1LEFTRIGHTNAME = "1";
    public static final String AXIS2UPDOWNNAME = "2";
    public static final String AXIS3UPDOWNNAME = "3";
    public static final String AXIS4EXTENDINGNAME = "4";
    public static final String AXIS5LEFTRIGHTNAME = "5";
    public static final String AXIS6TURNAXISNAME = "6";

    public static final String AXIS12SPEECHNAME = "basis";
    public static final String AXIS34SPEECHNAME = "arm";
    public static final String AXIS56SPEECHNAME = "greifer";

    /** the currently selected robot*/
    public static String selectedRobot = "roboter";
    public static MainActivity mainAct = null;


    /** singleton - so this mus be private */
    private CommandAnalyser() { }

    /**
     *
     * @return the only instance of CommandAnalyser
     */
    public static CommandAnalyser getCommandAnalyser(MainActivity mainact){
        if(CommandAnalyser.analyser == null){
            CommandAnalyser.analyser = new CommandAnalyser();
            CommandAnalyser.analyser.mainAct = mainact;
        }
        return CommandAnalyser.analyser;
    }

    /**
     *
     * @param payload JSON formatted String (LUIS result)
     * @return status string for printing on the screen
     */
    public void analyseResult(String payload){

        // TODO: analyse + if needed, send request
        Gson gson = new Gson();
        System.out.println("got JASON string: ");
        System.out.println(payload);
        Query returnedQuery = gson.fromJson(payload, Query.class);

        // get intent with highest score
        Intent highestIntent = null;
        for(Intent myintent : returnedQuery.intents){
            if(highestIntent == null){
                highestIntent = myintent;
            }else if(highestIntent.score < myintent.score){
                highestIntent = myintent;
            }
        }


        if(highestIntent == null){
            System.out.println("highestIntent = null, returning");
            mainAct.SpeakText("Befehl nicht erkannt.");
            return;
        }
        if(highestIntent.actions == null){
            System.out.println("highestIntent.actions = null, returning");
            mainAct.SpeakText("Befehl nicht erkannt.");
            return;
        }

        // find first triggered action (if none is triggered, abort

        IntentAction triggeredAction = null;
        for(IntentAction myAction : highestIntent.actions){
            if(myAction.triggered){
                triggeredAction = myAction;
                break;
            }
        }

        // only react if an action was triggered
        if(triggeredAction != null){

            String postString = null;
            switch (triggeredAction.name){
                case "turnIntend" : {
                    String bodyPart = getEntityNameFromParameter("bodyPart", triggeredAction.parameters);
                    String posIntValue = getEntityNameFromParameter("posIntValue", triggeredAction.parameters);
                    String direction = getEntityNameFromParameter("direction", triggeredAction.parameters);

                    String sign = "";
                    // negate value if needed
                    if(direction.equals("nach links") || direction.equals("runter") || direction.equals("herunter") || direction.equals("vor")){
                        sign= "-";
                    }

                    if(bodyPart == null || posIntValue == null || direction == null || getInternalAxisNameFromSpeechName(bodyPart, direction) == null){
                        // Abort Mission! I repeat: Abort Mission!
                        mainAct.SpeakText("Befehl nicht erkannt.");
                        return;
                    }

                    // create UrlString
                    postString = "/"+selectedRobot+"/"+getInternalAxisNameFromSpeechName(bodyPart, direction)+"/"+sign+posIntValue;

                    System.out.println("turnIntend triggered: " + postString);

                    mainAct.clearInfoText();
                    mainAct.WriteLine("turnIntend triggered: " + postString);

                    break;
                }
                case "linearMoveIntend" : {

                    String bodyPart = getEntityNameFromParameter("bodyPart", triggeredAction.parameters);
                    String posIntValue = getEntityNameFromParameter("posIntValue", triggeredAction.parameters);
                    String direction = getEntityNameFromParameter("direction", triggeredAction.parameters);


                    if(bodyPart == null || posIntValue == null || direction == null || getInternalAxisNameFromSpeechName(bodyPart, direction) == null){
                        // Abort Mission! I repeat: Abort Mission!
                        mainAct.SpeakText("Befehl nicht erkannt.");
                        return;
                    }
                    String sign = "";
                    // negate value if needed
                    if(direction.equals("rein") || direction.equals("herein") || direction.equals("zurück") || direction.equals("ein")){
                        sign= "-";
                    }


                    // create UrlString
                    postString = "/"+selectedRobot+"/"+getInternalAxisNameFromSpeechName(bodyPart, direction)+"/"+sign+posIntValue;

                    System.out.println("linearMoveIntend triggered: " + postString);

                    mainAct.clearInfoText();
                    mainAct.WriteLine("linearMoveIntend triggered: " + postString);
                    break;
                }
                case "startRecordIntend" : {

                    String recordName = getEntityNameFromParameter("recordName", triggeredAction.parameters);
                    if(recordName == null){
                        System.out.println("Robot name is null somehow... did you try to break the system?");
                        mainAct.SpeakText("Befehl nicht erkannt.");
                        return;
                    }

                    try {
                        recordName = URLEncoder.encode(recordName, "UTF-8");
                        // abort if an error occours
                        if(recordName == null){
                            System.out.println("Error starting new record.");
                            mainAct.SpeakText("Fehler beim entstandenen record.");
                            return;
                        }else{
                            System.out.println("Starting record: "+recordName);
                            postString = "/record/" + recordName;

                            mainAct.clearInfoText();
                            mainAct.WriteLine("Starting record: "+postString);

                        }

                    } catch (UnsupportedEncodingException e) {
                        System.out.println("Error encoding new record name. Abort.");
                        mainAct.SpeakText("Unerwarteter Fehler. Befehl nicht erkannt.");
                        e.printStackTrace();
                        // abort if an error occours
                        return;
                    }
                    break;
                }
                case "endRecordIntend" : {
                    postString = "/end";

                    System.out.println("ending record");

                    mainAct.clearInfoText();
                    mainAct.WriteLine("ending record");
                    break;
                }
                case "runRecordIntend" : {
                    String recordName = getEntityNameFromParameter("recordName", triggeredAction.parameters);
                    if(recordName == null){
                        System.out.println("Robot name is null somehow... did you try to break the system?");
                        mainAct.SpeakText("Robotername nicht angegeben. Befehl nicht erkannt.");
                        return;
                    }

                    try {
                        recordName = URLEncoder.encode(recordName, "UTF-8");
                        // abort if an error occours
                        if(recordName == null){
                            System.out.println("Error running record.");
                            mainAct.SpeakText("Befehl nicht erkannt.");
                            return;
                        }else{
                            System.out.println("Running record: "+recordName);
                            postString = "/run/" + selectedRobot + "/" + recordName;

                            mainAct.clearInfoText();
                            mainAct.WriteLine("Running record: "+postString);
                        }

                    } catch (UnsupportedEncodingException e) {
                        System.out.println("Error encoding new record name. Abort.");
                        e.printStackTrace();
                        mainAct.SpeakText("Wie können sie unser encoding nicht unterstützen?");
                        // abort if an error occours
                        return;
                    }
                    break;
                }
                case "selectRobotIntend" : {
                    String robotName = getEntityNameFromParameter("robotName", triggeredAction.parameters);
                    if(robotName == null){
                        System.out.println("Robot name is null somehow... did you try to break the system?");
                        return;
                    }

                    try {
                        String newSelectedRobot = URLEncoder.encode(robotName, "UTF-8");
                        if(newSelectedRobot == null){
                            System.out.println("Error setting new robot. Selected robot is still: "+selectedRobot);
                        }else{
                            selectedRobot = newSelectedRobot;
                            System.out.println("Selected robot is now: "+selectedRobot);

                            mainAct.updateCurrentSelectedRobot(selectedRobot);
                            mainAct.clearInfoText();
                            mainAct.WriteLine("Selected robot is now: "+selectedRobot);
                        }

                    } catch (UnsupportedEncodingException e) {
                        System.out.println("Error encoding robot name. Abort.");
                        mainAct.SpeakText("Fehler beim Encoden vom Robotername.");
                        e.printStackTrace();
                        return;
                    }
                    // no need to send something
                    return;
                }

            }


            if(postString != null){
                mainAct.SpeakText( returnedQuery.query + " wird ausgeführt.");


                sendRequest(postString);
            }else{
                mainAct.WriteLine("Befehl nicht erkannt:");
            }
        } else{
            System.out.println("no action triggered");
            mainAct.SpeakText("Befehl nicht erkannt.");
            mainAct.clearInfoText();
            mainAct.WriteLine("Befehl nicht erkannt:");
            mainAct.WriteLine(returnedQuery.query);
        }

    }

    /**
     * sends requests to the service
     * @param postString
     */
    private void sendRequest(String postString){
        // TODO send request


        AsyncHttpClient httpClient = new AsyncHttpClient();
        System.out.println("URL part string: " + postString);
        String urlString = "http://13.93.48.187:8080"+postString;
        System.out.println("final URL string: " +urlString);




        httpClient.post(urlString, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println("Sent data successful");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                System.out.println("Sent data FAILED");
            }
        });


    }

    // return entity name for a given parameterName
    private String getEntityNameFromParameter(String parameterName, List<ActionParameter> parameters) {

        for(ActionParameter parameter : parameters){
            if(parameter.name.equals(parameterName)){
                if((parameter.value != null) && (parameter.value.size() > 0)) {
                    return parameter.value.get(0).entity;
                }
            }
        }
        return "";
    }



    public String getInternalAxisNameFromSpeechName(String speechName, String speechDirection){
        speechName = speechName.toLowerCase();


        if(speechName.equals(AXIS12SPEECHNAME) || speechName.equals("bacis") || speechName.equals("basics") ) {
                if(speechDirection.equals("nach links") || speechDirection.equals("nach rechts") || speechDirection.equals("links") || speechDirection.equals("rechts")){
                    return AXIS1LEFTRIGHTNAME;
                }else if(speechDirection.equals("nach vorn") || speechDirection.equals("nach hinten") || speechDirection.equals("vor") || speechDirection.equals("zurück")){
                    return  AXIS2UPDOWNNAME;
                }
        }else if( speechName.equals(AXIS34SPEECHNAME) || speechName.equals("am") || speechName.equals("an") || speechName.equals("alarm") ) {
                if(speechDirection.equals("rauf") || speechDirection.equals("runter") || speechDirection.equals("nach oben") || speechDirection.equals("nach unten") || speechDirection.equals("drauf") || speechDirection.equals("drunter")){
                    return AXIS3UPDOWNNAME;
                }else if(speechDirection.equals("raus") || speechDirection.equals("rein") || speechDirection.equals("ein") || speechDirection.equals("aus")){
                    return AXIS4EXTENDINGNAME;
                }
        }else if(speechName.equals(AXIS56SPEECHNAME) || speechName.equals("geifer") || speechName.equals("schleifer") || speechName.equals("eifer") )  {
                if(speechDirection.equals("nach links") || speechDirection.equals("nach rechts") || speechDirection.equals("links") || speechDirection.equals("rechts")){
                    return AXIS5LEFTRIGHTNAME;
                // TODO für später: vernünftig auseinander halten (ist nicht vor/rück!)
                }else if(speechDirection.equals("rauf") || speechDirection.equals("runter") || speechDirection.equals("nach oben") || speechDirection.equals("nach unten") || speechDirection.equals("drauf") || speechDirection.equals("drunter")){
                    return  AXIS6TURNAXISNAME;
                }
        }

        return null;
    }


}
