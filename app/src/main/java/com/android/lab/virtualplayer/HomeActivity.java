package com.android.lab.virtualplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.android.lab.virtualplayer.client.UserActivity;
import com.android.lab.virtualplayer.server.ShareActivity;

public class HomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = this.getMenuInflater();
//        inflater.inflate(R.menu.menu_main, menu);
//        return true;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//
//        switch (item.getItemId()) {
//            case R.id.action_settings:
//                startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
//        }

        return super.onOptionsItemSelected(item);
    }

    public void sharer(View v) {
        startActivity(new Intent(HomeActivity.this, ShareActivity.class));
    }

    public void user(View v) {
        startActivity(new Intent(HomeActivity.this, UserActivity.class));
    }
}
