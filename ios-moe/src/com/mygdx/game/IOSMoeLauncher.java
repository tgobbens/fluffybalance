package com.mygdx.game;

import com.badlogic.gdx.backends.iosmoe.IOSApplication;
import com.badlogic.gdx.backends.iosmoe.IOSApplicationConfiguration;
import com.balanceball.BalanceBallSec;
import com.balanceball.Balanceball;
import com.balanceball.ShowAdmobListener;

import org.moe.binding.googlemobileads.GADBannerView;
import org.moe.binding.googlemobileads.GADRequest;
import org.moe.binding.googlemobileads.c.GoogleMobileAds;
import org.moe.binding.googlemobileads.protocol.GADBannerViewDelegate;
import org.moe.natj.general.Pointer;

import apple.coregraphics.struct.CGPoint;
import apple.coregraphics.struct.CGRect;
import apple.coregraphics.struct.CGSize;
import apple.uikit.UIScreen;
import apple.uikit.c.UIKit;

public class IOSMoeLauncher extends IOSApplication.Delegate implements ShowAdmobListener {

    private static final String TAG = IOSMoeLauncher.class.getSimpleName();

    private boolean mAdsInitialized = false;

    // production keys
    private static final String APP_ID = "ca-app-pub-6709098867177017~4029109081";
    //private static final String AD_UNIT_ID = "ca-app-pub-6709098867177017/5505842289"; // PRODUCTION
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"; // TESTING


    private GADBannerView mAdview;
    private IOSApplication mIosApplication;

    protected IOSMoeLauncher(Pointer peer) {
        super(peer);
    }

    @Override
    protected IOSApplication createApplication() {
        IOSApplicationConfiguration config = new IOSApplicationConfiguration();
        config.useCompass = true;
        config.useAccelerometer = true;

        mIosApplication = new IOSApplication(new BalanceBallSec(this), config);

        return mIosApplication;
    }

    public static void main(String[] argv) {
        UIKit.UIApplicationMain(0, null, null, IOSMoeLauncher.class.getName());
    }

    private void initializeAds() {
        if (mAdsInitialized) {
            return;
        }

        mAdsInitialized = true;

        mAdview = GADBannerView.alloc().initWithAdSize(GoogleMobileAds.kGADAdSizeLargeBanner());
        mAdview.setAdUnitID(AD_UNIT_ID);
        mAdview.setRootViewController(mIosApplication.getUIViewController());
        mIosApplication.getUIViewController().view().addSubview(mAdview);

        final GADRequest request = GADRequest.request();

        mAdview.setDelegate(new GADBannerViewDelegate() {
            @Override
            public void adViewDidReceiveAd(GADBannerView gadBannerView) {
                mAdview.setHidden(false);
            }
        });

        mAdview.loadRequest(request);
    }

    public void show() {
        initializeAds();

        final CGSize screenSize = UIScreen.mainScreen().bounds().size();
        double screenWidth = screenSize.width();

        final CGSize adSize = mAdview.bounds().size();
        double adWidth = adSize.width();
        double adHeight = adSize.height();

        float bannerWidth = (float) screenWidth;
        float bannerHeight = (float) (bannerWidth / adWidth * adHeight);

        mAdview.setFrame(new CGRect(
                new CGPoint((screenWidth / 2) - adWidth / 2, screenSize.height() - adHeight),
                new CGSize(bannerWidth, bannerHeight)));
    }

    @Override
    public void showAd() {
        show();
    }
}
