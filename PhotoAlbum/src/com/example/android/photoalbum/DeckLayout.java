/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.photoalbum;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

/**
 * Displays children as a deck. This layout handles gestures to show/hide
 * the various cards of the deck.
 */
@SuppressWarnings({"UnusedDeclaration"})
public class DeckLayout extends FrameLayout {
    private static final int INVALID_POINTER = -1;

    private int mActivePointerId = INVALID_POINTER;
    private boolean mIsTrackingGesture;
    private float mMotionX;

    private final int mTouchSlop;
    private final int mMinimumVelocity;
    private final int mMaximumVelocity;

    private VelocityTracker mVelocityTracker;

    private int mCurrentCard = 0;
    
    private Bitmap mShadow;
    private Rect mSrc;
    private final Rect mDst = new Rect();

    public DeckLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DeckLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();        
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        mShadow = BitmapFactory.decodeResource(context.getResources(), R.drawable.photo_shadow);
        mSrc = new Rect(0, 0, mShadow.getWidth(), mShadow.getHeight());
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        final int count = getChildCount();
        for (int i = 1; i < count; i++) {
            final View child = getChildAt(i);
            
            final int left = (int) (child.getX() + child.getWidth());
            mDst.set(left, 0, left + mShadow.getWidth(), getHeight());

            canvas.drawBitmap(mShadow, mSrc, mDst, null);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        if (action == MotionEvent.ACTION_MOVE && mIsTrackingGesture) {
            return true;
        }

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE: {
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    break;
                }

                final int pointerIndex = event.findPointerIndex(activePointerId);
                final float x = event.getX(pointerIndex);

                final int xDiff = (int) Math.abs(x - mMotionX);

                if (xDiff > mTouchSlop) {
                    mIsTrackingGesture = true;
                }

                mMotionX = x;
            } break;

            case MotionEvent.ACTION_DOWN: {
                if (mCurrentCard == getChildCount() - 1 && isInBottomChild(event)) {
                    break;
                }
                mActivePointerId = event.getPointerId(0);
                mMotionX = event.getX(0);
            } break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                mActivePointerId = INVALID_POINTER;
                mIsTrackingGesture = false;
            } break;

            case MotionEvent.ACTION_POINTER_UP: {
                onSecondaryPointerUp(event);
            } break;
        }

        return mIsTrackingGesture;
    }

    private boolean isInBottomChild(MotionEvent event) {
        final float x = event.getX(0);
        final float y = event.getY(0);

        final View child = getChildAt(0);
        return !(y < child.getTop() || y >= child.getBottom() ||
                x < child.getLeft() || x >= child.getRight());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_UP: {
                onSecondaryPointerUp(event);
            } break;
    
            case MotionEvent.ACTION_CANCEL: {
                mVelocityTracker.clear();

                mActivePointerId = INVALID_POINTER;
                mIsTrackingGesture = false;
            } break;

            case MotionEvent.ACTION_UP: {
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int initialVelocity = (int) velocityTracker.getXVelocity(mActivePointerId);

                if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
                    revealCard(initialVelocity);
                }

                velocityTracker.clear();

                mActivePointerId = INVALID_POINTER;
                mIsTrackingGesture = false;
            } break;
        }
    
        return true;
    }

    private void onSecondaryPointerUp(MotionEvent event) {
        final int pointerIndex = event.getActionIndex();
        final int pointerId = event.getPointerId(pointerIndex);

        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = event.getPointerId(newPointerIndex);
        }
    }

    private void revealCard(int velocity) {
        // Swipe to the left, show next card
        if (velocity < 0) {
            showNextCard();
        } else {
            showPreviousCard();
        }
    }

    /**
     * Shows the next card, making it visible in the process.
     */
    private void showNextCard() {
        if (mCurrentCard == getChildCount() - 1) return;
        mCurrentCard++;

        getChildAt(getChildCount() - 1 - mCurrentCard).setVisibility(VISIBLE);

        for (int i = 0; i < mCurrentCard; i++) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(getChildAt(getChildCount() - 1 - i),
                    "x", (mCurrentCard - i) * -getWidth() / 3);
            animator.setDuration(200);
            animator.start();
        }
    }

    /**
     * Shows the previous card and hides the card that becomes invisible.
     */
    private void showPreviousCard() {
        if (mCurrentCard == 0) return;
        mCurrentCard--;

        ObjectAnimator animator = null;
        for (int i = 0; i <= mCurrentCard; i++) {
            animator = ObjectAnimator.ofFloat(getChildAt(getChildCount() - 1 - i),
                    "x", (mCurrentCard - i) * -getWidth() / 3);
            animator.setDuration(200);
            animator.start();
        }

        // Hides the bottom-most card
        if (animator != null) {
            final View previous = getChildAt(getChildCount() - 1 - mCurrentCard - 1);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    previous.setVisibility(INVISIBLE);
                }
            });
        }
    }
}
