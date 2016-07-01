package com.robsterthelobster.ucibustracker;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.SupportMapFragment;

public class DetailActivity extends AppCompatActivity {

    private final String TAG = DetailActivity.class.getSimpleName();

    private MyAdapter mAdapter;
    private ViewPager mPager;
    private Toolbar toolbar;
    static String routeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        routeName = getIntent().getExtras().getString(Constants.INTENT_KEY);

        toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle(routeName);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mAdapter = new MyAdapter(getSupportFragmentManager(), this);

        mPager = (ViewPager) findViewById(R.id.pager_detail);
        mPager.setAdapter(mAdapter);

        // This is required to avoid a black flash when the map is loaded.  The flash is due
        // to the use of a SurfaceView as the underlying view of the map.
        mPager.requestTransparentRegion(mPager);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mPager);
    }

    public static class MyAdapter extends FragmentPagerAdapter {

        private final int PAGE_COUNT = 2;
        private final String TAG = MyAdapter.class.getSimpleName();
        private Context mContext;

        public MyAdapter(FragmentManager fm, Context context) {
            super(fm);
            mContext = context;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.BUNDLE_KEY, routeName);

                    ArrivalsFragment fragment = new ArrivalsFragment();
                    fragment.setArguments(bundle);

                    return fragment;
                case 1:
                    return new MapFragment();
                default:
                    Log.e(TAG, "Pager position does not exist");
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch(position){
                case 0:
                    return mContext.getString(R.string.route_stops_tab);
                case 1:
                    return mContext.getString(R.string.route_map_tab);
                default:
                    Log.e(TAG, "Pager position does not exist");
                    return null;
            }
        }
    }
}
