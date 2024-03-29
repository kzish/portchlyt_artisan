package globals;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.sirachlabs.porchlyt_artisan.AnswerServiceRequestDialogActivity;
import com.sirachlabs.porchlyt_artisan.CardPaymentReceivedActivity;
import com.sirachlabs.porchlyt_artisan.ConfirmPaymentRecievedActivity;
import com.sirachlabs.porchlyt_artisan.DisputeNotificationActivity;
import com.sirachlabs.porchlyt_artisan.R;
import com.sirachlabs.porchlyt_artisan.ViewJobActivity;
import com.sirachlabs.porchlyt_artisan.ViewNotificationActivity;
import com.sirachlabs.porchlyt_artisan.app;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.joda.time.LocalDateTime;
import org.json.JSONObject;

import MainActivityTabs.BlogFragment;
import MainActivityTabs.JobsFragment;
import MainActivityTabs.ProfileFragment;
import models.Account_status;
import models.appSettings;
import models.mArtisan.artisanRating;
import models.mArtisan.mArtisan;
import models.mJobs.JobStatus;
import models.mJobs.mJobs;
import models.mNotification;

public class MyMqtt extends Service {

    public static MqttAndroidClient mqttClient;
    public static Context ctx;
    static String clientId = "";//this is the client id for this specific device, this is the maintain the correct messages
    static String tag = "mqtt";
    public static String mqtt_server = "porchlyt_mqtt_server";

    //init the mqtt service
    public static void init_(Context context) {
        ctx = context;

        //get the correct client id for this specific device
        mArtisan artisan = app.db.mArtisanDao().get_artisan();
        appSettings aps = app.db.appSettingsDao().get_app_settings();
        clientId = artisan.app_id;//use this topic for real time comms with the client app


        mqttClient = new MqttAndroidClient(app.ctx, globals.mqtt_server, clientId);
        mqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                subscribeToTopic(clientId, 1);//listen to these realtime notifications
            }

