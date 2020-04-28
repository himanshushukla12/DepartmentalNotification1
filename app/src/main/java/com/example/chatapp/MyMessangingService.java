package com.example.chatapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MyMessangingService extends Service {
    public MyMessangingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
