package com.rgks.launcher3.overview.ui;

/**
 * Created by ran.luo on 2017/6/27.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.rgks.launcher3.R;

public class IndicatorView extends LinearLayout implements ViewPager.OnPageChangeListener {

    private int childViewCount = 0;

    private int mInterval;

    private int mCurrentPostion = 0;

    private Bitmap normalBp;

    private Bitmap selectBp;

    private ViewPager mViewPager;

    private int mWidth;

    private int mHeight;

    private int mRadius;

    private int normalColor;

    private int selectColor;


    interface OnPageChangeListener {

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

        public void onPageSelected(int position);

        public void onPageScrollStateChanged(int state);

    }


    private OnPageChangeListener mListener;

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.mListener = listener;
    }

    public IndicatorView(Context context) {
        this(context, null);
    }

    public IndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.IndicatorView);
        normalBp = drawableToBitamp(ta.getDrawable(R.styleable.IndicatorView_normalDrawable));
        selectBp = drawableToBitamp(ta.getDrawable(R.styleable.IndicatorView_selectDrawable));
        mInterval = ta.getDimensionPixelOffset(R.styleable.IndicatorView_indicatorInterval, 6);
        normalColor = ta.getColor(R.styleable.IndicatorView_normalColor, Color.GRAY);
        selectColor = ta.getColor(R.styleable.IndicatorView_selectColor, Color.WHITE);
        mRadius = context.getResources().getDimensionPixelSize(R.dimen.rgk_indicator_height);
        ta.recycle();
        init();

    }

    private void init() {
        if (null == normalBp) {
            normalBp = makeIndicatorBp(normalColor);
        }
        if (null == selectBp) {
            selectBp = makeIndicatorBp(selectColor);
        }
        mWidth = normalBp.getWidth();
        mHeight = normalBp.getWidth();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension((widthMode == MeasureSpec.EXACTLY) ? sizeWidth : (mWidth + mInterval) * childViewCount
                , (heightMode == MeasureSpec.EXACTLY) ? sizeHeight
                        : mHeight);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (getChildCount() < childViewCount && getChildCount() == 0) {
            for (int i = 0; i < childViewCount; i++) {
                addView(makeIndicatorItem());
            }
            setIndicatorState(mCurrentPostion);
        }
        super.dispatchDraw(canvas);
    }


    public void setViewPager(ViewPager viewpager) {
        if (null == viewpager) {
            return;
        }
        if (null == viewpager.getAdapter()) {
            throw new IllegalStateException("ViewPager does not have adapter.");
        }

        this.mViewPager = viewpager;
        this.mViewPager.addOnPageChangeListener(this);
        this.childViewCount = viewpager.getAdapter().getCount();
        invalidate();
    }

    public void setViewPager(ViewPager viewpager, int currposition) {
        if (null == viewpager) {
            return;
        }
        if (null == viewpager.getAdapter()) {
            throw new IllegalStateException("ViewPager does not have adapter.");
        }
        this.mViewPager = viewpager;
        this.mViewPager.addOnPageChangeListener(this);
        this.childViewCount = viewpager.getAdapter().getCount();
        this.mCurrentPostion = currposition;
        invalidate();
    }

    private View makeIndicatorItem() {
        ImageView iv = new ImageView(getContext());
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.width = normalBp.getWidth();
        lp.height = normalBp.getHeight();
        lp.rightMargin = mInterval;
        iv.setImageBitmap(normalBp);
        iv.setLayoutParams(lp);
        return iv;
    }

    private Bitmap makeIndicatorBp(int color) {
        Bitmap normalBp = Bitmap.createBitmap(mRadius * 2, mRadius * 2,
                Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        Canvas canvas = new Canvas(normalBp);
        canvas.drawCircle(mRadius, mRadius, mRadius, paint);
        return normalBp;
    }

    private Bitmap drawableToBitamp(Drawable drawable) {
        if (null == drawable) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) drawable;
            return bd.getBitmap();
        }
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (null != mListener) {
            mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }

    @Override
    public void onPageSelected(int position) {
        setIndicatorState(position);
        if (null != mListener) {
            mListener.onPageSelected(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (null != mListener) {
            mListener.onPageScrollStateChanged(state);
        }
    }


    public void setIndicatorState(int position) {
        for (int i = 0; i < getChildCount(); i++) {
            if (i == position)
                ((ImageView) getChildAt(i)).setImageBitmap(selectBp);
            else
                ((ImageView) getChildAt(i)).setImageBitmap(normalBp);
        }
    }

}
