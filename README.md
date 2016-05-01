### Status
[![Build Status](https://travis-ci.org/RowlandOti/SlidingTabStripLayout.svg?branch=master)](https://travis-ci.org/RowlandOti/SlidingTabStripLayout)

# SlidingTabStripLayout
Custom SlidingTabStripLayout of the one found in Android Design support library . It has additional features listed below
* SlidingTabStripLayout underline color
* Highlight of selected Tab with different shade of color and size of text(Different Text Appearance)

## Usage
Usage is very similar to the one in the support library . Since I retained the same package names . You can use it like below

```
<android.support.design.widget.SlidingTabStripLayout
        android:id="@+id/slidingTabStrips"
        style="@style/Widget.App.SlidingTabStripLayout"
        android:layout_width="match_parent"
        android:layout_height="40dp"
        android:background="@color/apptheme_primary_grey_light"
        />
```
```
this.slidingTabStrips = (SlidingTabStripLayout) rootView.findViewById(R.id.slidingTabStrips);
this.slidingTabStrips.setupWithViewPager(pager);
```

In your styles include the following
```
<!-- Widget SlidingTabStripLayout styles -->
    <style name="Widget.App.SlidingTabStripLayout" parent="Widget.Design.TabLayout">
        <item name="tabIndicatorColor">?attr/colorAccent</item>
        <item name="tabUnderlineColor">@color/apptheme_divider</item>
        <item name="tabIndicatorHeight">6dp</item>
        <item name="tabUnderlineHeight">2dp</item>
        <item name="tabTextAppearance">@style/MyCustomTabTextAppearance</item>
        <item name="android:textAppearance">@style/MyCustomTabTextAppearance</item>
        <item name="tabSelectedTextAppearance">@style/MyCustomTabTextAppearance.Bold</item>
        <item name="tabSelectedTextColor">@color/apptheme_primary_purple</item>
        <item name="tabIsSelectedTextBold">true</item>
        <item name="tabGravity">fill</item>
        <item name="tabMode">fixed</item>
    </style>

    <!-- TextView styles -->
    <!-- TextView styles Normal -->
    <style name="MyCustomTabTextAppearance" parent="CustomTabTextAppearance">
        <item name="textAllCaps">true</item>
        <item name="android:textSize">14sp</item>
        <item name="android:textStyle">normal</item>
        <item name="android:textColor">@color/selector_tab_text</item>
    </style>
    <!-- TextView styles Bold -->
    <style name="MyCustomTabTextAppearance.Bold" parent="CustomTabTextAppearance.Bold">
        <item name="android:textSize">16sp</item>
        <item name="android:textStyle">bold</item>
        <item name="android:shadowColor">@color/apptheme_primary_text_white</item>
        <item name="android:shadowRadius">2</item>
        <item name="android:shadowDx">0</item>
        <item name="android:shadowDy">0</item>
    </style>
```

Preview: 

![Alt text](https://github.com/RowlandOti/SlidingTabStripLayout/blob/master/documentation/png/stl1.png?raw=true "SlidingTabStripLayout Preview")        ![Alt text](https://github.com/RowlandOti/SlidingTabStripLayout/blob/master/documentation/png/stl2.png?raw=true "SlidingTabStripLayout Preview")




[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/RowlandOti/slidingtabstriplayout/trend.png)](https://bitdeli.com/free "Bitdeli Badge")

