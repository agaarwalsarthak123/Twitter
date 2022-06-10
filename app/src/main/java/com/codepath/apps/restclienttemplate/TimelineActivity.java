package com.codepath.apps.restclienttemplate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.TweetDao;
import com.codepath.apps.restclienttemplate.models.TweetWithUser;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {

    private EndlessRecyclerViewScrollListener scrollListener;
    public static final String TAG = "TimeLineActivity";
    private SwipeRefreshLayout swipeContainer;
    private final int REQUEST_CODE = 20;

    TweetDao tweetDao;
    TwitterClient client;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    public long max_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        rvTweets = findViewById(R.id.rvTweets);
        // Initialize the list of tweets and adapters
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);
        // Recycler view setup: Layout manager and the adapter
        rvTweets.setLayoutManager(new LinearLayoutManager(this));
        rvTweets.setAdapter(adapter);

        scrollListener = new EndlessRecyclerViewScrollListener(new LinearLayoutManager(this)) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.d(TAG,"aaaAaAaAaAa");
                loadNextDataFromApi(max_id);
            }
        };
        rvTweets.addOnScrollListener(scrollListener);


        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        // Setup refresh listener which triggers new data loading

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                fetchTimelineAsync(0);
            }



            private void fetchTimelineAsync(int i) {

                client.getHomeTimeLine(new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        adapter.clear();
                        // ...the data has come back, add new items to your adapter...
                        try {
                            adapter.addAll(Tweet.fromJsonArray(json.jsonArray));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // Now we call setRefreshing(false) to signal refresh has finished
                        swipeContainer.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.d("DEBUG", "Fetch timeline error: " + response);
                    }

                });
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        client = TwitterApp.getRestClient(this);
        tweetDao = ((TwitterApp) getApplicationContext()).getMyDatabase().tweetDao();

        // Find te recycler view



        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Showing data from database");
                List<TweetWithUser> tweetWithUsers = tweetDao.recentItems();
                List<Tweet> tweetsFromDB = TweetWithUser.getTweetList(tweetWithUsers);
                adapter.clear();
                adapter.addAll(tweetsFromDB);
            }
        });
        populateHomeTimeline();
    }

    public void loadNextDataFromApi(long id) {
        Log.d(TAG,"AssSassaSasaSsaaSs");
        client.getMoreHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
              //  bar();
                try {
                    JSONArray jsonArray = json.jsonArray;
                    Log.i(TAG, "onSuccess!  loadNextDataFromApi" + json.toString());
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    max_id = getMinId(tweets);
                    adapter.notifyDataSetChanged();
                    Log.i(TAG, "onSuccess! size is " + tweets.size());
                    // hideprgrss

                } catch (JSONException e) {
                    Log.e(TAG, "Json exception");
                    e.printStackTrace();
                }
                // hideprgrss

            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d("DEBUG", "Fetch timeline error: " + response);
            }
        }, id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.compose) {
            // Compose icon has been selected

            // Navigate to the compose activity
            Intent intent = new Intent(this, ComposeActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {
            // get data from the intent (tweet)
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            // update the recycler with the tweet
            // Modify data source of tweets
            tweets.add(0, tweet);
            // Update the adapter
            adapter.notifyItemInserted(0);
            rvTweets.smoothScrollToPosition(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void populateHomeTimeline() {
        client.getHomeTimeLine(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess!");
                JSONArray jsonArray = json.jsonArray;
                try {
                    List<Tweet>tweetsFromNetwork = Tweet.fromJsonArray(jsonArray);
                   //Log.d("Sarthak", String.valueOf(tweetsFromNetwork));

                    adapter.clear();
                    tweets.addAll(tweetsFromNetwork);
                    //adapter.addAll(tweetsFromNetwork);
                    max_id = getMinId(tweets);
                   adapter.notifyDataSetChanged();
                    swipeContainer.setRefreshing(false);
          /*       AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Saving data into the database");

                            // insert users first
                          //  Log.d("Sarthak", String.valueOf(tweetsFromNetwork));
                            List<User> usersFromNetwork = User.fromJsonTweetArray(tweetsFromNetwork);
                           // Log.d("Sarthak", String.valueOf(usersFromNetwork));
                            tweetDao.insertModel(usersFromNetwork.toArray(new Tweet[0]));
                            //insert tweets
                            tweetDao.insertModel(tweetsFromNetwork.toArray(new Tweet[0]));
                        }
                    });*/

                } catch (JSONException e) {
                    Log.e(TAG, "Json exception", e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure!" + response, throwable);
            }
        });
    }

    private static long getMinId(List<Tweet> tweets) {
        long id = Long.MAX_VALUE;
        for (int i = 0; i<tweets.size(); i++) {
           // Log.d("TimelineActivity", " " + tweets.get(i).id);
            if (tweets.get(i).id < id && tweets.get(i).id>1) {
                id = tweets.get(i).id;
            }
        }
        return id;
    }


    // TimelineActivity.java
    public void onLogoutButton(View view) {

        finish();
        // forget who's logged in
        TwitterApp.getRestClient(this).clearAccessToken();

        // navigate backwards to Login screen
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // this makes sure the Back button won't work
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // same as above
        startActivity(i);
    }
}