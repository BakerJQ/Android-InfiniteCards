package com.bakerj.infinitecards.transformer;

import android.view.View;

import com.bakerj.infinitecards.AnimationTransformer;

/**
 * @author BakerJ
 */
public class DefaultCommonTransformer implements AnimationTransformer {
    @Override
    public void transformAnimation(View view, float fraction, int cardWidth, int cardHeight, int fromPosition, int toPosition) {
        int positionCount = fromPosition - toPosition;
        float scale = (0.8f - 0.1f * fromPosition) + (0.1f * fraction * positionCount);
        view.setScaleX(scale);
        view.setScaleY(scale);
        view.setTranslationY(-cardHeight * (0.8f - scale) * 0.5f - cardWidth * (0.02f *
                fromPosition - 0.02f * fraction * positionCount));
    }

    @Override
    public void transformInterpolatedAnimation(View view, float fraction, int cardWidth, int cardHeight, int fromPosition, int toPosition) {

    }
}
