package com.codepath.apps.restclienttemplate;

import static com.facebook.stetho.inspector.network.ResponseHandlingInputStream.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeActivity extends AppCompatActivity {

    public static final int  Max_Tweet_Length = 140;
    public static final String TAG = "ComposeActivity";

    EditText etCompose;
    ProgressBar progressBar;
    Button btnTweet;
    TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        client = TwitterApp.getRestClient(this);

        etCompose = findViewById(R.id.etCompose);
        btnTweet = findViewById(R.id.btnTweet);
        progressBar= (ProgressBar) findViewById(R.id.pbLoading);
        progressBar.setVisibility(View.INVISIBLE);

        // Set click listener on button
        btnTweet.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                String tweetContent = etCompose.getText().toString();

                progressBar.setVisibility(View.VISIBLE);
                if(tweetContent.isEmpty()) {
                    Toast.makeText(ComposeActivity.this, "Sorry your tweet cannot be empty", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }
                if(tweetContent.length() > Max_Tweet_Length) {
                    Toast.makeText(ComposeActivity.this, "Sorry your tweet is to long", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }
                Toast.makeText(ComposeActivity.this,tweetContent, Toast.LENGTH_LONG).show();


                // run a background job and once complete


                // Making an API call to Twitter to publish the tweet
                client.publishTweet(tweetContent, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess to publish Tweet");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Log.i(TAG,"Published Tweet says: "+tweet.body);
                            Intent intent =  new Intent() ;
                            intent.putExtra("tweet", Parcels.wrap(tweet));
                            // set result code and bundle data for response
                            setResult(RESULT_OK, intent);
                            // closes the activity, pass data to parent
                            progressBar.setVisibility(View.INVISIBLE);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to publish Tweet", throwable);
                    }
                });
            }
        });

        // on some click or some loading we need to wait for...



    }
}