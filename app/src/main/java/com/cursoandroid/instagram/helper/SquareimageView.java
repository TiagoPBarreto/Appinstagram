package com.cursoandroid.instagram.helper;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatImageView;

public class SquareimageView extends AppCompatImageView {

    public SquareimageView(Context context) {
        super(context);
    }

    public SquareimageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareimageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        setMeasuredDimension(width, width);
    }

}
