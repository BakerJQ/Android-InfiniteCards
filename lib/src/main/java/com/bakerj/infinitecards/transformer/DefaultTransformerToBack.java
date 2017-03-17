package com.bakerj.infinitecards.transformer;

import android.view.View;

import com.bakerj.infinitecards.AnimationTransformer;
import com.nineoldandroids.view.ViewHelper;

/**
 * @author BakerJ
 */
public class DefaultTransformerToBack implements AnimationTransformer {
    @Override
    public void transformAnimation(View view, float fraction, int baseWidth, int baseHeight,
                                   int fromPosition, int toPosition) {
        int positionCount = fromPosition - toPosition;
        ViewHelper.setScaleX(view, (0.8f - 0.1f * fromPosition) + (0.1f * fraction * positionCount));
        ViewHelper.setScaleY(view, (0.8f - 0.1f * fromPosition) + (0.1f * fraction * positionCount));
        ViewHelper.setTranslationY(view, baseHeight * (0.25f - 0.1f * fromPosition) + baseHeight
                * 0.1f * fraction * positionCount);
    }

    @Override
    public void transformInterpolatedAnimation(View view, float fraction, int baseWidth,
                                               int baseHeight, int fromPosition, int toPosition) {
    }
}
