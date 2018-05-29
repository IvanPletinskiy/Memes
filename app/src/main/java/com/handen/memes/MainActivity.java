package com.handen.memes;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.handen.memes.database.Database;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    MenuItem prevMenuItem;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for(int i = 0; i < 20; i ++) {
            Group group = new Group(i, "ГРУППА № " + i, i % 2 == 0);
            Database.get().addGroup(group);
        }

        ArrayList<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(GroupListFragment.newInstance());
        fragments.add(PostListFragment.newInstance());
        fragments.add(PostListFragment.newInstance());

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
}
