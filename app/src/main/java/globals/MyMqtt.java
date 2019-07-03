package globals;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


import com.example.porchlyt_artisan.AnswerServiceRequestDialogActivity;
import com.example.porchlyt_artisan.CardPaymentReceivedActivity;
import com.example.porchlyt_artisan.ConfirmPaymentRecievedActivity;
import com.example.porchlyt_artisan.DisputeNotificationActivity;
import com.example.porchlyt_artisan.R;
import com.example.porchlyt_artisan.app;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;


import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import MainActivityTabs.ProfileFragment;
import io.realm.Realm;
import models.appSettings;
import models.mArtisan.mArtisan;

public class MyMqtt {
    public static MqttAndroidClient mqttClient;
    public static Context ctx;
    //final String serverUri = "tcp://postman.cloudmqtt.com:16998";
    final static String serverUri = "tcp://192.168.138.1:1883";
    static String clientId = "";//this is the client id for this specific device, this is the maintain the correct messages
    final String username = "uiemrvua";
    final String password = "cB51Tz-tin54";
    static String tag = "mqtt";
    public static String mqtt_server = "porchlyt_mqtt_server";

    //init the mqtt service
    public static void init(Context context) {
        ctx = context;

        //get the correct client id for this specific device
        Realm db = Realm.getDefaultInstance();
        mArtisan artisan = db.where(mArtisan.class).findFirst();
        appSettings aps = db.where(appSettings.class).findFirst();
        clientId = artisan.app_id;//use this topic for real time comms with the client app
        db.close();
        mqttClient = new MqttAndroidClient(app.ctx, serverUri, clientId);
        mqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                subscribeToTopic(clientId, 0);//listen to these realtime notifications
            }

            @Override
            public void connectionLost(Throwable throwable) {

                Log.e(tag, "connection lost: " + throwable.getLocalizedMessage());
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

                //this is for card payment
                if (type.equals("card_payment_recieved")) {
                    //todo set the amount to show on the profile fragment
                    try {
                        Double amount_payed = json.getDouble("amount_payed");
                        String _job_id = json.getString("_job_id");
                        Intent cp = new Intent(app.ctx, CardPaymentReceivedActivity.class);
                        cp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        cp.putExtra("amount_payed", amount_payed);
                        cp.putExtra("_job_id", _job_id);
                        app.ctx.startActivity(cp);
                    }catch (Exception ex){
                        //Toast.makeText(app.ctx,ex.getLocalizedMessage()+" ",Toast.LENGTH_LONG).show();
                    }
                }

                if (type.equals("rating_notification")) {
                    Realm db = globals.getDB();
                    try
                    {
                        //save my rating and notify the artisan
                        int rating = json.getInt("rating");
                        //update my rating
                        mArtisan m= db.where(mArtisan.class).findFirst();
                        db.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                m.artisanRating.add(rating);
                                ProfileFragment.get_my_rating();//show my rating
                                Toast.makeText(app.ctx,app.ctx.getString(R.string.you_have_recieved_a_rating_of)+" " +rating,Toast.LENGTH_LONG).show();
                            }
                        });
                    }catch (Exception ex)
                    {
                        Log.e(tag,ex.getMessage());
                    }
                    finally {

                        db.close();
                    }
                }


                if (type.equals("dispute_opened")) {

                    try {
                        String _job_id = json.getString("_job_id");
                        String reason_for_dispute = json.getString("reason_for_dispute");
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
                        Intent request = new Intent(app.ctx, AnswerServiceRequestDialogActivity.class);
                        request.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.EXTRA_DOCK_STATE_CAR);

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
                Log.e(tag, "message delivered");
            }
        });

        connect();//attempt to connect as soon as its created

    }


    public static void connect() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setMaxInflight(1);
        //mqttConnectOptions.setUserName(username);
        //mqttConnectOptions.setPassword(password.toCharArray());

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
                    Log.w(tag, "Failed to connect to: " + serverUri + " " + exception.toString());
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


    public static void subscribeToTopic(final String topic, int qos) {
        try {
            mqttClient.subscribe(topic, qos, app.ctx, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.e(tag, "Subscribed! to " + topic);

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(tag, "Subscribed fail! to " + topic);
                }

            });


        } catch (MqttException ex) {
            System.err.println("Exceptionst subscribing");
            ex.printStackTrace();
        }
    }
}