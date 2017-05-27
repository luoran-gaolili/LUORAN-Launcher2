package com.kikikeji.weizhuo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class CellLayoutCreator extends CellLayout {

	private Rect mRect = new Rect();

	public CellLayoutCreator(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public CellLayoutCreator(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public CellLayoutCreator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		//绘制添加页面图标
		Drawable addNewScreenDrawable = getResources().getDrawable(R.drawable.much_preview_addscreen);
		int drawableHeight = addNewScreenDrawable.getIntrinsicHeight();
		int drawableWidght = addNewScreenDrawable.getIntrinsicWidth();
		int left = (getMeasuredWidth() - drawableWidght) / 2;
		int top = (getMeasuredHeight() - drawableHeight) / 2;
		//绘制可点击区域  计算坐标
		mRect.set(left, top, left + drawableWidght, top + drawableHeight);
		//drawable绘制在Rect矩形区域内
		addNewScreenDrawable.setBounds(mRect);
		addNewScreenDrawable.draw(canvas);
	}
}
