package com.bakerj.infinitecards;

/**
 * @author BakerJ
 */
public interface ZIndexTransformer {
    void transformAnimation(CardItem card, float fraction, int baseWidth, int baseHeight, int
            fromPosition, int toPosition);

    void transformInterpolatedAnimation(CardItem card, float fraction, int baseWidth, int
            baseHeight, int fromPosition, int toPosition);
}
