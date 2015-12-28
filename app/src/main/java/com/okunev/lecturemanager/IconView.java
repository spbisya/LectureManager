package com.okunev.lecturemanager;

/**
 * Created by 777 on 12/12/2015.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class IconView extends LinearLayout implements Checkable {
    private RoundedImageView circleImageView;
    private ImageView mIcon;
    private TextView mFileName;
    private Bitmap thumbs;
    private boolean mChecked;

    public IconView(Context context) {
        super(context, null);
    }

    public IconView(Context context, int iconResId, String fileName) {
        super(context);

        this.setOrientation(VERTICAL);
        this.setPadding(3, 3, 3, 3);
        this.setGravity(Gravity.CENTER_HORIZONTAL);

        circleImageView = new RoundedImageView(context);
        circleImageView.setScaleType(CircleImageView.ScaleType.CENTER_CROP);
        circleImageView.setImageResource(iconResId);
        circleImageView.setCornerRadius(5.0f);
        addView(circleImageView, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

        mFileName = new TextView(context);
        mFileName.setSingleLine();
        mFileName.setEllipsize(TextUtils.TruncateAt.END);
        mFileName.setText(fileName);
        addView(mFileName, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }

    public IconView(Context context, int iconResId, String fileName, Boolean dir) {
        super(context);

        this.setOrientation(VERTICAL);
        this.setPadding(3, 3, 3, 3);
        this.setGravity(Gravity.CENTER_HORIZONTAL);

        mIcon = new ImageView(context);
        mIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mIcon.setImageResource(iconResId);
        addView(mIcon, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

        mFileName = new TextView(context);
        mFileName.setSingleLine();
        mFileName.setEllipsize(TextUtils.TruncateAt.END);
        mFileName.setText(fileName);
        addView(mFileName, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }


    public IconView(Context context, Bitmap iconResId, String fileName) {
        super(context);

        this.setOrientation(VERTICAL);
        this.setPadding(3, 3, 3, 3);
        this.setGravity(Gravity.CENTER_HORIZONTAL);

        circleImageView = new RoundedImageView(context);
        circleImageView.setScaleType(CircleImageView.ScaleType.CENTER_CROP);
        circleImageView.setImageBitmap(iconResId);
        circleImageView.setCornerRadius(5.0f);
        addView(circleImageView, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

        mFileName = new TextView(context);
        mFileName.setSingleLine();
        mFileName.setEllipsize(TextUtils.TruncateAt.END);
        mFileName.setText(fileName);
        addView(mFileName, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void setChecked(boolean checked) {
        mChecked = checked;
         setBackgroundColor(checked ? Color.parseColor("#C0C0C0") :    Color.parseColor("#FFFFFF"));
        //mFileName.setTextColor(checked ? Color.CYAN : Color.parseColor("#347ac8"));
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    public void setThumbs(Bitmap thumbs) {
        circleImageView.setImageBitmap(thumbs);
    }

    public void select() {
        mFileName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
    }

    public void deselect() {
        mFileName.setEllipsize(TextUtils.TruncateAt.END);
    }

    public void setIconResId(int resId) {
        circleImageView.setImageResource(resId);
    }

    public void setFileName(String fileName) {
        mFileName.setText(fileName);
    }
}
