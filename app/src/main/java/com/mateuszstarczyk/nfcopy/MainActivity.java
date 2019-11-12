package com.mateuszstarczyk.nfcopy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.mateuszstarczyk.nfcopy.nfc.hce.DaemonConfiguration;
import com.mateuszstarczyk.nfcopy.service.nfc.db.TinyDB;

import tud.seemuh.nfcgate.xposed.IPCBroadcastReceiver;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private IntentFilter mIntentFilter = new IntentFilter();
    private BroadcastReceiver broadcastoastReceiver;
    private IPCBroadcastReceiver ipcBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_cards, R.id.nav_new_card,
                R.id.nav_settings, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
        TinyDB tinydb = new TinyDB(this);
        tinydb.remove("last_deleted");
        tinydb.remove("last_deleted_index");
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        DaemonConfiguration.Init(this);

        broadcastoastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(context, intent.getStringExtra("text"), Toast.LENGTH_LONG).show();
            }
        };

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        registerReceiver(broadcastoastReceiver, new IntentFilter("com.mateuszstarczyk.nfcopy.toaster"));
        ipcBroadcastReceiver = new IPCBroadcastReceiver(this);
        mIntentFilter.addAction(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TinyDB tinydb = new TinyDB(this);
        tinydb.remove("last_deleted");
        tinydb.remove("last_deleted_index");
        unregisterReceiver(broadcastoastReceiver);
        unregisterReceiver(ipcBroadcastReceiver);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
