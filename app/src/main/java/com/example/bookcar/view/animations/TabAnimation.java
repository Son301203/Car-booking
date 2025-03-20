package com.example.bookcar.view.animations;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.List;

public class TabAnimation {
    private static final float SCALE_FROM = 1.0f;
    private static final float SCALE_TO = 0.8f;
    private static final int ANIMATION_DURATION = 150;
    private static final int UNDERLINE_HEIGHT = 4;
    private static final int UNDERLINE_COLOR = 0xFF4CAF50;

    public static void animateTab(LinearLayout selectedTab, List<LinearLayout> allTabs) {
        for (LinearLayout tab : allTabs) {
            removeExistingUnderline(tab);
        }

        ImageView icon = findIconInTab(selectedTab);
        if (icon != null) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(icon, "scaleX", SCALE_FROM, SCALE_TO, SCALE_FROM);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(icon, "scaleY", SCALE_FROM, SCALE_TO, SCALE_FROM);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY);
            animatorSet.setDuration(ANIMATION_DURATION);
            animatorSet.start();
        }

        View underline = new View(selectedTab.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(UNDERLINE_HEIGHT, selectedTab.getContext())
        );
        params.setMargins(0, dpToPx(4, selectedTab.getContext()), 0, 0);
        underline.setLayoutParams(params);
        underline.setBackgroundColor(UNDERLINE_COLOR);

        selectedTab.addView(underline);

        ObjectAnimator underlineAnim = ObjectAnimator.ofFloat(underline, "scaleX", 0f, 1f);
        underlineAnim.setDuration(ANIMATION_DURATION);
        underlineAnim.start();
    }

    private static ImageView findIconInTab(LinearLayout tabLayout) {
        for (int i = 0; i < tabLayout.getChildCount(); i++) {
            View child = tabLayout.getChildAt(i);
            if (child instanceof ImageView) {
                return (ImageView) child;
            }
        }
        return null;
    }

    private static void removeExistingUnderline(LinearLayout tabLayout) {
        for (int i = 0; i < tabLayout.getChildCount(); i++) {
            View child = tabLayout.getChildAt(i);
            if (child instanceof View && !(child instanceof ImageView)) {
                tabLayout.removeView(child);
                break;
            }
        }
    }

    private static int dpToPx(int dp, android.content.Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}