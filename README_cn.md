# InfiniteCards
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![](https://jitpack.io/v/BakerJQ/Android-InfiniteCards.svg)](https://jitpack.io/#BakerJQ/Android-InfiniteCards)

可自定义动效的卡片切换视图

实现思路：http://bakerjq.com/2017/05/28/20170528_InfiniteCard/

## 截屏
![](./screenshot/sample.gif)

## Gradle引用
根build.gradle添加：
``` groovy
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
app的build.gradle添加：
``` groovy
dependencies {
    implementation 'com.github.BakerJQ:Android-InfiniteCards:1.0.4'
}
```

## 参数
- animType : 动效展示类型
  - front : 将点击的卡片切换到第一个
  - switchPosition : 将点击的卡片和第一张卡片互换位置
  - frontToLast : 将第一张卡片移到最后，后面的卡片往前移动一个
- cardRatio : 卡片宽高比
- animDuration : 卡片动效时间
- animAddRemoveDelay : 卡片组切换时，添加与移出时，相邻卡片展示动效的间隔时间
- animAddRemoveDuration : 卡片组切换时，添加与移出时，卡片动效时间

## 使用
### xml布局
```xml
<com.bakerj.infinitecards.InfiniteCardView
        android:id="@+id/view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        infiniteCard:animDuration="1000"
        infiniteCard:cardRatio="1"/>

```
### 设置Adapter
直接继承BaseAdapter
```java
class MyAdapter extends BaseAdapter{
  ...
}
mAdapter = new MyAdapter(resId);
mCardView.setAdapter(mAdapter);
```
### 动效的转换和插值
#### 默认
默认情况下可以不设置，或者设置为各种Default
```java
mCardView.setAnimInterpolator(new LinearInterpolator());
mCardView.setTransformerToFront(new DefaultTransformerToFront());
mCardView.setTransformerToBack(new DefaultTransformerToBack());
mCardView.setZIndexTransformerToBack(new DefaultZIndexTransformerCommon());
```
#### 自定义
通过设置转换器与插值器，根据回调中的参数自定义动画效果
```java
mCardView.setTransformerToBack(new AnimationTransformer() {
    @Override
    public void transformAnimation(View view, float fraction, int cardWidth, int cardHeight, int fromPosition, int toPosition) {
        int positionCount = fromPosition - toPosition;
        float scale = (0.8f - 0.1f * fromPosition) + (0.1f * fraction * positionCount);
        ViewHelper.setScaleX(view, scale);
        ViewHelper.setScaleY(view, scale);
        if (fraction < 0.5) {
            ViewCompat.setRotationX(view, 180 * fraction);
        } else {
            ViewCompat.setRotationX(view, 180 * (1 - fraction));
        }
    }

    @Override
    public void transformInterpolatedAnimation(View view, float fraction, int cardWidth, int cardHeight, int fromPosition, int toPosition) {
        int positionCount = fromPosition - toPosition;
        float scale = (0.8f - 0.1f * fromPosition) + (0.1f * fraction * positionCount);
        ViewHelper.setTranslationY(view, -cardHeight * (0.8f - scale) * 0.5f - cardWidth * (0.02f *
                fromPosition - 0.02f * fraction * positionCount));
    }
});
mCardView.setZIndexTransformerToBack(new ZIndexTransformer() {
    @Override
    public void transformAnimation(CardItem card, float fraction, int cardWidth, int cardHeight, int fromPosition, int toPosition) {
        if (fraction < 0.4f) {
            card.zIndex = 1f + 0.01f * fromPosition;
        } else {
            card.zIndex = 1f + 0.01f * toPosition;
        }
    }

    @Override
    public void transformInterpolatedAnimation(CardItem card, float fraction, int cardWidth, int cardHeight, int fromPosition, int toPosition) {

    }
});
```
## *License*

InfiniteCards is released under the Apache 2.0 license.

```
Copyright 2017 BakerJ.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at following link.

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
