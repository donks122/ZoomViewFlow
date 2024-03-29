/*
 * Copyright 2010 Sony Ericsson Mobile Communications AB
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package zoom.view.flow.libs.zoom;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import zoom.view.flow.utils.Utils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * View capable of drawing an image at different zoom state levels
 */
public class ImageZoomView extends View implements Observer {

    /** Paint object used when drawing bitmap. */
    private final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);

    /** Rectangle used (and re-used) for cropping source image. */
    private final Rect mRectSrc = new Rect();

    /** Rectangle used (and re-used) for specifying drawing area on canvas. */
    private final Rect mRectDst = new Rect();

    /** Object holding aspect quotient */
    private final AspectQuotient mAspectQuotient = new AspectQuotient();

    /** The bitmap that we're zooming in, and drawing on the screen. */
    private Bitmap mBitmap;

    /** State of the zoom. */
    private ZoomState mState;
    
    private String _file = null;
    
    private boolean isHiRes = false;

    // Public methods

    /**
     * Constructor
     */
    public ImageZoomView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public Rect destinationRectangle() {
      return mRectDst;
    }
    
    public Rect sourceRectangle() {
      return mRectSrc;
    }
    
    public ZoomState zoomState() {
      return mState;
    }

    /**
     * Set image bitmap
     * 
     * @param bitmap The bitmap to view and zoom into
     */
    public void setImage(Bitmap bitmap) {
        mBitmap = bitmap;

        mAspectQuotient.updateAspectQuotient(getWidth(), getHeight(), mBitmap.getWidth(), mBitmap
                .getHeight());
        mAspectQuotient.notifyObservers();

        invalidate();
    }

    /**
     * Set object holding the zoom state that should be used
     * 
     * @param state The zoom state
     */
    public void setZoomState(ZoomState state) {
        if (mState != null) {
            mState.deleteObserver(this);
        }

        mState = state;
        mState.addObserver(this);

        invalidate();
    }
    
    public void setFile( String _f ) {
      _file = _f;
    }

    /**
     * Gets reference to object holding aspect quotient
     * 
     * @return Object holding aspect quotient
     */
    public AspectQuotient getAspectQuotient() {
        return mAspectQuotient;
    }

    // Superclass overrides
    
    public int getImageWidth() {
    	return ( mRectDst.right - mRectDst.left );
    }
    
    public boolean isHit( float x, float y ) {
      return ( x >= mRectDst.left && x <= mRectDst.right && y >= mRectDst.top && y <= mRectDst.bottom );
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null && mState != null) {
            final float aspectQuotient = mAspectQuotient.get();

            final int viewWidth = getWidth();
            final int viewHeight = getHeight();
            final int bitmapWidth = mBitmap.getWidth();
            final int bitmapHeight = mBitmap.getHeight();

            final float panX = mState.getPanX();
            final float panY = mState.getPanY();
            final float zoomX = mState.getZoomX(aspectQuotient) * viewWidth / bitmapWidth;
            final float zoomY = mState.getZoomY(aspectQuotient) * viewHeight / bitmapHeight;

            // Setup source and destination rectangles
            mRectSrc.left = (int)(panX * bitmapWidth - viewWidth / (zoomX * 2));
            mRectSrc.top = (int)(panY * bitmapHeight - viewHeight / (zoomY * 2));
            mRectSrc.right = (int)(mRectSrc.left + viewWidth / zoomX);
            mRectSrc.bottom = (int)(mRectSrc.top + viewHeight / zoomY);
            mRectDst.left = getLeft();
            mRectDst.top = getTop();
            mRectDst.right = getRight();
            mRectDst.bottom = getBottom();

            // Adjust source rectangle so that it fits within the source image.
            if (mRectSrc.left < 0) {
                mRectDst.left += -mRectSrc.left * zoomX;
                mRectSrc.left = 0;
            }
            if (mRectSrc.right > bitmapWidth) {
                mRectDst.right -= (mRectSrc.right - bitmapWidth) * zoomX;
                mRectSrc.right = bitmapWidth;
            }
            if (mRectSrc.top < 0) {
                mRectDst.top += -mRectSrc.top * zoomY;
                mRectSrc.top = 0;
            }
            if (mRectSrc.bottom > bitmapHeight) {
                mRectDst.bottom -= (mRectSrc.bottom - bitmapHeight) * zoomY;
                mRectSrc.bottom = bitmapHeight;
            }

            canvas.drawBitmap(mBitmap, mRectSrc, mRectDst, mPaint);
        }
    }

    
    public  void updateImage() {
      
      if( _file != null ) {
        
        if( mState.getZoom() > 2 && ! isHiRes ) {
          mBitmap = Utils.decodeFile( new File( _file ), 2048, 1280 ) ;
          setImage( mBitmap );
          isHiRes = true;
        }
        else {
          if( mState.getZoom() <= 2 && isHiRes ) {
            mBitmap = Utils.decodeFile( new File( _file ), 600, 1280 );
            setImage( mBitmap );
            isHiRes = false;
          }
        }
      } 
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        
        mAspectQuotient.updateAspectQuotient(right - left, bottom - top, mBitmap.getWidth(),
                mBitmap.getHeight());
        mAspectQuotient.notifyObservers();
    }

    // implements Observer
    public void update(Observable observable, Object data) {
        invalidate();
    }

    public Drawable getDrawable() {
      return new BitmapDrawable(getResources(),mBitmap);
    }

}
