package io.github.mthli.Tweetin.Task.Post;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.EditText;
import android.widget.ToggleButton;
import io.github.mthli.Tweetin.Activity.PostActivity;
import io.github.mthli.Tweetin.R;
import io.github.mthli.Tweetin.Unit.Flag.Flag;
import twitter4j.GeoLocation;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;

import java.io.File;

public class PostTask extends AsyncTask<Void, Integer, Boolean> {
    private PostActivity postActivity;
    private boolean checkIn;
    private boolean picture;
    private String text;
    private String picturePath;

    private Twitter twitter;
    private StatusUpdate update;

    private int postFlag;
    private long statusId;
    private String screenName;

    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;

    public PostTask(PostActivity postActivity) {
        this.postActivity = postActivity;
        this.checkIn = false;
        this.picture = false;
        this.picturePath = null;
        this.postFlag = 0;
        this.statusId = 0;
        this.screenName = null;
    }

    @Override
    protected void onPreExecute() {
        EditText postEdit = postActivity.getPostEdit();
        ToggleButton postCheckInButton = postActivity.getPostCheckInButton();
        ToggleButton postPhotoButton = postActivity.getPostPictureButton();
        if (postCheckInButton.isChecked()) {
            checkIn = true;
        }
        if (postPhotoButton.isChecked()) {
            picture = true;
        }
        text = postEdit.getText().toString();
        picturePath = postActivity.getPicturePath();

        twitter = postActivity.getTwitter();

        postFlag = postActivity.getPostFlag();
        statusId = postActivity.getStatusId();
        screenName = postActivity.getScreenName();

        notificationManager = (NotificationManager) postActivity
                .getSystemService(Context.NOTIFICATION_SERVICE);
        LocationManager locationManager = (LocationManager) postActivity
                .getSystemService(Context.LOCATION_SERVICE);
        builder = new NotificationCompat.Builder(postActivity);
        builder.setSmallIcon(R.drawable.ic_notification_send);
        builder.setTicker(
                postActivity.getString(R.string.post_notification_post_ing)
        );
        builder.setContentTitle(
                postActivity.getString(R.string.post_notification_post_ing)
        );
        builder.setContentText(text);
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(Flag.NOTIFICATION_PROGRESS_ID, notification);

        update = new StatusUpdate(text);
        if (checkIn) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    1000,
                    0,
                    new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            /* Do nothing */
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {
                            /* Do nothing */
                        }

                        @Override
                        public void onProviderEnabled(String provider) {
                            /* Do nothing */
                        }

                        @Override
                        public void onProviderDisabled(String provider) {
                            /* Do nothing */
                        }
                    }
            );
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                GeoLocation geo = new GeoLocation(
                        location.getLatitude(),
                        location.getLongitude()
                );
                update.setLocation(geo);
            }
        }
        if (picture) {
            File file = new File(picturePath);
            update.setMedia(file);
        }
        if (
                (postFlag == Flag.POST_REPLY || postFlag == Flag.POST_QUOTE)
                && text.contains(screenName)
                && statusId != -1l
        ) {
            update.setInReplyToStatusId(statusId);
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            twitter.updateStatus(update);

            builder.setSmallIcon(R.drawable.ic_notification_send);
            builder.setTicker(
                    postActivity.getString(R.string.post_notification_post_successful)
            );
            builder.setContentTitle(
                    postActivity.getString(R.string.post_notification_post_successful)
            );
            builder.setContentText(text);
            Notification notification = builder.build();
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(Flag.NOTIFICATION_PROGRESS_ID, notification);
            notificationManager.cancel(Flag.NOTIFICATION_PROGRESS_ID);
        } catch (Exception e) {
            builder.setSmallIcon(R.drawable.ic_notification_send);
            builder.setTicker(
                    postActivity.getString(R.string.post_notification_post_failed)
            );
            builder.setContentTitle(
                    postActivity.getString(R.string.post_notification_post_failed)
            );
            builder.setContentText(text);

            Intent resultIntent = postActivity.getIntent();
            resultIntent.putExtra(
                    postActivity.getString(R.string.post_intent_flag),
                    Flag.POST_RESEND
            );
            resultIntent.putExtra(
                    postActivity.getString(R.string.post_intent_resend_flag),
                    postFlag
            );
            resultIntent.putExtra(
                    postActivity.getString(R.string.post_intent_status_id),
                    statusId
            );
            resultIntent.putExtra(
                    postActivity.getString(R.string.post_intent_status_screen_name),
                    screenName
            );
            resultIntent.putExtra(
                    postActivity.getString(R.string.post_intent_status_text),
                    text
            );
            if (checkIn) {
                resultIntent.putExtra(
                        postActivity.getString(R.string.post_intent_check_in),
                        true
                );
            }
            if (picture) {
                resultIntent.putExtra(
                        postActivity.getString(R.string.post_intent_picture),
                        true
                );
                resultIntent.putExtra(
                        postActivity.getString(R.string.post_intent_picture_path),
                        picturePath
                );
            }
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(postActivity);
            stackBuilder.addParentStack(PostActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_ONE_SHOT
            );
            builder.setContentIntent(pendingIntent);

            Notification notification = builder.build();
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(Flag.NOTIFICATION_PROGRESS_ID, notification);

            return false;
        }

        if (isCancelled()) {
            return false;
        }
        return true;
    }

    @Override
    protected void onCancelled() {
        /* Do nothing */
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        /* Do nothing */
    }

    @Override
    protected void onPostExecute(Boolean result) {
        /* Do nothing */
    }
}
