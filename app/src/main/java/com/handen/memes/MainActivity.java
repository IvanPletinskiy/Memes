package com.handen.memes;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.handen.memes.database.Database;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    MenuItem prevMenuItem;
    BottomNavigationView bottomNavigationView;
    Preferences preferences = new Preferences();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!VKSdk.isLoggedIn())
            VKSdk.login(MainActivity.this, null);

        if(Database.get().getGroupsNames().size() == 0) {
            String[] defaultArray = getResources().getStringArray(R.array.defaultGroups);

            for(String s : defaultArray) {
                Group group = new Group(
                        Integer.parseInt(s.split(";")[0]),
                        s.split(";")[1],
                        true);
                Database.get().addGroup(group);
            }
            //for(int i = 0; i < 20; i ++) {
             //   Group group = new Group(i, "ГРУППА № " + i, i % 2 == 0);
           //     Database.get().addGroup(group);
            //}
        }

        ArrayList<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(GroupListFragment.newInstance());
        fragments.add(PostListFragment.newInstance());
        fragments.add(FavoritesFragment.newInstance());

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);

        viewPager = findViewById(R.id.viewPager);
        PagerAdapter adapter = new com.handen.memes.PagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                }
                else {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }

                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                prevMenuItem = bottomNavigationView.getMenu().getItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewPager.setCurrentItem(1);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.groups:
                                viewPager.setCurrentItem(0);
                                break;
                            case R.id.posts:
                                viewPager.setCurrentItem(1);
                                break;
                            case R.id.favorites:
                                viewPager.setCurrentItem(2);
                                break;
                        }
                        return false;
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // Пользователь успешно авторизовался
            }
            @Override
            public void onError(VKError error) {
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
      //  Database.savePosts(PostDownloader.getPostsPool());
    }
}
