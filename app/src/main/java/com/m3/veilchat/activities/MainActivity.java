package com.m3.veilchat.activities;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.m3.veilchat.R;
import com.m3.veilchat.fragments.HomeFragment;
import com.m3.veilchat.fragments.ChatsFragment;
import com.m3.veilchat.fragments.PersonasFragment;
import com.m3.veilchat.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(this::onNavItemSelected);

        // default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private boolean onNavItemSelected(@NonNull MenuItem item) {
        Fragment f;
        int itemId = item.getItemId();
        if (itemId == R.id.nav_chats) {
            f = new ChatsFragment();
        } else if (itemId == R.id.nav_personas) {
            f = new PersonasFragment();
        } else if (itemId == R.id.nav_profile) {
            f = new ProfileFragment();
        } else {
            f = new HomeFragment();
        }
        loadFragment(f);
        return true;
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commitAllowingStateLoss(); // Use commitAllowingStateLoss to avoid crash on state loss
    }
}
