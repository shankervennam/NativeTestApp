package com.example.rvennam.nativetestapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeAppInstallAdView;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.android.gms.ads.formats.NativeContentAdView;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
    private static final String ADMOB_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110";
    private static final String ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713";

    private Button mRefresh;
    private TextView mVideoStatus;
    private CheckBox mStartVideoAdsMuted;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, ADMOB_APP_ID);

        mRefresh = findViewById(R.id.btn_refresh);
        final CheckBox mAppInstall = findViewById(R.id.chk_app_install);
        final CheckBox mContentInstall = findViewById(R.id.chk_content_install);
        mStartVideoAdsMuted = findViewById(R.id.chk_start_muted);
        mVideoStatus = findViewById(R.id.tv_video_status);

        mRefresh.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                refreshAd(mAppInstall.isChecked(), mContentInstall.isChecked());
            }
        });

        refreshAd(mAppInstall.isChecked(), mContentInstall.isChecked());
    }

    private void refreshAd(boolean requestAppAds,boolean requestContentAds)
    {
        if(!requestAppAds && !requestContentAds)
        {
            Toast.makeText(this, "Select any one ad format",Toast.LENGTH_LONG).show();
            return;
        }
        mRefresh.setEnabled(false);

        AdLoader.Builder builder = new AdLoader.Builder(this, ADMOB_AD_UNIT_ID);

        if(requestAppAds)
        {
            builder.forAppInstallAd(new NativeAppInstallAd.OnAppInstallAdLoadedListener()
            {
                @Override
                public void onAppInstallAdLoaded(NativeAppInstallAd nativeAppInstallAd)
                {
                    FrameLayout frameLayout = findViewById(R.id.frame_layout);
                    NativeAppInstallAdView adView = (NativeAppInstallAdView) getLayoutInflater().inflate(R.layout.native_app_view, null);
                    populateAddInstallAdView(nativeAppInstallAd, adView);
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                }
            });
        }

        if(requestContentAds)
        {
            builder.forContentAd(new NativeContentAd.OnContentAdLoadedListener()
            {
                @Override
                public void onContentAdLoaded(NativeContentAd nativeContentAd)
                {
                    FrameLayout frameLayout = findViewById(R.id.frame_layout);
                    NativeContentAdView adView = (NativeContentAdView) getLayoutInflater().inflate(R.layout.native_content_view, null);
                    populateContentAdView(nativeContentAd, adView);
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                }
            });
        }

        VideoOptions videoOptions = new VideoOptions.Builder().setStartMuted(mStartVideoAdsMuted.isChecked()).build();

        NativeAdOptions nativeAd = new NativeAdOptions.Builder().setVideoOptions(videoOptions).build();

        builder.withNativeAdOptions(nativeAd);

        final AdLoader adLoader = builder.withAdListener(new AdListener()
        {
            @Override
            public void onAdFailedToLoad(int i)
            {
                mRefresh.setEnabled(true);
                Toast.makeText(MainActivity.this, "Failed to load  native Ad", Toast.LENGTH_LONG).show();
                super.onAdFailedToLoad(i);
            }
        }).build();

        Bundle extras = new Bundle();
        extras.putBoolean("_emulatorLiveAds", true);
        adLoader.loadAd(new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, extras).build());
    }

    private void populateAddInstallAdView(NativeAppInstallAd nativeAppInstallAd, NativeAppInstallAdView adView)
    {
        VideoController vc = nativeAppInstallAd.getVideoController();

        vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks()
        {
            @Override
            public void onVideoEnd()
            {
                mRefresh.setEnabled(true);
                mVideoStatus.setText("Video has ended");
                super.onVideoEnd();
            }
        });

        adView.setHeadlineView(adView.findViewById(R.id.app_install_headline));
        adView.setBodyView(adView.findViewById(R.id.app_install_body));
        adView.setCallToActionView(adView.findViewById(R.id.app_install_cta));
        adView.setIconView(adView.findViewById(R.id.app_icon));
        adView.setPriceView(adView.findViewById(R.id.app_install_price));
        adView.setStarRatingView(adView.findViewById(R.id.app_stars));
        adView.setStoreView(adView.findViewById(R.id.app_install_store));

        MediaView mediaView = adView.findViewById(R.id.app_install_media);
        adView.setMediaView(mediaView);

        ((TextView) adView.getHeadlineView()).setText(nativeAppInstallAd.getHeadline());
        ((TextView)adView.getBodyView()).setText(nativeAppInstallAd.getBody());
        ((ImageView)adView.getIconView()).setImageDrawable(nativeAppInstallAd.getIcon().getDrawable());
        ((Button) adView.getCallToActionView()).setText(nativeAppInstallAd.getCallToAction());

        if(vc.hasVideoContent())
        {
            mVideoStatus.setText(String.format(Locale.getDefault(), "Video Status:Ad contains a %.2f:1 video asset.", vc.getAspectRatio()));
        }
        else
        {
            mRefresh.setEnabled(true);
            mVideoStatus.setText("Video status doesn't contain a video asset");
        }

        if(nativeAppInstallAd.getPrice() == null)
        {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        }
        else
        {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAppInstallAd.getPrice());
        }

        if(nativeAppInstallAd.getStore() == null)
        {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        }
        else
        {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView)adView.getStoreView()).setText(nativeAppInstallAd.getStore());
        }

        if(nativeAppInstallAd.getStarRating() == null)
        {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        }
        else
        {
            ((RatingBar)adView.getStarRatingView()).setRating(nativeAppInstallAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }
        adView.setNativeAd(nativeAppInstallAd);
    }

    private void populateContentAdView(NativeContentAd nativeContentAd, NativeContentAdView adView)
    {
        mVideoStatus.setText("Video status:Ad doen't contain a video asset");
        mRefresh.setEnabled(true);

        adView.setHeadlineView(adView.findViewById(R.id.content_headline));
        adView.setImageView(adView.findViewById(R.id.content_image));
        adView.setBodyView(adView.findViewById(R.id.content_body));
        adView.setCallToActionView(adView.findViewById(R.id.content_call_to_action));
        adView.setLogoView(adView.findViewById(R.id.content_logo));
        adView.setAdvertiserView(adView.findViewById(R.id.content_advertiser));

        ((TextView) adView.getHeadlineView()).setText(nativeContentAd.getHeadline());
        ((TextView)adView.getBodyView()).setText(nativeContentAd.getBody());
        ((TextView)adView.getAdvertiserView()).setText(nativeContentAd.getAdvertiser());
        ((TextView) adView.getCallToActionView()).setText(nativeContentAd.getCallToAction());

        List<NativeAd.Image> images = nativeContentAd.getImages();

        if(images.size() > 0)
        {
            ((ImageView) adView.getImageView()).setImageDrawable(images.get(0).getDrawable());
        }

        NativeAd.Image logoImage = nativeContentAd.getLogo();

        if(logoImage == null)
        {
            adView.getLogoView().setVisibility(View.INVISIBLE);
        }
        else
        {
            ((ImageView)adView.getLogoView()).setImageDrawable(logoImage.getDrawable());
            adView.getLogoView().setVisibility(View.VISIBLE);
        }

        adView.setNativeAd(nativeContentAd);
    }
}