            @Override
            public void connectionLost(Throwable throwable) {

                Log.e(tag, "connection lost: " + throwable.getLocalizedMessage());
                connect();
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.e(tag, mqttMessage.toString() + "  real time message");
                //Toast.makeText(ctx,mqttMessage.toString(),Toast.LENGTH_SHORT).show();
                JSONObject json = null;
                String type = "";
                try {
                    json = new JSONObject(mqttMessage.toString());
                    type = json.getString("type");
                } catch (Exception ex) {
                    Log.e(tag, "line 72 " + ex.getMessage());
                    return;
                }

                //route the message to the correct handler


                if (type.equals("account_status")) {
                    String msg = json.getString("msg");
                    appSettings aps = app.db.appSettingsDao().get_app_settings();
                    if (msg.equals("active")) {
                        aps.account_status = Account_status.active.toString();
                        app.db.appSettingsDao().update(aps);
                    }
                    if (msg.equals("blocked")) {
                        aps.account_status = Account_status.blocked.toString();
                        app.db.appSettingsDao().update(aps);
                    }
                    if (msg.equals("remit_cash")) {
                        aps.account_status = Account_status.must_remit_cash.toString();
                        app.db.appSettingsDao().update(aps);
                    }
                    ProfileFragment.check_account_status();
                }


                if (type.equals("general_notification")) {
                    create_notification(json.getString("general_notification_message"));

                    //get the earnings information passed in the general notification
                    try {
                        String json_data = json.getString("monthly_earning");
                        JSONObject obj = new JSONObject(json_data);
                        String total_earnings = obj.getString("total_earnings");
                        String total_earnings_this_month = obj.getString("total_earnings_this_month");
                        String total_jobs = obj.getString("total_jobs");
                        String total_jobs_this_month = obj.getString("total_jobs_this_month");

                        mArtisan artisan = app.db.mArtisanDao().get_artisan();
                        artisan.earnings_since_last_disbursement = Double.parseDouble(total_earnings_this_month);
                        app.db.mArtisanDao().update_one(artisan);

                        ProfileFragment.set_my_earning();

                    } catch (Exception ex) {
                        Log.e(tag, ex + "");
                    }

                }

                if (type.equals("job_cancelled")) {
                    try {
                        String notification_id = create_notification(json.getString("reason_for_cancellation"));
                        String _job_id = json.getString("_job_id");
                        mJobs job = app.db.mJobsDao().get_job(_job_id);
                        job.end_time = LocalDateTime.now().toString();
                        job.job_status = JobStatus.cancelled.toString();
                        app.db.mJobsDao().update_one(job);
                        //refresh the jobs adapter
                        JobsFragment.refreshJobsAdapter();
                        ProfileFragment.load_jobs();

                        //close the ViewJobActivity if running
                        try {
                            ViewJobActivity.close_activity();
                        } catch (Exception ex) {
                        }

                        //open the notification activity
                        Intent notification = new Intent(app.ctx, ViewNotificationActivity.class);
                        notification.putExtra("notification_id", notification_id);
                        notification.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        app.ctx.startActivity(notification);
                    } catch (Exception ex) {
                        Log.e(tag, ex.getMessage());
                    }
                }


                if (type.equals("clear_artisan_earning")) {
                    try {
                        mArtisan artisan = app.db.mArtisanDao().get_artisan();
                        artisan.earnings_since_last_disbursement = 0;
                        app.db.mArtisanDao().update_one(artisan);
                        ProfileFragment.set_my_earning();
                    } catch (Exception ex) {
                        Log.e(tag, "clear_artisan_earning " + ex.getMessage());
                    } finally {
                    }
                }


                if (type.equals("artisan_earning")) {
                    //what the artisan has earned
                    //insert it into the database
                    double earning = json.getDouble("earning");
                    try {
                        mArtisan artisan = app.db.mArtisanDao().get_artisan();
                        artisan.earnings_since_last_disbursement += earning;
                        app.db.mArtisanDao().update_one(artisan);
                        ProfileFragment.set_my_earning();
                    } catch (Exception ex) {
                        Log.e(tag, "artisan_earning " + ex.getLocalizedMessage());
                    } finally {
                    }

                }


                if (type.equals("update_artisan_services")) {

                    create_notification(app.ctx.getString(R.string.your_services_have_been_updated));
                    try {
                        //update the artisans services
                        mArtisan artisan = app.db.mArtisanDao().get_artisan();
                        String[] services = json.getString("services").split(":");
                        artisan.skills = TextUtils.join(" . ", services);
                        app.db.mArtisanDao().update_one(artisan);
                        //display the skills
                        ProfileFragment.set_artisan_skills();
                    } catch (Exception ex) {
                        Toast.makeText(app.ctx, app.ctx.getString(R.string.error_updating_services), Toast.LENGTH_SHORT).show();
                    } finally {
                    }
                }

                //this is for card payment
                if (type.equals("card_payment_recieved")) {
                    //todo set the amount to show on the profile fragment

                    create_notification(app.ctx.getString(R.string.card_payment_received));

                    try {
                        Double amount_payed = json.getDouble("amount_payed");
                        String _job_id = json.getString("_job_id");
                        Intent cp = new Intent(app.ctx, CardPaymentReceivedActivity.class);
                        cp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        cp.putExtra("amount_payed", amount_payed);
                        cp.putExtra("_job_id", _job_id);

                        //close the ViewJobActivity if running
                        try {
                            ViewJobActivity.close_activity();
                        } catch (Exception ex) {
                        }


                        app.ctx.startActivity(cp);
                    } catch (Exception ex) {
                        //Toast.makeText(app.ctx,ex.getLocalizedMessage()+" ",Toast.LENGTH_LONG).show();
                    }
                }

                if (type.equals("rating_notification")) {
                    try {
                        //save my rating and notify the artisan
                        int rating = json.getInt("rating");
                        artisanRating a_rating = new artisanRating();
                        a_rating.numStars = rating;
                        //insert my rating into the db
                        app.db.mArtisanRatingDao().insert_one(a_rating);
                        ProfileFragment.get_my_rating();//show my rating
                        ProfileFragment.load_jobs();
                        create_notification(app.ctx.getString(R.string.you_have_recieved_a_rating_of) + " " + rating);
                    } catch (
                            Exception ex) {
                        Log.e(tag, ex.getMessage());
                    } finally {
                    }
                }


                if (type.equals("dispute_opened")) {

                    create_notification(app.ctx.getString(R.string.a_dispute_has_been_opened));

                    try {
                        String _job_id = json.getString("_job_id");
                        String reason_for_dispute = json.getString("reason_for_dispute");
                        mJobs job = app.db.mJobsDao().get_job(_job_id);
                        job.job_status = JobStatus.disputed.toString();
                        app.db.mJobsDao().update_one(job);
                        JobsFragment.refreshJobsAdapter();

                        Intent notification_activity = new Intent(app.ctx, DisputeNotificationActivity.class);
                        notification_activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        notification_activity.putExtra("reason_for_dispute", reason_for_dispute);
                        app.ctx.startActivity(notification_activity);
                    } catch (Exception ex) {
                        Log.e(tag, ex.getMessage());
                    }


                }

                //this is for cash payments
                if (type.equals("cash_payment_recieved")) {

                    create_notification(app.ctx.getString(R.string.cash_payment_received));
                    try {


                        String _job_id = json.getString("_job_id");
                        String amount_payed = json.getString("amount_payed");
                        Intent cp = new Intent(app.ctx, ConfirmPaymentRecievedActivity.class);
                        cp.putExtra("_job_id", _job_id);
                        cp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        //open activity to confirm payment
                        app.ctx.startActivity(cp);
                    } catch (Exception ex) {
                        Log.e(tag, ex.getMessage());
                    }
                }


                if (type.equals("request_task_notification")) {
                    try {

                        //on the screen when this activity starts
                        Intent request = new Intent(app.ctx, AnswerServiceRequestDialogActivity.class);
                        request.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        request.putExtra("lat", json.getString("lat"));
                        request.putExtra("lon", json.getString("lon"));
                        request.putExtra("services", json.getString("services"));
                        request.putExtra("client_app_id", json.getString("client_app_id"));
                        request.putExtra("client_mobile", json.getString("client_mobile"));
                        request.putExtra("request_id", json.getString("request_id"));
                        request.addCategory(Intent.CATEGORY_LAUNCHER);
                        request.putExtra("foreground", true);

                        app.ctx.startActivity(request);
                    } catch (Exception ex) {
                        Log.e(tag, "mqtt line 68 " + ex.getLocalizedMessage());
                    }
                }//.request_task_notification


            }//.messageArrived

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                //Log.e(tag, "message delivered");
            }
        });

        connect();//attempt to connect as soon as its created

    }


    public static void connect() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        //mqttConnectOptions.setMaxInflight(10);

        try {
            mqttClient.connect(mqttConnectOptions, app.ctx, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttClient.setBufferOpts(disconnectedBufferOptions);
                    Log.e(tag, "connection successfull");
                    //subscribeToTopic("test");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w(tag, "Failed to connect to: " + globals.mqtt_server + " " + exception.toString());
                }
            });


        } catch (MqttException ex) {
            Log.e(tag, "line 83 " + ex.getMessage());
        }
    }


    //send a string message to a specific topic
    public static boolean publishStringMessage(String message, String topic) {
        try {
            MqttMessage m = new MqttMessage();
            m.setPayload(message.getBytes());
            m.setQos(1);
            m.setRetained(true);
            mqttClient.publish(topic, m);
            return true;
        } catch (Exception ex) {
            Log.e(tag, ex.getMessage());
            return false;
        }
    }

    //send a string message to a specific topic
    public static boolean publishStringMessage(String message, String topic, int qos) {
        try {
            MqttMessage m = new MqttMessage();
            m.setPayload(message.getBytes());
            m.setQos(qos);
            m.setRetained(true);
            mqttClient.publish(topic, m);
            return true;
        } catch (Exception ex) {
            Log.e(tag, ex.getMessage());
            return false;
        }
    }


    public static void subscribeToTopic(final String topic, int qos) {
        try {
            mqttClient.subscribe(topic, qos, app.ctx, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.e(tag, "Subscribed! to " + topic);

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(tag, "Subscribedgl fail! to " + topic);
                }

            });


        } catch (MqttException ex) {
            System.err.println("Exceptionst subscribing");
            ex.printStackTrace();
        }
    }


    //insert notification into db
    private static String create_notification(String notification_text) {
        String[] notification_id = {""};
        try {
            //insert a notification
            mNotification notification = new mNotification();
            notification.notification_text = notification_text;
            notification_id[0] = notification._id;
            app.db.mNotificationDao().insert_one(notification);

            //notification ontop of screen
            Notification builder = new NotificationCompat.Builder(app.ctx)
                    .setSmallIcon(R.drawable.p_logo_)
                    .setContentTitle(app.ctx.getString(R.string.notification))
                    .setContentText(notification_text)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT).build();

            NotificationManager notificationManager =
                    (NotificationManager) app.ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, builder);


            //for oreo android 8
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                int notifyID = 1;
                String CHANNEL_ID = "my_channel_01";// The id of the channel.
                CharSequence name = app.ctx.getString(R.string.channel_name);// The user-visible name of the channel.
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                // Create a notification and set the notification channel.
                Notification notification_8 = new Notification.Builder(app.ctx)
                        .setContentTitle(app.ctx.getString(R.string.notification))
                        .setContentText(notification_text)
                        .setSmallIcon(R.drawable.p_logo_)
                        .setChannelId(CHANNEL_ID)
                        .build();

                NotificationManager mNotificationManager =
                        (NotificationManager) app.ctx.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.createNotificationChannel(mChannel);
                mNotificationManager.notify(notifyID, notification_8);

            }

            //play a sound for notification
            MediaPlayer mp = MediaPlayer.create(app.ctx, R.raw.plucky);
            mp.start();


            return notification_id[0];
        } catch (Exception ex) {
            Log.e(tag, "line 323 create_notification" + ex.getMessage());
            return "";
        } finally {
            BlogFragment.get_number_of_notifications();//refresh this
        }

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.e(tag, "mqtt_service started");

        if (MyMqtt.mqttClient == null) {//only if client is not already there then re-init
            MyMqtt.init_(this);
        } else if (!MyMqtt.mqttClient.isConnected()) {//if not connected attempt to connect
            connect();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(tag, "mqtt_service stopped");
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}