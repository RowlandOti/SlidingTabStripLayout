package android.support.design.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.design.widget.ValueAnimatorCompat.AnimatorListenerAdapter;
import android.support.design.widget.ValueAnimatorCompat.AnimatorUpdateListener;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class is a custom design of the same class found in the android.support.design library. I decided to use it
 * under the same package name for ease of implementation and reuse protected classes.
 * <p/>
 * To be used with ViewPager to provide a tab indicator component which give constant feedback as
 * to the user's scroll progress.
 * <p/>
 * To use the component, simply add it to your view hierarchy. Then in your
 * {@link android.app.Activity} or {@link android.support.v4.app.Fragment} call
 * {@link #setupWithViewPager(ViewPager)} ViewPager(android.support.v4.view.ViewPager)} providing it the ViewPager this layout
 * is being used for.
 * <p>
 * Created by Google -Android on 7/5/2015.
 * Created by Rowland on 7/5/2015.
 * </p>
 */
public class SlidingTabStripLayout extends HorizontalScrollView {

    private final String LOG_TAG = SlidingTabStripLayout.class.getSimpleName();
    private static final int MAX_TAB_TEXT_LINES = 2;
    private static final int DEFAULT_HEIGHT = 48;
    private static final int TAB_MIN_WIDTH_MARGIN = 56;
    private static final int FIXED_WRAP_GUTTER_MIN = 16;
    private static final int MOTION_NON_ADJACENT_OFFSET = 24;
    private static final int ANIMATION_DURATION = 300;
    public static final int MODE_SCROLLABLE = 0;
    public static final int MODE_FIXED = 1;
    public static final int GRAVITY_FILL = 0;
    public static final int GRAVITY_CENTER = 1;
    private final ArrayList<SlidingTabStripLayout.Tab> mTabs;
    private SlidingTabStripLayout.Tab mSelectedTab;
    private final SlidingTabStripLayout.SlidingTabStrip mTabStrip;
    private int mTabPaddingStart;
    private int mTabPaddingTop;
    private int mTabPaddingEnd;
    private int mTabPaddingBottom;
    private int mTabTextAppearance;
    private int mTabSelectedTextAppearance;
    private boolean isTabSelectedTextBold;
    private ColorStateList mTabTextColors;
    private final int mTabBackgroundResId;
    private final int mTabMinWidth;
    private int mTabMaxWidth;
    private final int mRequestedTabMaxWidth;
    private int mContentInsetStart;
    private int mTabGravity;
    private int mMode;

    private SlidingTabStripLayout.OnTabSelectedListener mOnTabSelectedListener;
    private OnClickListener mTabClickListener;

    public SlidingTabStripLayout(Context context) {
        this(context, (AttributeSet) null);
    }

    public SlidingTabStripLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingTabStripLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mTabs = new ArrayList();
        this.setHorizontalScrollBarEnabled(false);
        this.setFillViewport(true);
        this.mTabStrip = new SlidingTabStrip(context);
        this.addView(this.mTabStrip, -2, -1);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabLayout, defStyleAttr, R.style.Widget_Design_TabLayout);
        this.mTabStrip.setSelectedIndicatorHeight(a.getDimensionPixelSize(R.styleable.TabLayout_tabIndicatorHeight, 0));
        this.mTabStrip.setSelectedIndicatorColor(a.getColor(R.styleable.TabLayout_tabIndicatorColor, 0));
        this.mTabStrip.setUnderlineHeight(a.getDimensionPixelSize(R.styleable.TabLayout_tabUnderlineHeight, 0));
        this.mTabStrip.setUnderlineColor(a.getColor(R.styleable.TabLayout_tabUnderlineColor, 0));
        this.mTabTextAppearance = a.getResourceId(R.styleable.TabLayout_tabTextAppearance, R.style.TextAppearance_Design_Tab);
        this.mTabSelectedTextAppearance = a.getResourceId(R.styleable.TabLayout_tabSelectedTextAppearance, R.style.CustomTabTextAppearance_Bold);
        this.isTabSelectedTextBold = a.getBoolean(R.styleable.TabLayout_tabIsSelectedTextBold, false);
        this.mTabPaddingStart = this.mTabPaddingTop = this.mTabPaddingEnd = this.mTabPaddingBottom = a.getDimensionPixelSize(R.styleable.TabLayout_tabPadding, 0);
        this.mTabPaddingStart = a.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingStart, this.mTabPaddingStart);
        this.mTabPaddingTop = a.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingTop, this.mTabPaddingTop);
        this.mTabPaddingEnd = a.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingEnd, this.mTabPaddingEnd);
        this.mTabPaddingBottom = a.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingBottom, this.mTabPaddingBottom);

        if (a.hasValue(R.styleable.TabLayout_tabSelectedTextColor)) {
            TypedArray b = this.getContext().obtainStyledAttributes(mTabTextAppearance, R.styleable.TextAppearance);
            int selected = a.getColor(R.styleable.TabLayout_tabSelectedTextColor, 0);
            int defaulted = b.getColorStateList(R.styleable.TextAppearance_android_textColor).getDefaultColor();
            this.mTabTextColors = createColorStateList(selected, selected, defaulted);
        } else {
            this.mTabTextColors = this.loadTextColorFromTextAppearance(this.mTabTextAppearance);
        }


        this.mTabMinWidth = a.getDimensionPixelSize(R.styleable.TabLayout_tabMinWidth, 0);
        this.mRequestedTabMaxWidth = a.getDimensionPixelSize(R.styleable.TabLayout_tabMaxWidth, 0);
        this.mTabBackgroundResId = a.getResourceId(R.styleable.TabLayout_tabBackground, 0);
        this.mContentInsetStart = a.getDimensionPixelSize(R.styleable.TabLayout_tabContentStart, 0);
        this.mMode = a.getInt(R.styleable.TabLayout_tabMode, 1);
        this.mTabGravity = a.getInt(R.styleable.TabLayout_tabGravity, 0);
        a.recycle();
        this.applyModeAndGravity();
    }

    // Scroll to the position given
    public void setScrollPosition(int position, float positionOffset, boolean updateSelectedText) {
        if (!isAnimationRunning(this.getAnimation())) {
            if (position >= 0 && position < this.mTabStrip.getChildCount()) {
                this.mTabStrip.setIndicatorPositionFromTabPosition(position, positionOffset);
                this.scrollTo(this.calculateScrollXForTab(position, positionOffset), 0);
                if (updateSelectedText) {
                    this.setSelectedTabView(Math.round((float) position + positionOffset));
                }

            }
        }
    }

    public void addTab(SlidingTabStripLayout.Tab tab) {
        this.addTab(tab, this.mTabs.isEmpty());
    }

    public void addTab(SlidingTabStripLayout.Tab tab, int position) {
        this.addTab(tab, position, this.mTabs.isEmpty());
    }

    public void addTab(SlidingTabStripLayout.Tab tab, boolean setSelected) {
        if (tab.mParent != this) {
            throw new IllegalArgumentException("Tab belongs to a different TabLayout.");
        } else {
            this.addTabView(tab, setSelected);
            this.configureTab(tab, this.mTabs.size());
            if (setSelected) {
                tab.select();
            }

        }
    }

    public void addTab(SlidingTabStripLayout.Tab tab, int position, boolean setSelected) {
        if (tab.mParent != this) {
            throw new IllegalArgumentException("Tab belongs to a different TabLayout.");
        } else {
            this.addTabView(tab, position, setSelected);
            this.configureTab(tab, position);
            if (setSelected) {
                tab.select();
            }

        }
    }

    public void setOnTabSelectedListener(SlidingTabStripLayout.OnTabSelectedListener onTabSelectedListener) {
        this.mOnTabSelectedListener = onTabSelectedListener;
    }

    public SlidingTabStripLayout.Tab newTab() {
        return new SlidingTabStripLayout.Tab(this);
    }

    public int getTabCount() {
        return this.mTabs.size();
    }

    public SlidingTabStripLayout.Tab getTabAt(int index) {
        return (SlidingTabStripLayout.Tab) this.mTabs.get(index);
    }

    public void removeTab(SlidingTabStripLayout.Tab tab) {
        if (tab.mParent != this) {
            throw new IllegalArgumentException("Tab does not belong to this TabLayout.");
        } else {
            this.removeTabAt(tab.getPosition());
        }
    }

    public void removeTabAt(int position) {
        int selectedTabPosition = this.mSelectedTab != null ? this.mSelectedTab.getPosition() : 0;
        this.removeTabViewAt(position);
        SlidingTabStripLayout.Tab removedTab = (SlidingTabStripLayout.Tab) this.mTabs.remove(position);
        if (removedTab != null) {
            removedTab.setPosition(-1);
        }

        int newTabCount = this.mTabs.size();

        for (int i = position; i < newTabCount; ++i) {
            ((SlidingTabStripLayout.Tab) this.mTabs.get(i)).setPosition(i);
        }

        if (selectedTabPosition == position) {
            this.selectTab(this.mTabs.isEmpty() ? null : (SlidingTabStripLayout.Tab) this.mTabs.get(Math.max(0, position - 1)));
        }

    }

    public void removeAllTabs() {
        this.mTabStrip.removeAllViews();
        Iterator i = this.mTabs.iterator();

        while (i.hasNext()) {
            SlidingTabStripLayout.Tab tab = (SlidingTabStripLayout.Tab) i.next();
            tab.setPosition(-1);
            i.remove();
        }

    }

    public void setTabMode(int mode) {
        if (mode != this.mMode) {
            this.mMode = mode;
            this.applyModeAndGravity();
        }

    }

    public int getTabMode() {
        return this.mMode;
    }

    public void setTabGravity(int gravity) {
        if (this.mTabGravity != gravity) {
            this.mTabGravity = gravity;
            this.applyModeAndGravity();
        }

    }

    public int getTabGravity() {
        return this.mTabGravity;
    }

    public void setTabTextColors(ColorStateList textColor) {
        if (this.mTabTextColors != textColor) {
            this.mTabTextColors = textColor;
            this.updateAllTabs();
        }
    }

    public int getTabTextAppearance() {
        return this.mTabTextAppearance;
    }

    public void setTabTextAppearance(int textAppearance) {
        this.mTabTextAppearance = textAppearance;
        this.updateAllTabs();
    }


    public ColorStateList getTabTextColors() {
        return this.mTabTextColors;
    }

    public void setTabTextColors(int normalColor, int selectedColor) {
        this.setTabTextColors(createColorStateList(selectedColor, selectedColor, normalColor));
    }

    public void setupWithViewPager(ViewPager viewPager) {
        PagerAdapter adapter = viewPager.getAdapter();
        if (adapter == null) {
            throw new IllegalArgumentException("ViewPager does not have a PagerAdapter set");
        } else {
            this.setTabsFromPagerAdapter(adapter);
            viewPager.addOnPageChangeListener(new SlidingTabStripLayout.TabLayoutOnPageChangeListener(this));
            this.setOnTabSelectedListener(new SlidingTabStripLayout.ViewPagerOnTabSelectedListener(viewPager));
        }
    }

    public void setTabsFromPagerAdapter(PagerAdapter adapter) {
        this.removeAllTabs();
        int i = 0;

        for (int count = adapter.getCount(); i < count; ++i) {
            this.addTab(this.newTab().setText(adapter.getPageTitle(i)));
        }

    }

    private void updateAllTabs() {
        int i = 0;

        for (int z = this.mTabStrip.getChildCount(); i < z; ++i) {
            this.updateTab(i);
        }

    }

    private SlidingTabStripLayout.TabView createTabView(SlidingTabStripLayout.Tab tab) {
        SlidingTabStripLayout.TabView tabView = new SlidingTabStripLayout.TabView(this.getContext(), tab);
        tabView.setFocusable(true);
        if (this.mTabClickListener == null) {
            this.mTabClickListener = new OnClickListener() {
                public void onClick(View view) {
                    SlidingTabStripLayout.TabView tabView = (SlidingTabStripLayout.TabView) view;
                    tabView.getTab().select();
                }
            };
        }

        tabView.setOnClickListener(this.mTabClickListener);
        return tabView;
    }

    private void configureTab(SlidingTabStripLayout.Tab tab, int position) {
        tab.setPosition(position);
        this.mTabs.add(position, tab);
        int count = this.mTabs.size();

        for (int i = position + 1; i < count; ++i) {
            ((SlidingTabStripLayout.Tab) this.mTabs.get(i)).setPosition(i);
        }

    }

    private void updateTab(int position) {
        SlidingTabStripLayout.TabView view = (SlidingTabStripLayout.TabView) this.mTabStrip.getChildAt(position);
        if (view != null) {
            view.update();
        }

    }

    private void addTabView(SlidingTabStripLayout.Tab tab, boolean setSelected) {
        SlidingTabStripLayout.TabView tabView = this.createTabView(tab);
        this.mTabStrip.addView(tabView, this.createLayoutParamsForTabs());
        if (setSelected) {
            tabView.setSelected(true);
        }

    }

    private void addTabView(SlidingTabStripLayout.Tab tab, int position, boolean setSelected) {
        SlidingTabStripLayout.TabView tabView = this.createTabView(tab);
        this.mTabStrip.addView(tabView, position, this.createLayoutParamsForTabs());
        if (setSelected) {
            tabView.setSelected(true);
        }

    }

    private LinearLayout.LayoutParams createLayoutParamsForTabs() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -1);
        this.updateTabViewLayoutParams(lp);
        return lp;
    }

    private void updateTabViewLayoutParams(LinearLayout.LayoutParams lp) {
        if (this.mMode == 1 && this.mTabGravity == 0) {
            lp.width = 0;
            lp.weight = 1.0F;
        } else {
            lp.width = -2;
            lp.weight = 0.0F;
        }

    }

    private int dpToPx(int dps) {
        return Math.round(this.getResources().getDisplayMetrics().density * (float) dps);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case -2147483648:
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(this.dpToPx(48), MeasureSpec.getSize(heightMeasureSpec)), 1073741824);
                break;
            case 0:
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(this.dpToPx(48), 1073741824);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int defaultTabMaxWidth;
        if (this.mMode == 1 && this.getChildCount() == 1) {
            View maxTabWidth = this.getChildAt(0);
            defaultTabMaxWidth = this.getMeasuredWidth();
            if (maxTabWidth.getMeasuredWidth() > defaultTabMaxWidth) {
                int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, this.getPaddingTop() + this.getPaddingBottom(), maxTabWidth.getLayoutParams().height);
                int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(defaultTabMaxWidth, 1073741824);
                maxTabWidth.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }

        int maxTabWidth1 = this.mRequestedTabMaxWidth;
        defaultTabMaxWidth = this.getMeasuredWidth() - this.dpToPx(56);
        if (maxTabWidth1 == 0 || maxTabWidth1 > defaultTabMaxWidth) {
            maxTabWidth1 = defaultTabMaxWidth;
        }

        this.mTabMaxWidth = maxTabWidth1;
    }

    private void removeTabViewAt(int position) {
        this.mTabStrip.removeViewAt(position);
        this.requestLayout();
    }

    private void animateToTab(int newPosition) {
        this.clearAnimation();
        if (newPosition != -1) {
            if (this.getWindowToken() != null && ViewCompat.isLaidOut(this)) {
                int startScrollX = this.getScrollX();
                int targetScrollX = this.calculateScrollXForTab(newPosition, 0.0F);
                boolean duration = true;
                if (startScrollX != targetScrollX) {
                    ValueAnimatorCompat animator = ViewUtils.createAnimator();
                    animator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
                    animator.setDuration(300);
                    animator.setIntValues(startScrollX, targetScrollX);
                    animator.setUpdateListener(new AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimatorCompat animator) {
                            SlidingTabStripLayout.this.scrollTo(animator.getAnimatedIntValue(), 0);
                        }
                    });
                    animator.start();
                }

                this.mTabStrip.animateIndicatorToPosition(newPosition, 300);
            } else {
                this.setScrollPosition(newPosition, 0.0F, true);
            }
        }
    }

    private void setSelectedTabView(int position) {
        int tabCount = this.mTabStrip.getChildCount();

        for (int i = 0; i < tabCount; ++i) {
            View child = this.mTabStrip.getChildAt(i);
            child.setSelected(i == position);
        }

    }

    private static boolean isAnimationRunning(Animation animation) {
        return animation != null && animation.hasStarted() && !animation.hasEnded();
    }

    void selectTab(SlidingTabStripLayout.Tab tab) {
        if (this.mSelectedTab == tab) {
            if (this.mSelectedTab != null) {
                if (this.mOnTabSelectedListener != null) {
                    this.mOnTabSelectedListener.onTabReselected(this.mSelectedTab);
                }

                this.animateToTab(tab.getPosition());
            }
        } else {
            int newPosition = tab != null ? tab.getPosition() : -1;
            this.setSelectedTabView(newPosition);
            if ((this.mSelectedTab == null || this.mSelectedTab.getPosition() == -1) && newPosition != -1) {
                this.setScrollPosition(newPosition, 0.0F, true);
            } else {
                this.animateToTab(newPosition);
            }

            if (this.mSelectedTab != null && this.mOnTabSelectedListener != null) {
                this.mOnTabSelectedListener.onTabUnselected(this.mSelectedTab);
            }

            this.mSelectedTab = tab;
            if (this.mSelectedTab != null && this.mOnTabSelectedListener != null) {
                this.mOnTabSelectedListener.onTabSelected(this.mSelectedTab);
            }
        }

    }

    private int calculateScrollXForTab(int position, float positionOffset) {
        if (this.mMode == 0) {
            View selectedChild = this.mTabStrip.getChildAt(position);
            View nextChild = position + 1 < this.mTabStrip.getChildCount() ? this.mTabStrip.getChildAt(position + 1) : null;
            int selectedWidth = selectedChild != null ? selectedChild.getWidth() : 0;
            int nextWidth = nextChild != null ? nextChild.getWidth() : 0;
            return (int) ((float) selectedChild.getLeft() + (float) (selectedWidth + nextWidth) * positionOffset * 0.5F + (float) selectedChild.getWidth() * 0.5F - (float) this.getWidth() * 0.5F);
        } else {
            return 0;
        }
    }

    private void applyModeAndGravity() {
        int paddingStart = 0;
        if (this.mMode == 0) {
            paddingStart = Math.max(0, this.mContentInsetStart - this.mTabPaddingStart);
        }

        ViewCompat.setPaddingRelative(this.mTabStrip, paddingStart, 0, 0, 0);
        switch (this.mMode) {
            case 0:
                this.mTabStrip.setGravity(8388611);
                break;
            case 1:
                this.mTabStrip.setGravity(1);
        }

        this.updateTabViewsLayoutParams();
    }

    private void updateTabViewsLayoutParams() {
        for (int i = 0; i < this.mTabStrip.getChildCount(); ++i) {
            View child = this.mTabStrip.getChildAt(i);
            this.updateTabViewLayoutParams((LinearLayout.LayoutParams) child.getLayoutParams());
            child.requestLayout();
        }

    }

    private ColorStateList createColorStateList(int color_state_pressed, int color_state_selected, int color_state_default) {
        return new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_pressed}, //pressed
                        new int[]{android.R.attr.state_selected}, // enabled
                        new int[]{} //default
                },
                new int[]{
                        color_state_pressed,
                        color_state_selected,
                        color_state_default
                }
        );
    }

    private ColorStateList loadTextColorFromTextAppearance(int textAppearanceResId) {
        TypedArray a = this.getContext().obtainStyledAttributes(textAppearanceResId, R.styleable.TextAppearance);

        ColorStateList colorList;
        try {
            colorList = a.getColorStateList(R.styleable.TextAppearance_android_textColor);
        } finally {
            a.recycle();
        }

        return colorList;
    }


    public static class ViewPagerOnTabSelectedListener implements SlidingTabStripLayout.OnTabSelectedListener {
        private final String LOG_TAG = ViewPagerOnTabSelectedListener.class.getSimpleName();
        private final ViewPager mViewPager;

        public ViewPagerOnTabSelectedListener(ViewPager viewPager) {
            this.mViewPager = viewPager;
        }

        public void onTabSelected(SlidingTabStripLayout.Tab tab) {
            this.mViewPager.setCurrentItem(tab.getPosition());

            //Log.d(this.LOG_TAG, "Called onTabSelected");
        }

        public void onTabUnselected(SlidingTabStripLayout.Tab tab) {
            //Log.d(this.LOG_TAG, "Called onTabUnSelected");
        }

        public void onTabReselected(SlidingTabStripLayout.Tab tab) {
            //Log.d(this.LOG_TAG, "Called onTabReSelected");
        }
    }

    public static class TabLayoutOnPageChangeListener implements OnPageChangeListener {
        private final WeakReference<SlidingTabStripLayout> mTabLayoutRef;
        private int mScrollState;

        public TabLayoutOnPageChangeListener(SlidingTabStripLayout tabLayout) {
            this.mTabLayoutRef = new WeakReference(tabLayout);
        }

        public void onPageScrollStateChanged(int state) {
            this.mScrollState = state;
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            SlidingTabStripLayout tabLayout = (SlidingTabStripLayout) this.mTabLayoutRef.get();
            if (tabLayout != null) {
                tabLayout.setScrollPosition(position, positionOffset, this.mScrollState == 1);
            }

        }

        public void onPageSelected(int position) {
            SlidingTabStripLayout tabLayout = (SlidingTabStripLayout) this.mTabLayoutRef.get();
            if (tabLayout != null) {
                tabLayout.getTabAt(position).select();
            }

        }
    }

    /*
     * <p>
     * The colors can be customized in two ways. The first and simplest is to provide an array of
     * colors via {@link SlidingTabStripLayout.SlidingTabStrip.setSelectedIndicatorColor(Color)} Color(android.graphics.Color)}.
     * <p>*/
    private class SlidingTabStrip extends LinearLayout {
        private int mSelectedIndicatorHeight;
        private int mUnderlineHeight;
        private final Paint mSelectedIndicatorPaint;
        private final Paint mUnderlinePaint;
        private int mSelectedPosition = -1;
        private float mSelectionOffset;
        private int mIndicatorLeft = -1;
        private int mIndicatorRight = -1;

        SlidingTabStrip(Context context) {
            super(context);
            this.setWillNotDraw(false);
            this.mSelectedIndicatorPaint = new Paint();
            this.mUnderlinePaint = new Paint();

            //Bottom padding for the tabs container parent view to show indicator and underline
            setTabsContainerParentViewPaddings();
        }

        void setTabsContainerParentViewPaddings() {
            int bottomMargin = mSelectedIndicatorHeight >= mUnderlineHeight ? mSelectedIndicatorHeight : mUnderlineHeight;
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), bottomMargin);
        }

        void setSelectedIndicatorColor(int color) {
            this.mSelectedIndicatorPaint.setColor(color);
            this.mSelectedIndicatorPaint.setAntiAlias(true);
            ViewCompat.postInvalidateOnAnimation(this);
        }

        void setSelectedIndicatorHeight(int height) {
            this.mSelectedIndicatorHeight = height;
            ViewCompat.postInvalidateOnAnimation(this);
        }

        void setIndicatorPositionFromTabPosition(int position, float positionOffset) {
            if (!SlidingTabStripLayout.isAnimationRunning(this.getAnimation())) {
                this.mSelectedPosition = position;
                this.mSelectionOffset = positionOffset;
                this.updateIndicatorPosition();
            }
        }

        void setUnderlineColor(int color) {
            this.mUnderlinePaint.setColor(color);
            this.mUnderlinePaint.setAntiAlias(true);
            ViewCompat.postInvalidateOnAnimation(this);
        }

        void setUnderlineHeight(int height) {
            this.mUnderlineHeight = height;
            ViewCompat.postInvalidateOnAnimation(this);
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            if (MeasureSpec.getMode(widthMeasureSpec) == 1073741824) {
                if (SlidingTabStripLayout.this.mMode == 1 && SlidingTabStripLayout.this.mTabGravity == 1) {
                    int count = this.getChildCount();
                    int unspecifiedSpec = MeasureSpec.makeMeasureSpec(0, 0);
                    int largestTabWidth = 0;
                    int gutter = 0;

                    int i;
                    View child;
                    for (i = count; gutter < i; ++gutter) {
                        child = this.getChildAt(gutter);
                        child.measure(unspecifiedSpec, heightMeasureSpec);
                        largestTabWidth = Math.max(largestTabWidth, child.getMeasuredWidth());
                    }

                    if (largestTabWidth <= 0) {
                        return;
                    }

                    gutter = SlidingTabStripLayout.this.dpToPx(16);
                    if (largestTabWidth * count <= this.getMeasuredWidth() - gutter * 2) {
                        for (i = 0; i < count; ++i) {
                            child = this.getChildAt(i);
                            LayoutParams lp = (LayoutParams) child.getLayoutParams();
                            lp.width = largestTabWidth;
                            lp.weight = 0.0F;
                        }
                    } else {
                        SlidingTabStripLayout.this.mTabGravity = 0;
                        SlidingTabStripLayout.this.updateTabViewsLayoutParams();
                    }

                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }

            }
        }

        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            if (!SlidingTabStripLayout.isAnimationRunning(this.getAnimation())) {
                this.updateIndicatorPosition();
            }

        }

        private void updateIndicatorPosition() {
            View selectedTitle = this.getChildAt(this.mSelectedPosition);
            int left;
            int right;
            if (selectedTitle != null && selectedTitle.getWidth() > 0) {
                left = selectedTitle.getLeft();
                right = selectedTitle.getRight();
                if (this.mSelectionOffset > 0.0F && this.mSelectedPosition < this.getChildCount() - 1) {
                    View nextTitle = this.getChildAt(this.mSelectedPosition + 1);
                    left = (int) (this.mSelectionOffset * (float) nextTitle.getLeft() + (1.0F - this.mSelectionOffset) * (float) left);
                    right = (int) (this.mSelectionOffset * (float) nextTitle.getRight() + (1.0F - this.mSelectionOffset) * (float) right);
                }
            } else {
                right = -1;
                left = -1;
            }

            this.setIndicatorPosition(left, right);
        }

        private void setIndicatorPosition(int left, int right) {
            if (left != this.mIndicatorLeft || right != this.mIndicatorRight) {
                this.mIndicatorLeft = left;
                this.mIndicatorRight = right;
                ViewCompat.postInvalidateOnAnimation(this);
            }

        }

        void animateIndicatorToPosition(final int position, int duration) {
            boolean isRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
            View targetView = this.getChildAt(position);
            final int targetLeft = targetView.getLeft();
            final int targetRight = targetView.getRight();
            final int startLeft;
            final int startRight;
            if (Math.abs(position - this.mSelectedPosition) <= 1) {
                startLeft = this.mIndicatorLeft;
                startRight = this.mIndicatorRight;
            } else {
                int animator = SlidingTabStripLayout.this.dpToPx(24);
                if (position < this.mSelectedPosition) {
                    if (isRtl) {
                        startLeft = startRight = targetLeft - animator;
                    } else {
                        startLeft = startRight = targetRight + animator;
                    }
                } else if (isRtl) {
                    startLeft = startRight = targetRight + animator;
                } else {
                    startLeft = startRight = targetLeft - animator;
                }
            }

            if (startLeft != targetLeft || startRight != targetRight) {
                ValueAnimatorCompat animator1 = ViewUtils.createAnimator();
                animator1.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
                animator1.setDuration(duration);
                animator1.setFloatValues(0.0F, 1.0F);
                animator1.setUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimatorCompat animator) {
                        float fraction = animator.getAnimatedFraction();
                        SlidingTabStrip.this.setIndicatorPosition(AnimationUtils.lerp(startLeft, targetLeft, fraction), AnimationUtils.lerp(startRight, targetRight, fraction));
                    }
                });
                animator1.setListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(ValueAnimatorCompat animator) {
                        SlidingTabStrip.this.mSelectedPosition = position;
                        SlidingTabStrip.this.mSelectionOffset = 0.0F;
                    }

                    public void onAnimationCancel(ValueAnimatorCompat animator) {
                        SlidingTabStrip.this.mSelectedPosition = position;
                        SlidingTabStrip.this.mSelectionOffset = 0.0F;
                    }
                });
                animator1.start();
            }

        }

        protected void onDraw(Canvas canvas) {
            // draw underline
            if (this.mUnderlineHeight > 0) {
                canvas.drawRect(1, (float) (this.getHeight() - mUnderlineHeight), (float) this.getWidth() + 1, (float) this.getHeight(), mUnderlinePaint);
            }
            if (this.mIndicatorLeft >= 0 && this.mIndicatorRight > this.mIndicatorLeft) {
                canvas.drawRect((float) this.mIndicatorLeft, (float) (this.getHeight() - this.mSelectedIndicatorHeight), (float) this.mIndicatorRight, (float) this.getHeight(), this.mSelectedIndicatorPaint);
            }

        }
    }

    class TabView extends LinearLayout implements OnLongClickListener {
        private final SlidingTabStripLayout.Tab mTab;
        private TextView mTextView;
        private ImageView mIconView;
        private View mCustomView;

        public TabView(Context context, SlidingTabStripLayout.Tab tab) {
            super(context);
            this.mTab = tab;
            if (SlidingTabStripLayout.this.mTabBackgroundResId != 0) {
                //this.setBackgroundDrawable(TintManager.getDrawable(context, SlidingTabStripLayout.this.mTabBackgroundResId));
                this.setBackgroundResource(SlidingTabStripLayout.this.mTabBackgroundResId);
            }

            ViewCompat.setPaddingRelative(this, SlidingTabStripLayout.this.mTabPaddingStart, SlidingTabStripLayout.this.mTabPaddingTop, SlidingTabStripLayout.this.mTabPaddingEnd, SlidingTabStripLayout.this.mTabPaddingBottom);
            this.setGravity(17);
            this.update();
        }

        public void setSelected(boolean selected) {
            //boolean changed = this.isSelected() != selected;
            super.setSelected(selected);
            if (selected) {
                this.sendAccessibilityEvent(4);
                if (this.mTextView != null) {
                    this.mTextView.setSelected(selected);
                    if (isTabSelectedTextBold) {
                        this.mTextView.setTextAppearance(this.getContext(), mTabSelectedTextAppearance);

                    }
                }

                if (this.mIconView != null) {
                    this.mIconView.setSelected(selected);
                }
            } else {
                this.mTextView.setTextAppearance(this.getContext(), mTabTextAppearance);
            }
        }

        @TargetApi(14)
        public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
            super.onInitializeAccessibilityEvent(event);
            event.setClassName(android.support.v7.app.ActionBar.Tab.class.getName());
        }

        @TargetApi(14)
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(info);
            info.setClassName(android.support.v7.app.ActionBar.Tab.class.getName());
        }

        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            if (SlidingTabStripLayout.this.mTabMaxWidth != 0 && this.getMeasuredWidth() > SlidingTabStripLayout.this.mTabMaxWidth) {
                super.onMeasure(MeasureSpec.makeMeasureSpec(SlidingTabStripLayout.this.mTabMaxWidth, 1073741824), heightMeasureSpec);
            } else if (SlidingTabStripLayout.this.mTabMinWidth > 0 && this.getMeasuredHeight() < SlidingTabStripLayout.this.mTabMinWidth) {
                super.onMeasure(MeasureSpec.makeMeasureSpec(SlidingTabStripLayout.this.mTabMinWidth, 1073741824), heightMeasureSpec);
            }

        }

        final void update() {
            SlidingTabStripLayout.Tab tab = this.mTab;
            View custom = tab.getCustomView();
            if (custom != null) {
                ViewParent icon = custom.getParent();
                if (icon != this) {
                    if (icon != null) {
                        ((ViewGroup) icon).removeView(custom);
                    }

                    this.addView(custom);
                }

                this.mCustomView = custom;
                if (this.mTextView != null) {
                    this.mTextView.setVisibility(View.GONE);
                }

                if (this.mIconView != null) {
                    this.mIconView.setVisibility(View.GONE);
                    this.mIconView.setImageDrawable((Drawable) null);
                }
            } else {
                if (this.mCustomView != null) {
                    this.removeView(this.mCustomView);
                    this.mCustomView = null;
                }

                Drawable icon1 = tab.getIcon();
                CharSequence text = tab.getText();
                if (icon1 != null) {
                    if (this.mIconView == null) {
                        ImageView hasText = new ImageView(this.getContext());
                        LayoutParams textView = new LayoutParams(-2, -2);
                        textView.gravity = 16;
                        hasText.setLayoutParams(textView);
                        this.addView(hasText, 0);
                        this.mIconView = hasText;
                    }

                    this.mIconView.setImageDrawable(icon1);
                    this.mIconView.setVisibility(View.VISIBLE);
                } else if (this.mIconView != null) {
                    this.mIconView.setVisibility(View.GONE);
                    this.mIconView.setImageDrawable((Drawable) null);
                }

                boolean hasText1 = !TextUtils.isEmpty(text);
                if (hasText1) {
                    if (this.mTextView == null) {
                        AppCompatTextView textView1 = new AppCompatTextView(this.getContext());
                        textView1.setTextAppearance(this.getContext(), SlidingTabStripLayout.this.mTabTextAppearance);
                        textView1.setMaxLines(2);
                        textView1.setEllipsize(TruncateAt.END);
                        textView1.setGravity(17);
                        if (SlidingTabStripLayout.this.mTabTextColors != null) {
                            textView1.setTextColor(SlidingTabStripLayout.this.mTabTextColors);
                        }

                        this.addView(textView1, -2, -2);
                        this.mTextView = textView1;
                    }

                    this.mTextView.setText(text);
                    this.mTextView.setContentDescription(tab.getContentDescription());
                    this.mTextView.setVisibility(View.VISIBLE);
                } else if (this.mTextView != null) {
                    this.mTextView.setVisibility(View.GONE);
                    this.mTextView.setText((CharSequence) null);
                }

                if (this.mIconView != null) {
                    this.mIconView.setContentDescription(tab.getContentDescription());
                }

                if (!hasText1 && !TextUtils.isEmpty(tab.getContentDescription())) {
                    this.setOnLongClickListener(this);
                } else {
                    this.setOnLongClickListener((OnLongClickListener) null);
                    this.setLongClickable(false);
                }
            }

        }

        public boolean onLongClick(View v) {
            int[] screenPos = new int[2];
            this.getLocationOnScreen(screenPos);
            Context context = this.getContext();
            int width = this.getWidth();
            int height = this.getHeight();
            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            Toast cheatSheet = Toast.makeText(context, this.mTab.getContentDescription(), Toast.LENGTH_SHORT);
            cheatSheet.setGravity(49, screenPos[0] + width / 2 - screenWidth / 2, height);
            cheatSheet.show();
            return true;
        }

        public SlidingTabStripLayout.Tab getTab() {
            return this.mTab;
        }
    }

    /*
     * <p>
     * The views used as tabs can be customized by calling {@link SlidingTabStripLayout.Tab.setCustomView(View)} Color(android.view.View)},
     * providing the layout ID of your custom layout.
     * <p/>
    * */
    public static final class Tab {
        public static final int INVALID_POSITION = -1;
        private Object mTag;
        private Drawable mIcon;
        private CharSequence mText;
        private CharSequence mContentDesc;
        private int mPosition = -1;
        private View mCustomView;
        private final SlidingTabStripLayout mParent;

        Tab(SlidingTabStripLayout parent) {
            this.mParent = parent;
        }

        public Object getTag() {
            return this.mTag;
        }

        public SlidingTabStripLayout.Tab setTag(Object tag) {
            this.mTag = tag;
            return this;
        }

        View getCustomView() {
            return this.mCustomView;
        }

        public SlidingTabStripLayout.Tab setCustomView(View view) {
            this.mCustomView = view;
            if (this.mPosition >= 0) {
                this.mParent.updateTab(this.mPosition);
            }

            return this;
        }

        public SlidingTabStripLayout.Tab setCustomView(int layoutResId) {
            return this.setCustomView(LayoutInflater.from(this.mParent.getContext()).inflate(layoutResId, (ViewGroup) null));
        }

        public Drawable getIcon() {
            return this.mIcon;
        }

        public int getPosition() {
            return this.mPosition;
        }

        void setPosition(int position) {
            this.mPosition = position;
        }

        public CharSequence getText() {
            return this.mText;
        }

        public SlidingTabStripLayout.Tab setIcon(Drawable icon) {
            this.mIcon = icon;
            if (this.mPosition >= 0) {
                this.mParent.updateTab(this.mPosition);
            }

            return this;
        }

        public SlidingTabStripLayout.Tab setIcon(int resId) {
            //return this.setIcon(TintManager.getDrawable(this.mParent.getContext(), resId));
            return null;
        }

        public SlidingTabStripLayout.Tab setText(CharSequence text) {
            this.mText = text;
            if (this.mPosition >= 0) {
                this.mParent.updateTab(this.mPosition);
            }

            return this;
        }

        public SlidingTabStripLayout.Tab setText(int resId) {
            return this.setText(this.mParent.getResources().getText(resId));
        }

        public void select() {
            this.mParent.selectTab(this);
        }

        public SlidingTabStripLayout.Tab setContentDescription(int resId) {
            return this.setContentDescription(this.mParent.getResources().getText(resId));
        }

        public SlidingTabStripLayout.Tab setContentDescription(CharSequence contentDesc) {
            this.mContentDesc = contentDesc;
            if (this.mPosition >= 0) {
                this.mParent.updateTab(this.mPosition);
            }

            return this;
        }

        public CharSequence getContentDescription() {
            return this.mContentDesc;
        }
    }

    public interface OnTabSelectedListener {
        void onTabSelected(SlidingTabStripLayout.Tab tab);

        void onTabUnselected(SlidingTabStripLayout.Tab tab);

        void onTabReselected(SlidingTabStripLayout.Tab tab);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface TabGravity {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }
}
