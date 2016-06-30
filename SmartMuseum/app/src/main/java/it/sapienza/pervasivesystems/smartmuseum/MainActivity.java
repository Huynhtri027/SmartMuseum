package it.sapienza.pervasivesystems.smartmuseum;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.SystemRequirementsChecker;

import java.util.List;

import it.sapienza.pervasivesystems.smartmuseum.business.beacons.Ranging;
import it.sapienza.pervasivesystems.smartmuseum.business.beacons.RangingDetection;
import it.sapienza.pervasivesystems.smartmuseum.business.interlayercommunication.ILCMessage;
import it.sapienza.pervasivesystems.smartmuseum.business.slack.SlackBusiness;
import it.sapienza.pervasivesystems.smartmuseum.view.slack.ChatAsync;
import it.sapienza.pervasivesystems.smartmuseum.view.slack.ChatAsyncResponse;
import it.sapienza.pervasivesystems.smartmuseum.view.slack.QuestionsActivity;

public class MainActivity extends AppCompatActivity implements RangingDetection, ChatAsyncResponse {

    //This activity will range for beacons for some seconds to understand if the user is inside or outside
    //the museum;
    public static Ranging beaconsRanging;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Open Slack Session;
        new ChatAsync(this, SmartMuseumApp.loggedUser, SlackBusiness.SlackCommand.OPEN_SESSION, "", "").execute();

        //show progress popup
        this.progressDialog = new ProgressDialog(MainActivity.this,
                R.style.AppTheme_Dark_Dialog);
        this.progressDialog.setIndeterminate(true);
        this.progressDialog.setMessage("Loading data... Please wait.");
        this.progressDialog.show();

        //start ranging;
        this.beaconsRanging = new Ranging(this);
        Ranging.rangingDetection = this;
        this.beaconsRanging.initRanging();

//        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this,
//                R.style.AppTheme_Dark_Dialog);
//        progressDialog.setIndeterminate(true);
//        progressDialog.setMessage("Detecting your position. Please wait...");
//        progressDialog.show();

//        new android.os.Handler().postDelayed(
//                new Runnable() {
//                    public void run() {
//                        goToFirstActivity();
//                        progressDialog.dismiss();
//                    }
//                }, 3000);

//        Intent intent = null;
//
//        //if the user is inside of the museum, the list of exhibits will be called
//        if(SmartMuseumApp.isUserInsideMuseum) {
//            intent = new Intent(this, ListOfExhibitsActivity.class);
//        } else { // otherwise the history of users exhibits list will be called
//            intent = new Intent(this, ListOfUHExhibitsActivity.class);
//        }
//
//        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        Log.i("MainActivity", "Start Ranging");
        this.beaconsRanging.startRanging();
    }

    @Override
    protected void onPause() {
        Log.i("MainActivity", "Stop Ranging");
        this.beaconsRanging.stopRanging();

        super.onPause();
    }

    private void goToFirstActivity() {

        //ANDREA: Slack test. at the end goto LoginActivity;
        //TODO: control if the user is already logged in and if he is inside or outside
//        Intent intent = new Intent(this, LoginActivity.class);

        Intent intent = new Intent(this, QuestionsActivity.class);

        startActivity(intent);
    }

    @Override
    public void beaconsDetected(ILCMessage message) {
        Log.i("MainActivity", message.getMessageText());
        List<Beacon> listOfBeaconsDetected = (List<Beacon>) message.getMessageObject();
        for(Beacon b: listOfBeaconsDetected) {
            Log.i("MainActivity", "Beacon detected: " + b.getMajor() + ":" + b.getMinor() + ", " + b.getMacAddress() + ", " + b.getRssi());
        }

        //stop ranging;
        Log.i("MainActivity", "Stop Ranging");
        this.beaconsRanging.stopRanging();

        SmartMuseumApp.isUserInsideMuseum = true;
    }

    @Override
    public void sessionOpened(ILCMessage message) {
        Log.i("CHATACTIVITY", message.getMessageText());

        //hide progress popup;
        this.progressDialog.dismiss();
        goToFirstActivity();
    }

    @Override
    public void sessionClosed(ILCMessage message) {
        Log.i("CHATACTIVITY", message.getMessageText());
    }

    @Override
    public void messagesDownloaed(ILCMessage message) {
    }

    @Override
    public void messageSent(ILCMessage message) {

    }

    @Override
    public void messagesPushed(ILCMessage message) {

    }
}
