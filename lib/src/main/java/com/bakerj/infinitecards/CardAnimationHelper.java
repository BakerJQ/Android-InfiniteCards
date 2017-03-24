package com.bakerj.infinitecards;

import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.BaseAdapter;

import com.bakerj.infinitecards.transformer.DefaultCommonTransformer;
import com.bakerj.infinitecards.transformer.DefaultTransformerAdd;
import com.bakerj.infinitecards.transformer.DefaultTransformerRemove;
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
class CardAnimationHelper implements Animator.AnimatorListener,
        ValueAnimator.AnimatorUpdateListener {
    //animation duration
    static final int ANIM_DURATION = 1000, ANIM_ADD_REMOVE_DELAY = 200,
            ANIM_ADD_REMOVE_DURATION = 500;
    //animation type
    private int mAnimType = InfiniteCardView.ANIM_TYPE_FRONT;
    //animation duration
    private int mAnimDuration = ANIM_DURATION, mAnimAddRemoveDelay = ANIM_ADD_REMOVE_DELAY,
            mAnimAddRemoveDuration = ANIM_ADD_REMOVE_DURATION;
    //card container view
    private InfiniteCardView mCardView;
    //card item list
    private LinkedList<CardItem> mCards;
    //total card count
    private int mCardCount;
    //card width, card height
    //for judge Z index
    //    private ArrayList<CardItem> mCards4JudgeZIndex;
    //current card moving to back, current card moving to front
    private CardItem mCardToBack, mCardToFront;
    //current card position moving to front, current card position moving to front
    private int mPositionToBack = 0, mPositionToFront = 0;
    private int mCardWidth, mCardHeight;
    //is doing animation now
    private boolean mIsAnim = false, mIsAddRemoveAnim = false;
    //animator
    private ValueAnimator mValueAnimator;
    //custom animation transformer for card moving to front, card moving to back, and common card
    private AnimationTransformer mTransformerToFront, mTransformerToBack, mTransformerCommon;
    //custom animation transformer for card add and remove
    private AnimationTransformer mTransformerAnimAdd, mTransformerAnimRemove;
    //custom Z index transformer for card moving to front, card moving to back, and common card
    private ZIndexTransformer mZIndexTransformerToFront, mZIndexTransformerToBack, mZIndexTransformerCommon;
    //animation interpolator
    private Interpolator mAnimInterpolator, mAnimAddRemoveInterpolator;
    //view adapter needs to be notify while animation
    private BaseAdapter mTempAdapter;
    //current animation fraction
    private float mCurrentFraction = 1;

    CardAnimationHelper(int mAnimType, int mAnimDuration, InfiniteCardView infiniteCardView) {
        this.mAnimType = mAnimType;
        this.mAnimDuration = mAnimDuration;
        this.mCardView = infiniteCardView;
        initTransformer();
        initAnimator();
    }

    private void initTransformer() {
        mAnimInterpolator = new LinearInterpolator();
        mAnimAddRemoveInterpolator = new LinearInterpolator();
        mTransformerToFront = new DefaultTransformerToFront();
        mTransformerToBack = new DefaultTransformerToBack();
        mTransformerCommon = new DefaultCommonTransformer();
        mTransformerAnimAdd = new DefaultTransformerAdd();
        mTransformerAnimRemove = new DefaultTransformerRemove();
        mZIndexTransformerToFront = new DefaultZIndexTransformerToFront();
        mZIndexTransformerToBack = new DefaultZIndexTransformerCommon();
        mZIndexTransformerCommon = new DefaultZIndexTransformerCommon();
    }

    /**
     * setup animator
     */
    private void initAnimator() {
        mValueAnimator = ValueAnimator.ofFloat(0, 1).setDuration(mAnimDuration);
        mValueAnimator.addUpdateListener(this);
        mValueAnimator.addListener(this);
    }

    /**
     * do animation while update
     *
     * @param animation animation
     */
    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        mCurrentFraction = (float) animation.getAnimatedValue();
        float fractionInterpolated = mCurrentFraction;
        if (mAnimInterpolator != null) {
            fractionInterpolated = mAnimInterpolator.getInterpolation(mCurrentFraction);
        }
        doAnimationBackToFront(mCurrentFraction, fractionInterpolated);
        doAnimationFrontToBack(mCurrentFraction, fractionInterpolated);
        doAnimationCommon(mCurrentFraction, fractionInterpolated);
        bringToFrontByZIndex();
    }

    /**
     * do animation for card moving from back to front
     *
     * @param fraction             animation progress from 0.0f to 1.0f
     * @param fractionInterpolated interpolated animation progress
     */
    private void doAnimationBackToFront(float fraction, float fractionInterpolated) {
        mTransformerToFront.transformAnimation(mCardToFront.view,
                fraction, mCardWidth, mCardHeight, mPositionToFront, 0);
        if (mAnimInterpolator != null) {
            mTransformerToFront.transformInterpolatedAnimation(mCardToFront.view,
                    fractionInterpolated, mCardWidth, mCardHeight, mPositionToFront, 0);
        }
        doAnimationZIndex(mZIndexTransformerToFront, mCardToFront, fraction, fractionInterpolated,
                mPositionToFront, 0);
    }

    /**
     * do animation for card moving from from to back
     *
     * @param fraction             animation progress from 0.0f to 1.0f
     * @param fractionInterpolated interpolated animation progress
     */
    private void doAnimationFrontToBack(float fraction, float fractionInterpolated) {
        if (mAnimType == InfiniteCardView.ANIM_TYPE_FRONT) {
            return;
        }
        mTransformerToBack.transformAnimation(mCardToBack.view, fraction, mCardWidth,
                mCardHeight, 0, mPositionToBack);
        if (mAnimInterpolator != null) {
            mTransformerToBack.transformInterpolatedAnimation(mCardToBack.view,
                    fractionInterpolated, mCardWidth, mCardHeight, 0, mPositionToBack);
        }
        doAnimationZIndex(mZIndexTransformerToBack, mCardToBack, fraction, fractionInterpolated,
                0, mPositionToBack);
    }

    /**
     * do animation for common card items
     *
     * @param fraction             animation progress from 0.0f to 1.0f
     * @param fractionInterpolated interpolated animation progress
     */
    private void doAnimationCommon(float fraction, float fractionInterpolated) {
        if (mAnimType == InfiniteCardView.ANIM_TYPE_FRONT) {
            for (int i = 0; i < mPositionToFront; i++) {
                CardItem card = mCards.get(i);
                doAnimationCommonView(card.view, fraction, fractionInterpolated, i, i + 1);
                doAnimationZIndex(mZIndexTransformerCommon, card, fraction, fractionInterpolated,
                        i, i + 1);
            }
        } else if (mAnimType == InfiniteCardView.ANIM_TYPE_FRONT_TO_LAST) {
            for (int i = mPositionToFront + 1; i < mCardCount; i++) {
                CardItem card = mCards.get(i);
                doAnimationCommonView(card.view, fraction, fractionInterpolated, i, i - 1);
                doAnimationZIndex(mZIndexTransformerCommon, card, fraction, fractionInterpolated,
                        i, i - 1);
            }
        }
    }

    /**
     * do animation for common card views
     *
     * @param view                 card view
     * @param fraction             animation progress from 0.0f to 1.0f
     * @param fractionInterpolated interpolated animation progress
     * @param fromPosition         card moving from
     * @param toPosition           card moving to
     */
    private void doAnimationCommonView(View view, float fraction, float fractionInterpolated, int
            fromPosition, int toPosition) {
        mTransformerCommon.transformAnimation(view, fraction, mCardWidth,
                mCardHeight, fromPosition, toPosition);
        if (mAnimInterpolator != null) {
            mTransformerCommon.transformInterpolatedAnimation(view, fractionInterpolated, mCardWidth,
                    mCardHeight, fromPosition, toPosition);
        }
    }

    /**
     * do calculation for card Z index
     *
     * @param transformer          Z index transformer
     * @param card                 card item
     * @param fraction             animation progress from 0.0f to 1.0f
     * @param fractionInterpolated interpolated animation progress
     * @param fromPosition         card moving from
     * @param toPosition           card moving to
     */
    private void doAnimationZIndex(ZIndexTransformer transformer, CardItem card, float fraction,
                                   float fractionInterpolated, int fromPosition, int toPosition) {
        transformer.transformAnimation(card, fraction, mCardWidth,
                mCardHeight, fromPosition, toPosition);
        if (mAnimInterpolator != null) {
            transformer.transformInterpolatedAnimation(card, fractionInterpolated, mCardWidth,
                    mCardHeight, fromPosition, toPosition);
        }
    }

    /**
     * bring card to front by Z index, the card with smaller Z index is in front of the card with
     * bigger Z index
     */
    private void bringToFrontByZIndex() {
        if (mAnimType == InfiniteCardView.ANIM_TYPE_FRONT) {
            //if the animation type is ANIM_TYPE_FRONT, which means other cards are under common
            // animation, so start cycling the card items from the position before the moving
            // card, and while the moving card's Z index is smaller than an other card, we
            // call bringToFront for it, otherwise we call bringToFront for other cards
            for (int i = mPositionToFront - 1; i >= 0; i--) {
                CardItem card = mCards.get(i);
                if (card.zIndex > mCardToFront.zIndex) {
                    mCardToFront.view.bringToFront();
                } else {
                    card.view.bringToFront();
                }
            }
        } else {
            //sort the card items by Z index and call bringToFront foreach view
//            Collections.sort(mCards4JudgeZIndex, this);
//            for (int i = mCardCount - 1; i >= 0; i--) {
//                mCards4JudgeZIndex.get(i).view.bringToFront();
//            }
            //##########################for better performance#########################
            boolean cardToFrontBrought = false;//is card moving to front called bringToFront
            //cycling the card items
            for (int i = mCardCount - 1; i > 0; i--) {
                CardItem card = mCards.get(i);
                //get the card before current card
                CardItem cardPre = i > 1 ? mCards.get(i - 1) : null;
                //is card moving to back behind the card before current card
                boolean cardToBackBehindCardPre = cardPre == null ||
                        mCardToBack.zIndex > cardPre.zIndex;
                //if the card moving to back Z index is smaller than current card, and is behind
                // the card before current card, we should call bringToFront for it
                boolean bringCardToBackViewToFront = mCardToBack.zIndex < card.zIndex && cardToBackBehindCardPre;
                //is card moving to front behind the current card
                boolean cardToFrontBehindCardPre = cardPre == null ||
                        mCardToFront.zIndex > cardPre.zIndex;
                //if the card moving to front Z index is smaller than current card, and is behind
                // the card before current card, we should call bringToFront for it
                boolean bringCardToFrontViewToFront = mCardToFront.zIndex < card.zIndex && cardToFrontBehindCardPre;
                //if current card is not the card moving to front
                if (i != mPositionToFront) {
                    //call bringToFront for it
                    card.view.bringToFront();
                    //if we should bring the card moving to back to front, just do it
                    if (bringCardToBackViewToFront) {
                        mCardToBack.view.bringToFront();
                    }
                    //if we should bring the card moving to front to front, just do it
                    if (bringCardToFrontViewToFront) {
                        mCardToFront.view.bringToFront();
                        cardToFrontBrought = true;
                    }
                    //if we has both bring the card moving to front and back to front, and the
                    // card moving to back Z index is smaller than the card moving to front Z
                    // index, we call bringToFront for the card moving to back
                    if (bringCardToBackViewToFront && bringCardToFrontViewToFront &&
                            mCardToBack.zIndex < mCardToFront.zIndex) {
                        mCardToBack.view.bringToFront();
                    }
                } else {
                    //if current card is the card moving to front, and behind the card before
                    if (cardToFrontBehindCardPre) {
                        mCardToFront.view.bringToFront();
                        cardToFrontBrought = true;
                        //if the card moving to back Z index is smaller than the card moving to
                        // front, call bringToFront for it
                        if (cardToBackBehindCardPre && mCardToBack.zIndex < mCardToFront.zIndex) {
                            mCardToBack.view.bringToFront();
                        }
                    }
                }
            }
            // it the card moving to front has not call bringToFront yet, which means it is
            // already in the first position, call bringToFront for it
            if (!cardToFrontBrought) {
                mCardToFront.view.bringToFront();
            }
        }
    }

    @Override
    public void onAnimationStart(Animator animation) {
        mCurrentFraction = 0;
    }

    /**
     * animation end
     *
     * @param animation animation
     */
    @Override
    public void onAnimationEnd(Animator animation) {
        if (mAnimType == InfiniteCardView.ANIM_TYPE_FRONT) {
            //move the card moving to front to the first position
            mCards.remove(mPositionToFront);
            mCards.addFirst(mCardToFront);
        } else if (mAnimType == InfiniteCardView.ANIM_TYPE_SWITCH) {
            //switch the position of the card moving to front and back
            mCards.remove(mPositionToFront);
            mCards.removeFirst();
            mCards.addFirst(mCardToFront);
            mCards.add(mPositionToFront, mCardToBack);
        } else {
            //moving the first position card to last
            mCards.remove(mPositionToFront);
            mCards.removeFirst();
            mCards.addFirst(mCardToFront);
            mCards.addLast(mCardToBack);
        }
        mPositionToFront = 0;
        mPositionToBack = 0;
        mCurrentFraction = 1;
        mIsAnim = false;
        if (mTempAdapter != null) {
            notifyDataSetChanged(mTempAdapter);
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    /**
     * init adapter view
     *
     * @param adapter adapter
     */
    void initAdapterView(BaseAdapter adapter, boolean reset) {
        if (mCardWidth > 0 && mCardHeight > 0) {
            if (mCards == null) {
                mCardView.removeAllViews();
                firstSetAdapter(adapter);
            } else if (reset || mCards.size() != adapter.getCount()) {
                resetAdapter(adapter);
            } else {
                notifySetAdapter(adapter);
            }
        }
    }

    /**
     * reset adapter view
     *
     * @param adapter adapter
     */
    private void resetAdapter(BaseAdapter adapter) {
        if (mTransformerAnimRemove == null) {
            mCardView.removeAllViews();
            firstSetAdapter(adapter);
        } else {
            mIsAddRemoveAnim = true;
            for (int i = 0; i < mCardCount; i++) {
                CardItem cardItem = mCards.get(i);
                showAnimRemove(cardItem.view, mAnimAddRemoveDelay * i, i, i == mCardCount - 1, adapter);
            }
        }
    }

    private void showAnimRemove(final View view, int delay, final int position,
                                final boolean isLast, final BaseAdapter adapter) {
        final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1).setDuration(mAnimAddRemoveDuration);
        valueAnimator.setStartDelay(delay);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = (float) animation.getAnimatedValue();
                mTransformerAnimRemove.transformAnimation(view, fraction, mCardWidth, mCardHeight,
                        position, position);
                if (mAnimAddRemoveInterpolator != null) {
                    mTransformerAnimRemove.transformInterpolatedAnimation(view,
                            mAnimAddRemoveInterpolator.getInterpolation(fraction),
                            mCardWidth, mCardHeight, position, position);
                }
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                if (isLast) {
                    mIsAddRemoveAnim = false;
                    mCardView.removeAllViews();
                    if (mTempAdapter != null) {
                        notifyDataSetChanged(mTempAdapter);
                    } else {
                        firstSetAdapter(adapter);
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mCardView.post(new Runnable() {
            @Override
            public void run() {
                valueAnimator.start();
            }
        });
    }

    /**
     * first time set an adapter
     *
     * @param adapter adapter
     */
    private void firstSetAdapter(BaseAdapter adapter) {
        if (mTransformerAnimAdd != null) {
            mIsAddRemoveAnim = true;
        }
        mCards = new LinkedList<>();
//            mCards4JudgeZIndex = new ArrayList<>();
        mCardCount = adapter.getCount();
        for (int i = mCardCount - 1; i >= 0; i--) {
            View child = adapter.getView(i, null, mCardView);
            CardItem cardItem = new CardItem(child, 0, i);
            mCardView.addCardView(cardItem);
            mZIndexTransformerCommon.transformAnimation(cardItem, mCurrentFraction, mCardWidth, mCardHeight, i, i);
            mTransformerCommon.transformAnimation(child, mCurrentFraction, mCardWidth, mCardHeight, i, i);
            mCards.addFirst(cardItem);
//                mCards4JudgeZIndex.add(cardItem);
            child.setVisibility(View.INVISIBLE);
            showAnimAdd(child, i * mAnimAddRemoveDelay, i, i == mCardCount - 1);
        }
    }

    private void showAnimAdd(final View view, int delay, final int position, final boolean isLast) {
        if (mTransformerAnimAdd == null) {
            return;
        }
        final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1).setDuration(mAnimAddRemoveDuration);
        valueAnimator.setStartDelay(delay);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = (float) animation.getAnimatedValue();
                mTransformerAnimAdd.transformAnimation(view, fraction, mCardWidth, mCardHeight,
                        position, position);
                if (mAnimAddRemoveInterpolator != null) {
                    mTransformerAnimAdd.transformInterpolatedAnimation(view,
                            mAnimAddRemoveInterpolator.getInterpolation(fraction),
                            mCardWidth, mCardHeight, position, position);
                }
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isLast) {
                    mIsAddRemoveAnim = false;
                    if (mTempAdapter != null) {
                        notifyDataSetChanged(mTempAdapter);
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mCardView.post(new Runnable() {
            @Override
            public void run() {
                valueAnimator.start();
            }
        });
    }

    /**
     * notify
     *
     * @param adapter adapter
     */
    private void notifySetAdapter(BaseAdapter adapter) {
        mCardCount = adapter.getCount();
        for (int i = 0; i < mCardCount; i++) {
            CardItem cardItem = mCards.get(i);
            View child = adapter.getView(cardItem.adapterIndex, cardItem.view, mCardView);
            if (child != cardItem.view) {
                if (cardItem.view != null) {
                    mCardView.removeView(cardItem.view);
                }
                cardItem.view = child;
                mCardView.addCardView(cardItem, i);
                mZIndexTransformerCommon.transformAnimation(cardItem, mCurrentFraction, mCardWidth, mCardHeight,
                        i, i);
                mTransformerCommon.transformAnimation(child, mCurrentFraction, mCardWidth, mCardHeight, i, i);
            }
        }
        for (int i = mCardCount - 1; i >= 0; i--) {
            mCards.get(i).view.bringToFront();
        }
    }

    void notifyDataSetChanged(BaseAdapter adapter) {
        if (mIsAnim || mIsAddRemoveAnim) {
            mTempAdapter = adapter;
        } else {
            mTempAdapter = null;
            initAdapterView(adapter, false);
        }
    }

    void bringCardToFront(CardItem card) {
        if (mCards == null || mTransformerCommon == null || mTransformerToFront ==
                null || mTransformerToBack == null) {
            return;
        }
        int position = mCards.indexOf(card);
        bringCardToFront(position);
    }

    /**
     * bring the specific position card to front
     *
     * @param position position
     */
    void bringCardToFront(int position) {
        if (position >= 0 && position != mPositionToFront && !mIsAnim && !mIsAddRemoveAnim) {
            mPositionToFront = position;
            //if the animation type is not ANIM_TYPE_SWITCH, the card to back post is the last
            // position
            mPositionToBack = mAnimType == InfiniteCardView.ANIM_TYPE_SWITCH ? mPositionToFront :
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

//    @Override
//    public int compare(CardItem o1, CardItem o2) {
//        return o1.zIndex < o2.zIndex ? -1 : 1;
//    }

    void setCardSize(int cardWidth, int cardHeight) {
        this.mCardWidth = cardWidth;
        this.mCardHeight = cardHeight;
    }

    void setTransformerToFront(AnimationTransformer toFrontTransformer) {
        if(mIsAnim || mIsAddRemoveAnim){
            return;
        }
        this.mTransformerToFront = toFrontTransformer;
    }

    void setTransformerToBack(AnimationTransformer toBackTransformer) {
        if(mIsAnim || mIsAddRemoveAnim){
            return;
        }
        this.mTransformerToBack = toBackTransformer;
    }

    void setCommonSwitchTransformer(AnimationTransformer commonTransformer) {
        if(mIsAnim || mIsAddRemoveAnim){
            return;
        }
        this.mTransformerCommon = commonTransformer;
    }

    void setTransformerCommon(AnimationTransformer transformerCommon) {
        if(mIsAnim || mIsAddRemoveAnim){
            return;
        }
        this.mTransformerCommon = transformerCommon;
    }

    void setZIndexTransformerToFront(ZIndexTransformer zIndexTransformerToFront) {
        if(mIsAnim || mIsAddRemoveAnim){
            return;
        }
        this.mZIndexTransformerToFront = zIndexTransformerToFront;
    }

    void setZIndexTransformerToBack(ZIndexTransformer zIndexTransformerToBack) {
        if(mIsAnim || mIsAddRemoveAnim){
            return;
        }
        this.mZIndexTransformerToBack = zIndexTransformerToBack;
    }

    void setZIndexTransformerCommon(ZIndexTransformer zIndexTransformerCommon) {
        if(mIsAnim || mIsAddRemoveAnim){
            return;
        }
        this.mZIndexTransformerCommon = zIndexTransformerCommon;
    }

    void setAnimInterpolator(Interpolator animInterpolator) {
        if(mIsAnim || mIsAddRemoveAnim){
            return;
        }
        this.mAnimInterpolator = animInterpolator;
    }

    void setAnimType(int animType) {
        if(mIsAnim || mIsAddRemoveAnim){
            return;
        }
        this.mAnimType = animType;
    }

    void setTransformerAnimAdd(AnimationTransformer transformerAnimAdd) {
        if(mIsAnim || mIsAddRemoveAnim){
            return;
        }
        this.mTransformerAnimAdd = transformerAnimAdd;
    }

    void setTransformerAnimRemove(AnimationTransformer transformerAnimRemove) {
        if(mIsAnim || mIsAddRemoveAnim){
            return;
        }
        this.mTransformerAnimRemove = transformerAnimRemove;
    }

    void setAnimAddRemoveInterpolator(Interpolator animAddRemoveInterpolator) {
        if(mIsAnim || mIsAddRemoveAnim){
            return;
        }
        this.mAnimAddRemoveInterpolator = animAddRemoveInterpolator;
    }

    void setAnimAddRemoveDelay(int animAddRemoveDelay) {
        if(mIsAnim || mIsAddRemoveAnim){
            return;
        }
        this.mAnimAddRemoveDelay = animAddRemoveDelay;
    }

    void setAnimAddRemoveDuration(int animAddRemoveDuration) {
        if(mIsAnim || mIsAddRemoveAnim){
            return;
        }
        this.mAnimAddRemoveDuration = animAddRemoveDuration;
    }

    boolean isAnimating(){
        return mIsAnim || mIsAddRemoveAnim;
    }
}
