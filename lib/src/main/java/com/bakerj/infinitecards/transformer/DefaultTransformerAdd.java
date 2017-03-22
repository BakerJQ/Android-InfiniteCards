package com.bakerj.infinitecards.transformer;

import android.view.View;

import com.bakerj.infinitecards.AnimationTransformer;
import com.nineoldandroids.view.ViewHelper;

/**
 * @author BakerJ
 * @date 2017/3/22
 */
public class DefaultTransformerAdd implements AnimationTransformer {
    @Override
    public void transformAnimation(View view, float fraction, int cardWidth, int cardHeight, int fromPosition, int toPosition) {
        fromPosition = toPosition + 1;
        int positionCount = fromPosition - toPosition;
        ViewHelper.setScaleX(view, (0.8f - 0.1f * fromPosition) + (0.1f * fraction * positionCount));
        ViewHelper.setScaleY(view, (0.8f - 0.1f * fromPosition) + (0.1f * fraction * positionCount));
        ViewHelper.setTranslationY(view, -cardWidth * 0.8f * 0.08f * fromPosition + cardWidth
                * 0.8f * 0.08f * fraction * positionCount);
        ViewHelper.setAlpha(view, fraction);
    }

    @Override
    public void transformInterpolatedAnimation(View view, float fraction, int cardWidth, int cardHeight, int fromPosition, int toPosition) {

    }
}
