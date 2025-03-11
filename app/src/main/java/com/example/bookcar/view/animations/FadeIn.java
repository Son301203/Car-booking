package com.example.bookcar.view.animations;

import android.app.Activity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ListView;

public class FadeIn {
    Activity context;

    public FadeIn(Activity context) {
        this.context = context;
    }

    public void fadeIn(ListView lv) {
        Animation fadeIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        lv.setLayoutAnimation(new LayoutAnimationController(fadeIn));
    }
}
