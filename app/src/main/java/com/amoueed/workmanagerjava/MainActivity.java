package com.amoueed.workmanagerjava;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.button);
        Button cancelButton = findViewById(R.id.cancelButton);

        if(isWorkScheduled("sendData")){
            Toast.makeText(MainActivity.this,"true",Toast.LENGTH_LONG).show();
        }else{
            //Work Manager periodicWorkRequest activated on Button click
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Work Manager Constraints
                    Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
                    //Work Manager periodicWorkRequest setup
                    PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest
                            .Builder(UploadWorker.class, 15, TimeUnit.MINUTES)
                            //.setConstraints(constraints)
                            .addTag("sendData")
                            .build();

                    WorkManager.getInstance(MainActivity.this).enqueue(periodicWorkRequest);
                    Toast.makeText(MainActivity.this,""+isWorkScheduled("sendData"),Toast.LENGTH_LONG).show();
                }
            });
            Toast.makeText(MainActivity.this,"false",Toast.LENGTH_LONG).show();
        }

        //Work Manager periodicWorkRequest deactivated on Button click
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WorkManager.getInstance(MainActivity.this).cancelAllWorkByTag("sendData");
                Toast.makeText(MainActivity.this,""+isWorkScheduled("sendData"),Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean isWorkScheduled(String tag) {
        WorkManager instance = WorkManager.getInstance(MainActivity.this);
        ListenableFuture<List<WorkInfo>> statuses = instance.getWorkInfosByTag(tag);
        try {
            boolean running = false;
            List<WorkInfo> workInfoList = statuses.get();
            for (WorkInfo workInfo : workInfoList) {
                WorkInfo.State state = workInfo.getState();
                running = state == WorkInfo.State.RUNNING | state == WorkInfo.State.ENQUEUED;
            }
            return running;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}