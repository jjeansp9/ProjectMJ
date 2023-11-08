package kr.jeet.edu.manager.view;


import android.graphics.drawable.Drawable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.request.transition.TransitionFactory;

public class DrawableAlwaysCrossFadeFactory implements TransitionFactory<Drawable> {
    private DrawableCrossFadeTransition resourceTransition = new DrawableCrossFadeTransition(1000, true); // customize to your own needs or apply a builder pattern

    @Override
    public Transition<Drawable> build(DataSource dataSource, boolean isFirstResource) {
        return resourceTransition;
    }
}
