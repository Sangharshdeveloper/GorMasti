package com.masti.shortvideoapp.ActivitesFragment.Profile;

import com.masti.shortvideoapp.SimpleClasses.AppCompatLocaleActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.tabs.TabLayout;
import com.masti.shortvideoapp.ActivitesFragment.Profile.Favourite.FavouriteVideosF;
import com.masti.shortvideoapp.ActivitesFragment.Search.SearchHashTagsF;
import com.masti.shortvideoapp.ActivitesFragment.SoundLists.FavouriteSoundF;
import com.masti.shortvideoapp.Adapters.ViewPagerAdapter;
import com.masti.shortvideoapp.R;
import com.masti.shortvideoapp.SimpleClasses.Functions;
import com.masti.shortvideoapp.SimpleClasses.Variables;

public class FavouriteMainA extends AppCompatLocaleActivity {

    Context context;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(FavouriteMainA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, FavouriteMainA.class,false);
        setContentView(R.layout.activity_favourite_main_);
        context = FavouriteMainA.this;

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FavouriteMainA.super.onBackPressed();
            }
        });

        Set_tabs();
    }


    // set up the favourite video sound and hastage fragment
    protected TabLayout tabLayout;
    protected ViewPager menuPager;
    ViewPagerAdapter adapter;

    public void Set_tabs() {

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        menuPager = (ViewPager) findViewById(R.id.viewpager);
        menuPager.setOffscreenPageLimit(3);
        tabLayout = (TabLayout) findViewById(R.id.tabs);


        adapter.addFrag(new FavouriteVideosF(), getString(R.string.videos));
        adapter.addFrag(new FavouriteSoundF(), getString(R.string.sounds));
        adapter.addFrag(new SearchHashTagsF("favourite"), getString(R.string.hashtag));


        menuPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(menuPager);

    }



}
