package com.bakerj.infinitecards.transformer;

import android.view.View;

import com.bakerj.infinitecards.AnimationTransformer;
import com.nineoldandroids.view.ViewHelper;

/**
 * @author BakerJ
 */
public class DefaultTransformerToFront implements AnimationTransformer {
    @Override
    public void transformAnimation(View view, float fraction, int cardWidth, int cardHeight,
                                   int fromPosition, int toPosition) {
        int positionCount = fromPosition - toPosition;
        ViewHelper.setScaleX(view, (0.8f - 0.1f * fromPosition) + (0.1f * fraction * positionCount));
        ViewHelper.setScaleY(view, (0.8f - 0.1f * fromPosition) + (0.1f * fraction * positionCount));
        ViewHelper.setRotationX(view, 180 * (1 - fraction));
        if (fraction < 0.5) {
            ViewHelper.setTranslationY(view, -cardWidth * 0.8f * 0.08f * fromPosition -
                    cardHeight * fraction);
        } else {
            ViewHelper.setTranslationY(view, -cardWidth * 0.8f * 0.08f * fromPosition -
                    cardHeight * (1 - fraction) + cardWidth * 0.8f * 0.08f * fraction *
                    positionCount);
        }
    }

    @Override
    public void transformInterpolatedAnimation(View view, float fraction, int cardWidth,
                                               int cardHeight, int fromPosition, int toPosition) {
    }
}
