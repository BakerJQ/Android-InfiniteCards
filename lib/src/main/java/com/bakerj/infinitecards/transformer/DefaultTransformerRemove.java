package com.bakerj.infinitecards.transformer;

import android.view.View;

import com.bakerj.infinitecards.AnimationTransformer;
import com.nineoldandroids.view.ViewHelper;

/**
 * @author BakerJ
 * @date 2017/3/22
 */
public class DefaultTransformerRemove implements AnimationTransformer {
    @Override
    public void transformAnimation(View view, float fraction, int cardWidth, int cardHeight, int fromPosition, int toPosition) {
        ViewHelper.setScaleX(view, (0.8f - 0.1f * fromPosition));
        ViewHelper.setScaleY(view, (0.8f - 0.1f * fromPosition));
        ViewHelper.setTranslationY(view, -cardWidth * 0.8f * 0.08f * fromPosition + cardHeight *
                fraction);
        ViewHelper.setAlpha(view, 1 - fraction);
    }

    @Override
    public void transformInterpolatedAnimation(View view, float fraction, int cardWidth, int cardHeight, int fromPosition, int toPosition) {

    }
}
