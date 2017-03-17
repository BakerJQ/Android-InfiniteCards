package com.bakerj.infinitecards;

import android.view.View;

/**
 * @author BakerJ
 */
public class CardItem {
    public View view;
    public float zIndex;

    CardItem(View view, float zIndex) {
        this.view = view;
        this.zIndex = zIndex;
    }

    @Override
    public int hashCode() {
        return view.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CardItem && view.equals(((CardItem) obj).view);
    }
}
