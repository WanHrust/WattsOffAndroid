package rustamav.wattsoff.com;

/**
 * Created by Rustam on 3/28/2016.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;

public class GcmMessageHandlerGoogle extends GcmListenerService {
    public static final int MESSAGE_NOTIFICATION_ID = 435345;
    private NotificationCompat.Builder mBuilder;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        SharedPreferences pref = getSharedPreferences("AppPref", MODE_PRIVATE);
        String messageType = data.getString("messageType");
        SharedPreferences.Editor edit = pref.edit();
        //Storing Data using SharedPreferences

        if (messageType.equalsIgnoreCase("actionStarted")) {
            String message = data.getString("message");
            String title = data.getString("title");
            String requiredAmount = data.getString("requiredAmount");
            String time = data.getString("time");

            edit.putString("state", "actionStarted");
            edit.putString("requiredAmount", requiredAmount);
            edit.putString("gcmmessage", message);
            edit.putString("timeToAct", time);
            edit.commit();
            //Log.d("GCM*", message);
            createNotification(title, message, requiredAmount);
        } else if (messageType.equalsIgnoreCase("actionEnd")) {
            edit.putString("state", "actionEnded");
            edit.commit();
        } else if (messageType.equalsIgnoreCase("progress")) {
            String totalContribution = data.getString("message");
            edit.putString("totalAmountContributed", totalContribution);
            edit.commit();
        }

    }

    // Creates notification based on title and body received
    private void createNotification(String title, String body, String requiredAmount) {
        Context context = getBaseContext();
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher).setContentTitle(title)
                .setContentText(body).setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});


        Intent resultIntent = new Intent(this, ActionActivity.class);
        resultIntent.putExtra("gcmmessage", body);
        resultIntent.putExtra("gcmrequiredamount", requiredAmount);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(MESSAGE_NOTIFICATION_ID, mBuilder.build());
    }

}