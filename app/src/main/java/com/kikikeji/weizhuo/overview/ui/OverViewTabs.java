package com.kikikeji.weizhuo.overview.ui;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kikikeji.weizhuo.DragController;
import com.kikikeji.weizhuo.Launcher;
import com.kikikeji.weizhuo.LauncherModel;
import com.kikikeji.weizhuo.MuchAppsCustomizePagedView;
import com.kikikeji.weizhuo.MuchAppsCustomizeTabHost;
import com.kikikeji.weizhuo.R;

import java.util.ArrayList;
import java.util.List;


public class OverViewTabs implements InitPage {

    private List<View> mListViews = new ArrayList<View>();
    private SparseArray<TextView> mTitleMap = new SparseArray<TextView>();
    private Context mContext;
    private static final int EFFECT_INDEX = 0;
    private static final int WALLPAPER_INDEX = 1;
    private static final int WIDGET_INDEX = 2;
    private LinearLayout linearLayout;
    private TextView mTitleEffect, mTitleWallPaper, mTitleWidget;
    private ImageView mNavIndicator;// 动画图片
    private FrameLayout mContainer;
    private WallpaperLayout mWallpaperContent;
    private UnderlinesNoFadeLayout mEffectContent;
    private MuchAppsCustomizeTabHost mAppsCustomizeTabHost;
    private MuchAppsCustomizePagedView mAppsCustomizeContent;
    private int mCurrIndex = 0;// 当前页卡编号
    private static final int TITLE_COUNT = 3;
    private DragController mDragController;
    LayoutAnimationController mAnimController;

    // private Context mContext;
    public void init(Context context, View view, DragController dragController) {
        mContext = context;
        mDragController = dragController;
        InitTextView(view);
        InitContainerView(context, view);
        initAnimationController();
    }

