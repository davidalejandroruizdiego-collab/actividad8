package com.diego.actividad8;


import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.List;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "SMS_ALERTS_CHANNEL";
    private static final String TAG = "SmsReceiver";

    private final List<String> MONITORED_NUMBERS = Arrays.asList("+1234567890", "5551234");

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {

            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);

            if (messages != null) {
                for (SmsMessage sms : messages) {
                    String sender = sms.getDisplayOriginatingAddress();
                    String messageBody = sms.getMessageBody();

                    Log.d(TAG, "SMS recibido de: " + sender);

                    if (isMonitored(sender)) {
                        Toast.makeText(context, "ALERTA SMS de número monitoreado: " + sender, Toast.LENGTH_LONG).show();
                        sendNotification(context, sender, messageBody);
                    }
                }
            }
        }
    }

    private boolean isMonitored(String sender) {
        for (String monitored : MONITORED_NUMBERS) {
            if (sender.contains(monitored)) {
                return true;
            }
        }
        return false;
    }


    private void sendNotification(Context context, String sender, String body) {
        createNotificationChannel(context);

        int notificationId = sender.hashCode();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("🚨 ALERTA - Mensaje Monitoreado")
                .setContentText("De: " + sender + ". Contenido: " + body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(ContextCompat.getColor(context, R.color.alert_danger))
                .setStyle(new NotificationCompat.BigTextStyle().bigText("De: " + sender + "\nContenido: " + body))
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                Log.e(TAG, "Permiso POST_NOTIFICATIONS denegado. No se puede mostrar la alerta.");
                return;
            }
        }

        notificationManager.notify(notificationId, builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Alertas SMS Monitoreadas";
            String description = "Notificaciones de alta prioridad para contactos vigilados.";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
