package com.azure.mobile.azuredataandroidexample_java.Controllers;

import android.app.Application;

import com.azure.data.AzureData;
import com.azure.data.model.PermissionMode;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.distribute.Distribute;

import static com.azure.data.util.FunctionalUtils.onCallback;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AppCenter.start(this, "49370b39-3b0d-40d7-92fe-4e7bc3c45b1f", Distribute.class);
        // configure AzureData - fill in your account name and master key
        AzureData.configure(getApplicationContext(), "", "", PermissionMode.All, onCallback(builder -> {

        }));
    }
}