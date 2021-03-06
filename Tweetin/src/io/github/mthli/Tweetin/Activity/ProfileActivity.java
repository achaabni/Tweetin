package io.github.mthli.Tweetin.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import io.github.mthli.Tweetin.Fragment.ProfileFragment;
import io.github.mthli.Tweetin.R;
import io.github.mthli.Tweetin.Unit.Anim.ActivityAnim;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class ProfileActivity extends FragmentActivity {
    private Twitter twitter;
    private long useId = 0l;
    private long userId = 0l;
    private String useScreenName = null;
    private String userScreenName = null;
    public Twitter getTwitter() {
        return twitter;
    }
    public long getUseId() {
        return useId;
    }
    public long getUserId() {
        return userId;
    }
    public String getUseScreenName() {
        return useScreenName;
    }
    public String getUserScreenName() {
        return userScreenName;
    }

    private ProfileFragment profileFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        SharedPreferences preferences = getSharedPreferences(
                getString(R.string.sp_name),
                MODE_PRIVATE
        );
        useId = preferences.getLong(getString(R.string.sp_use_id), 0);
        useScreenName = preferences.getString(
                getString(R.string.sp_use_screen_name),
                null
        );
        String conKey = preferences.getString(getString(R.string.sp_consumer_key), null);
        String conSecret = preferences.getString(getString(R.string.sp_consumer_secret), null);
        String accToken = preferences.getString(getString(R.string.sp_access_token), null);
        String accTokenSecret = preferences.getString(getString(R.string.sp_access_token_secret), null);
        TwitterFactory factory = new TwitterFactory();
        twitter = factory.getInstance();
        twitter.setOAuthConsumer(conKey, conSecret);
        AccessToken token = new AccessToken(accToken, accTokenSecret);
        twitter.setOAuthAccessToken(token);

        Intent intent = getIntent();
        userId = intent.getLongExtra(
                getString(R.string.profile_intent_user_id),
                0
        );
        userScreenName = intent.getStringExtra(
                getString(R.string.profile_intent_user_screen_name)
        );

        profileFragment = (ProfileFragment) getSupportFragmentManager()
                .findFragmentById(R.id.profile_fragment);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ActivityAnim anim = new ActivityAnim();
            profileFragment.cancelAllTask();
            finish();
            anim.rightOut(this);
        }

        return true;
    }
    @Override
    public void onDestroy() {
        profileFragment.cancelAllTask();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation== Configuration.ORIENTATION_LANDSCAPE) {
            /* Do nothing */
        }
        else{
            /* Do nothing */
        }
    }
}
