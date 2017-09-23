package com.bakerj.infinitecards.transformer;

import android.view.View;

import com.bakerj.infinitecards.AnimationTransformer;

/**
 * @author BakerJ
 */
public class DefaultTransformerToFront implements AnimationTransformer {
    @Override
    public void transformAnimation(View view, float fraction, int cardWidth, int cardHeight,
                                   int fromPosition, int toPosition) {
        int positionCount = fromPosition - toPosition;
        float scale = (0.8f - 0.1f * fromPosition) + (0.1f * fraction * positionCount);
        view.setScaleX(scale);
        view.setScaleY(scale);
        view.setRotationX(180 * (1 - fraction));
        if (fraction < 0.5) {
            view.setTranslationY(-cardHeight * (0.8f - scale) * 0.5f - cardWidth * 0.02f
                    * fromPosition - cardHeight * fraction);
        } else {
            view.setTranslationY(-cardHeight * (0.8f - scale) * 0.5f - cardWidth * (0.02f *
                    fromPosition - 0.02f * fraction * positionCount) - cardHeight * (1 - fraction));
        }
    }

    @Override
    public void transformInterpolatedAnimation(View view, float fraction, int cardWidth,
                                               int cardHeight, int fromPosition, int toPosition) {
    }
}
