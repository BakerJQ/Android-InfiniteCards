package com.bakerj.infinitecards.transformer;

import com.bakerj.infinitecards.CardItem;
import com.bakerj.infinitecards.ZIndexTransformer;

/**
 * @author BakerJ
 */
public class DefaultZIndexTransformerToFront implements ZIndexTransformer {
    @Override
    public void transformAnimation(CardItem card, float fraction, int baseWidth, int baseHeight, int fromPosition, int toPosition) {
        if (fraction < 0.6f) {
            card.zIndex = 1f + 0.01f * fromPosition;
        } else {
            card.zIndex = 1f + 0.01f * toPosition;
        }
    }

    @Override
    public void transformInterpolatedAnimation(CardItem card, float fraction, int baseWidth, int baseHeight, int fromPosition, int toPosition) {

    }
}