    private LayoutAnimationController initAnimationController() {
        Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.overview_item_load);
        mAnimController = new LayoutAnimationController(anim, 0f);
        mAnimController.setOrder(LayoutAnimationController.ORDER_NORMAL);
        return mAnimController;
    }

    private void InitContainerView(Context context, View view) {
        mContainer = (FrameLayout) view.findViewById(R.id.container);
        mContainer.removeAllViewsInLayout();
        mListViews.clear();
        loadContentByIndex(0);
    }

    /**
     * 初始化头标
     */

    private void InitTextView(View view) {
        //LinearLayout layout = (LinearLayout) view.findViewById(R.id.nav_layout);
        mTitleMap.clear();
        mTitleEffect = (TextView) view.findViewById(R.id.title_effect);
        mTitleWallPaper = (TextView) view.findViewById(R.id.title_wrapper);
        mTitleWidget = (TextView) view.findViewById(R.id.title_widget);
        linearLayout = (LinearLayout) view.findViewById(R.id.nav_layout);
        mTitleMap.put(EFFECT_INDEX, mTitleEffect);
        mTitleMap.put(WALLPAPER_INDEX, mTitleWallPaper);
        mTitleMap.put(WIDGET_INDEX, mTitleWidget);
        mNavIndicator = (ImageView) view.findViewById(R.id.nav_indicator);
        LayoutParams params = mNavIndicator.getLayoutParams();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(getScreenWidth(), LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(layoutParams);
        int marginLeft = mContext.getResources()
                .getDimensionPixelOffset(R.dimen.much_overmode_function_nav_margin_left);
        int marginRight = mContext.getResources().getDimensionPixelOffset(
                R.dimen.much_overmode_function_nav_margin_right);
        //layoutParams.width=(getScreenWidth() - marginLeft - marginRight) / TITLE_COUNT;

        params.width = getScreenWidth() / TITLE_COUNT;

        mTitleEffect.setOnClickListener(new MyOnClickListener(EFFECT_INDEX));
        mTitleWallPaper.setOnClickListener(new MyOnClickListener(WALLPAPER_INDEX));
        mTitleWidget.setOnClickListener(new MyOnClickListener(WIDGET_INDEX));
    }

    //获取屏幕宽度
    private int getScreenWidth() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;// 获取分辨率宽度
    }

    private class MyOnClickListener implements OnClickListener {
        private int index = 0;

        public MyOnClickListener(int i) {
            index = i;
        }

        public void onClick(View v) {
            if (mCurrIndex == index) {
                return;
            }
            loadContentByIndex(index);
            int titleWidth = mTitleEffect.getWidth();
            Animation animation = new TranslateAnimation(titleWidth * mCurrIndex, titleWidth * index, 0, 0);
            mCurrIndex = index;
            animation.setFillAfter(true);// True:图片停在动画结束位置
            animation.setDuration(300);
            mNavIndicator.startAnimation(animation);
        }

    }

    /**
     * 点击指示器加载对应内容
     *
     * @param index
     */
    private void loadContentByIndex(int index) {
        mContainer.removeAllViewsInLayout();
        if (index == EFFECT_INDEX) {
            // mEffectContent = new UnderlinesNoFadeLayout(mContext, getAnimationController());
            mEffectContent = UnderlinesNoFadeLayout.getUnderLinesSingleInstance(mContext);
            if (mEffectContent != null) {
                mEffectContent.initPage(0);
                mEffectContent.setmAnimController(getAnimationController());
                mEffectContent.init(mContext);
                mEffectContent.setIsFirstDone(true);
                ViewGroup parent = (ViewGroup) mEffectContent.getParent();
                if (parent != null) {
                    parent.removeAllViews();
                }
                mContainer.addView(mEffectContent);
            }
        } else if (index == WALLPAPER_INDEX) {
            //  mWallPaperContent = new WallpaperLayout(mContext, getAnimationController());
            mWallpaperContent = WallpaperLayout.getUnderLinesSingleInstance(mContext);
            if (mWallpaperContent != null) {
                mWallpaperContent.initPage(0);
                mWallpaperContent.setmAnimController(getAnimationController());
                mWallpaperContent.init(mContext);
                ViewGroup parent = (ViewGroup) mWallpaperContent.getParent();
                if (parent != null) {
                    parent.removeAllViews();
                }
                mContainer.addView(mWallpaperContent);
            }
        } else if (index == WIDGET_INDEX) {
            if (mAppsCustomizeContent == null) {
                // Setup AppsCustomize
                mAppsCustomizeTabHost = (MuchAppsCustomizeTabHost) LayoutInflater.from(mContext).inflate(R.layout.snail_apps_customize_pane, null);
                mAppsCustomizeContent = (MuchAppsCustomizePagedView)
                        mAppsCustomizeTabHost.findViewById(R.id.apps_customize_pane_content);
                mAppsCustomizeContent.setup((Launcher) mContext, mDragController);
            }
            mAppsCustomizeTabHost.reset();
            if (mAppsCustomizeContent != null) {
                mAppsCustomizeContent.onPackagesUpdated(
                        LauncherModel.getSortedWidgetsAndShortcuts(mContext));
            }
            mAppsCustomizeTabHost.setContentTypeImmediate(MuchAppsCustomizePagedView.ContentType.Widgets);
            Launcher launcher = (Launcher) mContext;
            /*launcher.dispatchOnLauncherTransitionPrepare(mAppsCustomizeTabHost, true, false);
            launcher.dispatchOnLauncherTransitionStart(mAppsCustomizeTabHost, true, false);
            launcher.dispatchOnLauncherTransitionEnd(mAppsCustomizeTabHost, true, false);*/
            //每次点击加载第一页
            mAppsCustomizeTabHost.initPage(0);
            ViewGroup parent = (ViewGroup) mAppsCustomizeTabHost.getParent();
            if (parent != null) {
                parent.removeAllViews();

            }
            mContainer.addView(mAppsCustomizeTabHost);
        }
        setSeletorTitleAlpha(index);
    }

    private LayoutAnimationController getAnimationController() {
        if (mAnimController == null) {
            mAnimController = initAnimationController();
        }
        return mAnimController;
    }

    private void setSeletorTitleAlpha(int selectedIndex) {
        for (int i = 0; i < mTitleMap.size(); i++) {
            if (mTitleMap.keyAt(i) == selectedIndex) {
                TextView selectedView = mTitleMap.get(i);
                selectedView.setAlpha(1f);
                continue;
            }
            if (mTitleMap.keyAt(i) == mCurrIndex) {
                TextView unSelectedView = mTitleMap.get(i);
                unSelectedView.setAlpha(0.6f);
            }
        }
    }

    @Override
    public void initPage(int pageIndex) {
        if (pageIndex == 0) {
            mCurrIndex = 0;
            mNavIndicator.clearAnimation();
            loadContentByIndex(pageIndex);
            resetTitleAlpha();
        }
    }

    private void resetTitleAlpha() {
        for (int i = 0; i < mTitleMap.size(); i++) {
            if (i == 0) {
                TextView selectedView = mTitleMap.get(i);
                selectedView.setAlpha(1f);
                continue;
            }
            TextView unSelectedView = mTitleMap.get(i);
            unSelectedView.setAlpha(0.6f);
        }
    }

    public void onDestory() {
        mContainer.removeAllViewsInLayout();
        mContainer = null;
        mListViews.clear();
        mTitleMap.clear();
        mContext = null;
    }
}
