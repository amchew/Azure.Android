package com.azure.mobile.azuredataandroidexample_java.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.azure.data.AzureData;
import com.azure.data.model.Database;
import com.azure.mobile.azuredataandroidexample_java.Adapter.Callback;
import com.azure.mobile.azuredataandroidexample_java.Adapter.CardAdapter;
import com.azure.mobile.azuredataandroidexample_java.Adapter.DatabaseViewHolder;
import com.azure.mobile.azuredataandroidexample_java.R;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.AuthenticationResult;
import com.microsoft.identity.client.MsalClientException;
import com.microsoft.identity.client.MsalException;
import com.microsoft.identity.client.MsalServiceException;
import com.microsoft.identity.client.PublicClientApplication;

import static com.azure.data.util.FunctionalUtils.onCallback;

public class DatabaseActivity extends Activity {

    private static final String TAG = "DatabaseActivity";
    private ProgressBar _spinner;
    private PublicClientApplication _sampleApp;
    private CardAdapter<Database> _adapter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.databases_activity);

        LinearLayoutManager linearLayoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        _adapter = new CardAdapter<>(R.layout.database_view, new Callback<Object>() {
            @Override
            public Void call() {
                Database db = (Database) this._result;
                DatabaseViewHolder vHolder = (DatabaseViewHolder) this._viewHolder;

                vHolder.idTextView.setText(db.getId());
                vHolder.ridTextView.setText(db.getResourceId());
                vHolder.selfTextView.setText(db.getSelfLink());
                vHolder.eTagTextView.setText(db.getEtag());
                vHolder.collsTextView.setText(db.getCollectionsLink());
                vHolder.usersTextView.setText(db.getUsersLink());

                vHolder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(getBaseContext(), CollectionsActivity.class);
                    intent.putExtra("db_id", db.getId());
                    startActivity(intent);
                });

                return null;
            }
        }, DatabaseViewHolder.class);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(_adapter);

        Button clearButton = findViewById(R.id.button_clear);
        Button fetchButton = findViewById(R.id.button_fetch);
        Button createButton = findViewById(R.id.button_create);
        Button loginButton = findViewById(R.id.button_login);
        _spinner = findViewById(R.id.spinner);
        clearButton.setOnClickListener(v -> _adapter.clear());
        loginButton.setOnClickListener(v -> login());

        createButton.setOnClickListener(v -> {

            View editTextView = getLayoutInflater().inflate(R.layout.edit_text, null);
            EditText editText = editTextView.findViewById(R.id.editText);
            TextView messageTextView = editTextView.findViewById(R.id.messageText);
            messageTextView.setText(R.string.database_dialogue);

            new AlertDialog.Builder(DatabaseActivity.this)
                    .setView(editTextView)
                    .setPositiveButton("Create", (dialog, whichButton) -> {
                        String databaseId = editText.getText().toString();
                        final ProgressDialog progressDialog = ProgressDialog.show(DatabaseActivity.this, "", "Creating. Please wait...", true);

                        AzureData.createDatabase(databaseId, onCallback(response -> {

                            Log.e(TAG, "Database create result: " + response.isSuccessful());

                            runOnUiThread(() -> {
                                dialog.cancel();
                                _adapter.addData(response.getResource());

                                progressDialog.cancel();
                            });
                        }));
                    })
                    .setNegativeButton("Cancel", (dialog, whichButton) -> {
                    }).show();
        });

        fetchButton.setOnClickListener(v -> {
            try
            {
                final ProgressDialog dialog = ProgressDialog.show(DatabaseActivity.this, "", "Loading. Please wait...", true);

                AzureData.getDatabases(null, onCallback(response -> {

                    Log.e(TAG, "Database list result: " + response.isSuccessful());

                    runOnUiThread(() -> {
                        _adapter.clear();

                        for (Database db: response.getResource().getItems()) {
                            _adapter.addData(db);
                        }

                        dialog.cancel();
                    });
                }));
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        _sampleApp.handleInteractiveRequestRedirect(requestCode, resultCode, data);
    }

    private void login() {
        _spinner.setVisibility(View.VISIBLE);

        _sampleApp = new PublicClientApplication(
                this.getApplicationContext(),
                Constants.CLIENT_ID,
                String.format(Constants.AUTHORITY, Constants.TENANT, Constants.SISU_POLICY));
        String[] scopes = Constants.SCOPES.split("\\s+");
        _sampleApp.acquireToken(this, scopes, getAuthInteractiveCallback());
    } private AuthenticationCallback getAuthInteractiveCallback() {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                _spinner.setVisibility(View.GONE);

                /* Successfully got a token, call api now */
                Log.d(TAG, "Successfully authenticated");
                Log.d(TAG, "ID Token: " + authenticationResult.getIdToken());

                Toast.makeText(DatabaseActivity.this, "Successful login.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(MsalException exception) {
                _spinner.setVisibility(View.GONE);
                /* Failed to acquireToken */
                Log.d(TAG, "Authentication failed: " + exception.toString());

                Toast.makeText(DatabaseActivity.this, "Authentication failed: " + exception.toString(), Toast.LENGTH_LONG).show();
                if (exception instanceof MsalClientException) {
                    /* Exception inside MSAL, more info inside MsalError.java */
                    assert true;
                } else if (exception instanceof MsalServiceException) {
                    /* Exception when communicating with the STS, likely config issue */
                    assert true;

                }
            }

            @Override
            public void onCancel() {
                _spinner.setVisibility(View.GONE);
                /* User canceled the authentication */
                Log.d(TAG, "User cancelled login.");
            }
        };
    }
}