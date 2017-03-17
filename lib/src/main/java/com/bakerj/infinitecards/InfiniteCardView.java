package com.bakerj.infinitecards;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

import com.bakerj.infinitecards.transformer.DefaultCommonTransformer;
import com.bakerj.infinitecards.transformer.DefaultTransformerToBack;
import com.bakerj.infinitecards.transformer.DefaultTransformerToFront;
import com.bakerj.infinitecards.transformer.DefaultZIndexTransformerCommon;
import com.bakerj.infinitecards.transformer.DefaultZIndexTransformerToFront;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.LinkedList;

/**
 * @author BakerJ
 */
public class InfiniteCardView extends FrameLayout implements Animator.AnimatorListener,
        ValueAnimator.AnimatorUpdateListener {
    public static final int ANIM_TYPE_FRONT = 0, ANIM_TYPE_SWITCH = 1, ANIM_TYPE_FRONT_TO_LAST = 2;
    private static final float CARD_SIZE_FRACTION = 1;
    private static final int ANIM_DURATION = 1000;
    private BaseAdapter mAdapter;
    private LinkedList<CardItem> mCards;
    //    private ArrayList<CardItem> mCards4JudgeZIndex;
    private CardItem mCardToBack, mCardToFront;
    private int mCardCount;
    private int mPositionToFront = 0, mPositionToBack = 0;
    private boolean mIsAnim = false;
    private ValueAnimator mValueAnimator;
    private AnimationTransformer mTransformerToFront, mTransformerToBack, mTransformerCommon;
    private ZIndexTransformer mZIndexTransformerToFront, mZIndexTransformerToBack, mZIndexTransformerCommon;
    private Interpolator mAnimInterpolator;
    private int mCardBaseWidth, mCardBaseHeight;
    private int mAnimType = ANIM_TYPE_FRONT;

    public InfiniteCardView(@NonNull Context context) {
        this(context, null);
    }

    public InfiniteCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InfiniteCardView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true);
        mAnimInterpolator = new LinearInterpolator();
        mTransformerToFront = new DefaultTransformerToFront();
        mTransformerToBack = new DefaultTransformerToBack();
        mTransformerCommon = new DefaultCommonTransformer();
        mZIndexTransformerToFront = new DefaultZIndexTransformerToFront();
        mZIndexTransformerToBack = new DefaultZIndexTransformerCommon();
        mZIndexTransformerCommon = new DefaultZIndexTransformerCommon();
        initAnimator();
    }

    private void initAnimator() {
        mValueAnimator = ValueAnimator.ofFloat(0, 1).setDuration(ANIM_DURATION);
        mValueAnimator.addUpdateListener(this);
        mValueAnimator.addListener(this);
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float fraction = (float) animation.getAnimatedValue();
        float fractionInterpolated = fraction;
        if (mAnimInterpolator != null) {
            fractionInterpolated = mAnimInterpolator.getInterpolation(fraction);
        }
        doAnimationBackToFront(fraction, fractionInterpolated);
        doAnimationFrontToBack(fraction, fractionInterpolated);
        doAnimationCommon(fraction, fractionInterpolated);
        bringToFrontByZIndex();
    }

    private void doAnimationBackToFront(float fraction, float fractionInterpolated) {
        mTransformerToFront.transformAnimation(mCardToFront.view,
                fraction, mCardBaseWidth, mCardBaseHeight, mPositionToFront, 0);
        if (mAnimInterpolator != null) {
            mTransformerToFront.transformInterpolatedAnimation(mCardToFront.view,
                    fractionInterpolated, mCardBaseWidth, mCardBaseHeight, mPositionToFront, 0);
        }
        doAnimationZIndex(mZIndexTransformerToFront, mCardToFront, fraction, fractionInterpolated,
                mPositionToFront, 0);
    }

    private void doAnimationFrontToBack(float fraction, float fractionInterpolated) {
        if (mAnimType == ANIM_TYPE_FRONT) {
            return;
        }
        mTransformerToBack.transformAnimation(mCardToBack.view, fraction, mCardBaseWidth,
                mCardBaseHeight, 0, mPositionToBack);
        if (mAnimInterpolator != null) {
            mTransformerToBack.transformInterpolatedAnimation(mCardToBack.view,
                    fractionInterpolated, mCardBaseWidth, mCardBaseHeight, 0, mPositionToBack);
        }
        doAnimationZIndex(mZIndexTransformerToBack, mCardToBack, fraction, fractionInterpolated,
                0, mPositionToBack);
    }

    private void doAnimationCommon(float fraction, float fractionInterpolated) {
        if (mAnimType == ANIM_TYPE_FRONT) {
            for (int i = 0; i < mPositionToFront; i++) {
                CardItem card = mCards.get(i);
                doAnimationCommonView(card.view, fraction, fractionInterpolated, i, i + 1);
                doAnimationZIndex(mZIndexTransformerCommon, card, fraction, fractionInterpolated,
                        i, i + 1);
            }
        } else if (mAnimType == ANIM_TYPE_FRONT_TO_LAST) {
            for (int i = mPositionToFront + 1; i < mCardCount; i++) {
                CardItem card = mCards.get(i);
                doAnimationCommonView(card.view, fraction, fractionInterpolated, i, i - 1);
                doAnimationZIndex(mZIndexTransformerCommon, card, fraction, fractionInterpolated,
                        i, i - 1);
            }
        }
    }

    private void doAnimationCommonView(View view, float fraction, float fractionInterpolated, int
            fromPosition, int toPosition) {
        mTransformerCommon.transformAnimation(view, fraction, mCardBaseWidth,
                mCardBaseHeight, fromPosition, toPosition);
        if (mAnimInterpolator != null) {
            mTransformerCommon.transformInterpolatedAnimation(view, fractionInterpolated, mCardBaseWidth,
                    mCardBaseHeight, fromPosition, toPosition);
        }
    }

    private void doAnimationZIndex(ZIndexTransformer transformer, CardItem card, float fraction,
                                   float fractionInterpolated, int fromPosition, int toPosition) {
        transformer.transformAnimation(card, fraction, mCardBaseWidth,
                mCardBaseHeight, fromPosition, toPosition);
        if (mAnimInterpolator != null) {
            transformer.transformInterpolatedAnimation(card, fractionInterpolated, mCardBaseWidth,
                    mCardBaseHeight, fromPosition, toPosition);
        }
    }

    private void bringToFrontByZIndex() {
        if (mAnimType == ANIM_TYPE_FRONT) {
            for (int i = mPositionToFront - 1; i >= 0; i--) {
                CardItem card = mCards.get(i);
                if (card.zIndex > mCardToFront.zIndex) {
                    mCardToFront.view.bringToFront();
                } else {
                    card.view.bringToFront();
                }
            }
        } else {
//            Collections.sort(mCards4JudgeZIndex, this);
//            for (int i = mCardCount - 1; i >= 0; i--) {
//                mCards4JudgeZIndex.get(i).view.bringToFront();
//            }
            boolean cardToFrontJudged = false;
            for (int i = mCardCount - 1; i > 0; i--) {
                CardItem card = mCards.get(i);
                CardItem cardPre = i > 1 ? mCards.get(i - 1) : null;
                boolean cardToBackBehindCardPre = cardPre == null ||
                        mCardToBack.zIndex > cardPre.zIndex;
                boolean bringCardToBackViewToFront = mCardToBack.zIndex < card.zIndex && cardToBackBehindCardPre;
                boolean cardToFrontBehindCardPre = cardPre == null ||
                        mCardToFront.zIndex > cardPre.zIndex;
                boolean bringCardToFrontViewToFront = mCardToFront.zIndex < card.zIndex && cardToFrontBehindCardPre;
                if (i != mPositionToFront) {
                    card.view.bringToFront();
                    if (bringCardToBackViewToFront) {
                        mCardToBack.view.bringToFront();
                    }
                    if (bringCardToFrontViewToFront) {
                        mCardToFront.view.bringToFront();
                        cardToFrontJudged = true;
                    }
                    if (bringCardToBackViewToFront && bringCardToFrontViewToFront &&
                            mCardToBack.zIndex < mCardToFront.zIndex) {
                        mCardToBack.view.bringToFront();
                    }
                } else {
                    if (cardToFrontBehindCardPre) {
                        mCardToFront.view.bringToFront();
                        cardToFrontJudged = true;
                        if (cardToBackBehindCardPre && mCardToBack.zIndex < mCardToFront.zIndex) {
                            mCardToBack.view.bringToFront();
                        }
                    }
                }
            }
            if (!cardToFrontJudged) {
                mCardToFront.view.bringToFront();
            }
        }
    }

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (mAnimType == ANIM_TYPE_FRONT) {
            mCards.remove(mPositionToFront);
            mCards.addFirst(mCardToFront);
        } else if (mAnimType == ANIM_TYPE_SWITCH) {
            mCards.remove(mPositionToFront);
            mCards.removeFirst();
            mCards.addFirst(mCardToFront);
            mCards.add(mPositionToFront, mCardToBack);
        } else {
            mCards.remove(mPositionToFront);
            mCards.removeFirst();
            mCards.addFirst(mCardToFront);
            mCards.addLast(mCardToBack);
        }
        mPositionToFront = 0;
        mPositionToBack = 0;
        mIsAnim = false;
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mCardBaseWidth == 0) {
            mCardBaseWidth = getMeasuredWidth();
            mCardBaseHeight = (int) (mCardBaseWidth * CARD_SIZE_FRACTION);
            initAdapterView();
        }
    }

    private void initAdapterView() {
        if (mCardBaseWidth > 0 && mCardBaseHeight > 0 && mCards == null) {
            mCards = new LinkedList<>();
//            mCards4JudgeZIndex = new ArrayList<>();
            mCardCount = mAdapter.getCount();
            for (int i = mCardCount - 1; i >= 0; i--) {
                View child = mAdapter.getView(i, null, this);
                CardItem cardItem = new CardItem(child, 0);
                addCardView(cardItem);
                mZIndexTransformerCommon.transformAnimation(cardItem, 1, mCardBaseWidth, mCardBaseHeight, i, i);
                mTransformerCommon.transformAnimation(child, 1, mCardBaseWidth, mCardBaseHeight, i, i);
                mCards.addFirst(cardItem);
//                mCards4JudgeZIndex.add(cardItem);
            }
        }
    }

    private void addCardView(final CardItem card) {
        View view = card.view;
        view.setLayoutParams(new ViewGroup.LayoutParams(mCardBaseWidth, mCardBaseHeight));
        addView(view);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                bringCardToFront(card);
            }
        });
    }

    private void bringCardToFront(CardItem card) {
        if (!isClickable() || mCards == null || mTransformerCommon == null || mTransformerToFront ==
                null || mTransformerToBack == null) {
            return;
        }
        int position = mCards.indexOf(card);
        bringCardToFront(position);
    }

    public void bringCardToFront(int position) {
        if (position >= 0 && position != mPositionToFront && !mIsAnim) {
            mPositionToFront = position;
            mPositionToBack = mAnimType == ANIM_TYPE_SWITCH ? mPositionToFront :
                    (mCardCount - 1);
            mCardToBack = mCards.getFirst();
            mCardToFront = mCards.get(mPositionToFront);
            if (mValueAnimator.isRunning()) {
                mValueAnimator.end();
            }
            mIsAnim = true;
            mValueAnimator.start();
        }
    }

    private void notifyDataSetChanged() {

    }

    public void setAdapter(BaseAdapter adapter) {
        removeAllViews();
        this.mAdapter = adapter;
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                notifyDataSetChanged();
            }
        });
        initAdapterView();
    }

    public void setTransformerToFront(AnimationTransformer toFrontTransformer) {
        this.mTransformerToFront = toFrontTransformer;
    }

    public void setTransformerToBack(AnimationTransformer toBackTransformer) {
        this.mTransformerToBack = toBackTransformer;
    }

    public void setCommonSwitchTransformer(AnimationTransformer commonTransformer) {
        this.mTransformerCommon = commonTransformer;
    }

    public void setTransformerCommon(AnimationTransformer transformerCommon) {
        this.mTransformerCommon = transformerCommon;
    }

    public void setZIndexTransformerToFront(ZIndexTransformer zIndexTransformerToFront) {
        this.mZIndexTransformerToFront = zIndexTransformerToFront;
    }

    public void setZIndexTransformerToBack(ZIndexTransformer zIndexTransformerToBack) {
        this.mZIndexTransformerToBack = zIndexTransformerToBack;
    }

    public void setZIndexTransformerCommon(ZIndexTransformer zIndexTransformerCommon) {
        this.mZIndexTransformerCommon = zIndexTransformerCommon;
    }

    public void setAnimInterpolator(Interpolator animInterpolator) {
        this.mAnimInterpolator = animInterpolator;
    }

    public void setmAnimType(int mAnimType) {
        this.mAnimType = mAnimType;
    }

//    @Override
//    public int compare(CardItem o1, CardItem o2) {
//        return o1.zIndex < o2.zIndex ? -1 : 1;
//    }
}
