package com.bakerj.infinitecards;

import android.view.View;

/**
 * @author BakerJ
 */
public class CardItem {
    public View view;
    public float zIndex;
    int adapterIndex;

    CardItem(View view, float zIndex, int adapterIndex) {
        this.view = view;
        this.zIndex = zIndex;
        this.adapterIndex = adapterIndex;
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
