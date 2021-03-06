package it.sapienza.pervasivesystems.smartmuseum.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.sapienza.pervasivesystems.smartmuseum.R;
import it.sapienza.pervasivesystems.smartmuseum.SmartMuseumApp;
import it.sapienza.pervasivesystems.smartmuseum.business.interlayercommunication.ILCMessage;
import it.sapienza.pervasivesystems.smartmuseum.business.slack.SlackBusiness;
import it.sapienza.pervasivesystems.smartmuseum.business.user.UserBusiness;
import it.sapienza.pervasivesystems.smartmuseum.model.entity.SlackChannelModel;
import it.sapienza.pervasivesystems.smartmuseum.model.entity.UserModel;
import it.sapienza.pervasivesystems.smartmuseum.view.slack.ChatAsync;
import it.sapienza.pervasivesystems.smartmuseum.view.slack.ChatAsyncResponse;

public class SignupActivity extends AppCompatActivity implements SignupAsyncResponse, ChatAsyncResponse {
    private static final String TAG = "SignupActivity";
    private ProgressDialog progressDialog;

    @Bind(R.id.input_name)
    EditText _nameText;
    @Bind(R.id.input_email)
    EditText _emailText;
    @Bind(R.id.input_password)
    EditText _passwordText;
    @Bind(R.id.btn_signup)
    Button _signupButton;
    @Bind(R.id.link_login)
    TextView _loginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToSlack();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });

        this.progressDialog = new ProgressDialog(SignupActivity.this, R.style.AppTheme_Dark_Dialog);
    }

    private void connectToSlack() {
        // Open Slack Session only if it is not already been opened;
        if (SmartMuseumApp.slackSession == null) {
            new ChatAsync(this, SmartMuseumApp.loggedUser, SlackBusiness.SlackCommand.OPEN_SESSION, "", "").execute();

            //show progress popup
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Connecting to Slack... Please Wait.");
            progressDialog.show();
        } else {
            this.signup();
        }
    }

    private void signup() {
        if (!validate()) {
            onSignupFailed("Registration failed. Please try again.");
            return;
        }

        _signupButton.setEnabled(false);

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setEmail(email);
        userModel.setPassword(password);
        userModel.setProfileImage("img111111111");

        this.progressDialog.setIndeterminate(true);
        this.progressDialog.setMessage("Creating Account... Please wait.");
        this.progressDialog.show();

        new SignupAsync(this, userModel).execute();
    }

    private void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        //finish();
        this.goToLoginActivity();
    }

    private void onSignupFailed(String msg) {
        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
        _signupButton.setEnabled(true);
    }

    private boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void processFinish(ILCMessage message) {
        Log.i("SignupActivity", message.getMessageText());

        switch(message.getMessageType()) {
            case SUCCESS:
                this.progressDialog.dismiss();
                this.onSignupSuccess();
                break;
            case ERROR:
                onSignupFailed(message.getMessageText());
                break;
            case WARNING:
                Toast.makeText(getBaseContext(), message.getMessageText(), Toast.LENGTH_LONG).show();
                onSignupSuccess();

                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Creating Account...");
                progressDialog.show();
                progressDialog.dismiss();
                break;
        }
    }

    @Override
    public void sessionOpened(ILCMessage message) {
        //hide progress popup;
        this.progressDialog.dismiss();
        this.signup();
    }

    @Override
    public void sessionClosed(ILCMessage message) {

    }

    @Override
    public void messagesDownloaed(ILCMessage message) {

    }

    @Override
    public void messageSent(ILCMessage message) {

    }

    @Override
    public void channelCreated(ILCMessage message) {

    }

    @Override
    public void channelListFetched(ILCMessage message) {

    }
}

/***********************************************************************/
/* Async Task to retrieve data from neo4j rest ws */
interface SignupAsyncResponse {
    void processFinish(ILCMessage message);
}

class SignupAsync extends AsyncTask<Void, Integer, String>
{
    private SignupAsyncResponse delegate;

    private UserModel userModel;
    private boolean operationResult = false;
    private final UserBusiness userBusiness = new UserBusiness();
    private ILCMessage message = new ILCMessage();
    private SlackBusiness slackBusiness = new SlackBusiness();

    public SignupAsync(SignupAsyncResponse d, UserModel um) {
        this.userModel = um;
        this.delegate = d;
    }

    protected void onPreExecute (){
        Log.d("SignupAsync","On pre Exceute......");
    }

    protected String doInBackground(Void...arg0) {
        Log.d("SignupAsync","On doInBackground...");

        UserModel userAlreadyRegistered = this.userBusiness.getUserByEmail(this.userModel.getEmail());
        if(userAlreadyRegistered == null) {

            //We choose one of the slack channels for the chat implementation;
            SlackChannelModel freeChannel = this.slackBusiness.getFreeSlackChannel();
            if(freeChannel == null) {
                this.message.setMessageType(ILCMessage.MessageType.ERROR);
                this.message.setMessageText("Error. We ran out of free slack channels. Please contact the system administrator.");
                this.message.setMessageObject(new Boolean(false));
            }
            else {
                this.operationResult = this.userBusiness.createUser(userModel, freeChannel);
                if (this.operationResult) {
                    this.message.setMessageType(ILCMessage.MessageType.SUCCESS);
                    this.message.setMessageText("You correctly registered to SmartMuseum!");
                    this.message.setMessageObject(new Boolean(this.operationResult));
                } else {
                    this.message.setMessageType(ILCMessage.MessageType.ERROR);
                    this.message.setMessageText("There was a problem with your registration. Please try again.");
                    this.message.setMessageObject(new Boolean(this.operationResult));
                }
            }
        }
        else {
            this.operationResult = false;
            this.message.setMessageType(ILCMessage.MessageType.WARNING);
            this.message.setMessageText("Your email is already in our system. Please choose another one.");
            this.message.setMessageObject(new Boolean(this.operationResult));
        }
        return "createUser result: " + this.operationResult;
    }

    protected void onProgressUpdate(Integer...a){
        Log.d("SignupAsync", "You are in progress update ... " + a[0]);
    }

    protected void onPostExecute(String result) {
        this.delegate.processFinish(this.message);
    }
}
