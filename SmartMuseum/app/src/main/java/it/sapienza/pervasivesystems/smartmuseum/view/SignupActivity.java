package it.sapienza.pervasivesystems.smartmuseum.view;

import android.app.ProgressDialog;
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
import it.sapienza.pervasivesystems.smartmuseum.model.db.UserDB;
import it.sapienza.pervasivesystems.smartmuseum.model.entity.UserModel;

public class SignupActivity extends AppCompatActivity implements SignupAsyncResponse {
    private static final String TAG = "SignupActivity";


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
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        // TODO: Implement your own signup logic here.
        /*************ANDREA TEST***********************/
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setEmail(email);
        userModel.setPassword(password);
        userModel.setProfileImage("img111111111");
        new SignupAsync(this, userModel).execute();
        /***********************************************/

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onSignupSuccess or onSignupFailed
                        // depending on success
                        onSignupSuccess();
                        // onSignupFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }


    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
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

    @Override
    public void processFinish(boolean result) {
        if(result) {
            Log.i("SignupActivity", "processFinish> SIGNUP OK");
        }
        else {
            Log.i("SignupActivity", "processFinish> SIGUP ERROR");
        }
    }
}

/***********************************************************************/
/* Async Task to retrieve data from neo4j rest ws */
interface SignupAsyncResponse {
    void processFinish(boolean result);
}

class SignupAsync extends AsyncTask<Void, Integer, String>
{
    private SignupAsyncResponse delegate;

    private UserModel userModel;
    private boolean operationResult = false;

    public SignupAsync(SignupAsyncResponse d, UserModel um) {
        this.userModel = um;
        this.delegate = d;
    }

    protected void onPreExecute (){
        Log.d("SignupAsync","On pre Exceute......");
    }

    protected String doInBackground(Void...arg0) {
        Log.d("SignupAsync","On doInBackground...");

        this.operationResult = new UserDB().createUser(userModel);

        return "createUser result: " + this.operationResult;
    }

    protected void onProgressUpdate(Integer...a){
        Log.d("SignupAsync", "You are in progress update ... " + a[0]);
    }

    protected void onPostExecute(String result) {
        this.delegate.processFinish(this.operationResult);
    }
}