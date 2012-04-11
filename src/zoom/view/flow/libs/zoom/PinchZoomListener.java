package zoom.view.flow.libs.zoom;

import zoom.view.flow.libs.ViewFlow;
import zoom.view.flow.utils.ZoomFlags;
import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.util.FloatMath;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;

public class PinchZoomListener implements View.OnTouchListener {

  private int SWIPE_MIN_DISTANCE = 200;
  private static final float SWIPE_TRESHOLD_DELTA = (float) -0.031;
  private static final int SWIPE_MAX_OFF_PATH = 500;

  int swipeLeft = 0;
  int swipeRight = 0;

  private ViewFlow vf = null;

  private enum Mode {
    UNDEFINED, PAN, PINCHZOOM
  }

  /** Current listener mode */
  private Mode mMode = Mode.UNDEFINED;

  /** Zoom control to manipulate */
  private DynamicZoomControl mZoomControl;

  /** X-coordinate of previously handled touch event */
  private float mX;

  /** Y-coordinate of previously handled touch event */
  private float mY;

  /** X-coordinate of latest down event */
  private float mDownX;

  /** Y-coordinate of latest down event */
  private float mDownY;

  private PointF mMidPoint = new PointF();

  /** Velocity tracker for touch events */
  private VelocityTracker mVelocityTracker;

  /** Distance touch can wander before we think it's scrolling */
  private int mScaledTouchSlop;

  /** Maximum velocity for fling */
  private int mScaledMaximumFlingVelocity;

  /** Distance between fingers */
  private float oldDist = 1f;

  private long panAfterPinchTimeout = 0;

  private MyGestureListener myGestureListener;

  public ImageZoomView _currentView;

  public PinchZoomListener(Activity pContext) {

    mScaledTouchSlop = ViewConfiguration.get(pContext).getScaledTouchSlop();
    mScaledMaximumFlingVelocity = ViewConfiguration.get(pContext)
        .getScaledMaximumFlingVelocity();

    myGestureListener = new MyGestureListener(pContext);

  }

  /**
   * Sets the zoom control to manipulate
   * 
   * @param zc
   *          Zoom control
   */
  public void setZoomControl(DynamicZoomControl zc) {
    mZoomControl = zc;
  }

  public void setViewFlow(ViewFlow viewFlow) {
    vf = viewFlow;
  }

  public boolean onTouch(View v, MotionEvent event) {

    ImageZoomView _wr = (ImageZoomView) v;
    myGestureListener.detectEvents(_wr, event);
    final int action = event.getAction() & MotionEvent.ACTION_MASK;
    final float x = event.getX();
    final float y = event.getY();

    if (mVelocityTracker == null) {
      mVelocityTracker = VelocityTracker.obtain();
    }
    mVelocityTracker.addMovement(event);

    switch (action) {
    case MotionEvent.ACTION_DOWN:
      mZoomControl.stopFling();
      mDownX = x;
      mDownY = y;
      mX = x;
      mY = y;
      break;

    case MotionEvent.ACTION_POINTER_DOWN:
      if (event.getPointerCount() > 1) {
        oldDist = spacing(event);
        if (oldDist > 10f) {
          midPoint(mMidPoint, event);
          mMode = Mode.PINCHZOOM;
        }
      }
      break;

    case MotionEvent.ACTION_UP:
      if (mMode == Mode.PAN) {
        final long now = System.currentTimeMillis();
        if (panAfterPinchTimeout < now) {
          mVelocityTracker.computeCurrentVelocity(1000,
              mScaledMaximumFlingVelocity);
          mZoomControl.startFling(
              -mVelocityTracker.getXVelocity() / v.getWidth(),
              -mVelocityTracker.getYVelocity() / v.getHeight());
        }
      } else if (mMode != Mode.PINCHZOOM) {
        // mZoomControl.startFling(0, 0);
      }
      mVelocityTracker.recycle();
      mVelocityTracker = null;
    case MotionEvent.ACTION_POINTER_UP:
      if (event.getPointerCount() > 1 && mMode == Mode.PINCHZOOM) {
        panAfterPinchTimeout = System.currentTimeMillis() + 100;
      }
      mMode = Mode.UNDEFINED;
      break;

    case MotionEvent.ACTION_MOVE:
      final float dx = (x - mX) / v.getWidth();
      final float dy = (y - mY) / v.getHeight();

      if (mMode == Mode.PAN) {
        if (mZoomControl.getZoomState().getZoom() > 1f) {
          mZoomControl.pan(-dx, -dy);
        }
      } else if (mMode == Mode.PINCHZOOM) {
        float newDist = spacing(event);
        if (newDist > 10f) {
          final float scale = newDist / oldDist;
          final float xx = mMidPoint.x / v.getWidth();
          final float yy = mMidPoint.y / v.getHeight();
          mZoomControl.zoom(scale, xx, yy);
          _wr.updateImage();
          oldDist = newDist;
        }
      } else {
        final float scrollX = mDownX - x;
        final float scrollY = mDownY - y;

        final float dist = (float) Math.sqrt(scrollX * scrollX + scrollY
            * scrollY);
        if (mZoomControl.getZoomState().getZoom() > 1f) {
          if (dist >= mScaledTouchSlop) {
            mMode = Mode.PAN;
          }
        }
      }

      mX = x;
      mY = y;
      break;

    default:
      mVelocityTracker.recycle();
      mVelocityTracker = null;
      mMode = Mode.UNDEFINED;
      break;
    }
    return true;
  }

  /** Determine the space between the first two fingers */
  private float spacing(MotionEvent event) {
    float x = event.getX(0) - event.getX(1);
    float y = event.getY(0) - event.getY(1);
    return FloatMath.sqrt(x * x + y * y);
  }

  /** Calculate the mid point of the first two fingers */
  private void midPoint(PointF point, MotionEvent event) {
    float x = event.getX(0) + event.getX(1);
    float y = event.getY(0) + event.getY(1);
    point.set(x / 2, y / 2);
  }

  class MyGestureListener extends SimpleOnGestureListener implements
      OnTouchListener {
    Context context;
    private GestureDetector gDetector;

    public MyGestureListener() {
      super();
    }

    public MyGestureListener(Context context) {
      this(context, null);
    }

    public MyGestureListener(Context context, GestureDetector gDetector) {

      if (gDetector == null)
        gDetector = new GestureDetector(context, this);
      this.context = context;
      this.gDetector = gDetector;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
        float velocityY) {

      // do not do anything if the swipe does not reach a certain length
      // of distance
      if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {

        return false;
      }
      // right to left swipe

      if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
          && mZoomControl.getPanDeltaMax() < SWIPE_TRESHOLD_DELTA) {

        if (vf != null) {
          mZoomControl.zoom(1f, 0.5f, 0.5f);
          mZoomControl.resetPan();
          mZoomControl.resetZoomState();
          mZoomControl.update();
          ZoomFlags.unsetZoomed();
          // Show next page
          vf.showNextPage();
          vf.removeCallbacks(null);
        }

      }// left to right swipe
      else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
          && mZoomControl.getPanDeltaMin() < SWIPE_TRESHOLD_DELTA) {

        if (vf != null) {
          mZoomControl.zoom(1f, 0.5f, 0.5f);
          mZoomControl.resetPan();
          mZoomControl.resetZoomState();
          mZoomControl.update();
          ZoomFlags.unsetZoomed();
          // Show next page
          vf.showPreviousPage();
          vf.removeCallbacks(null);
        }

      }

      return super.onFling(e1, e2, velocityX, velocityY);
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
      doubleTapBehaviour(e);

      if (_currentView != null) {

        _currentView.updateImage();
      }
      return true;
    }

    public void doubleTapBehaviour(MotionEvent e) {

      if (mZoomControl.getZoomState().getZoom() < 1.1f) {
        float x = e.getX();
        float y = e.getY();
        float xx = x / _currentView.getWidth();
        float yy = y / _currentView.getHeight();
        mZoomControl.zoom(5f, xx, yy);
        ZoomFlags.setZoomed();
      } else {
        mZoomControl.resetZoomState();
        ZoomFlags.unsetZoomed();
      }
      mZoomControl.getZoomState().notifyObservers();

    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
      if (_currentView != null)
        _currentView.updateImage();
      if( ! _currentView.isHit( e.getX(), e.getY() ) )
        return false;
      return super.onSingleTapConfirmed(e);
    }

    public boolean onTouch(View v, MotionEvent event) {

      // Within the MyGestureListener class you can now manage the
      // event.getAction() codes.

      // Note that we are now calling the gesture Detectors onTouchEvent.
      // And given we've set this class as the GestureDetectors listener
      // the onFling, onSingleTap etc methods will be executed.
      return gDetector.onTouchEvent(event);
    }

    public GestureDetector getDetector() {
      return gDetector;
    }

    public void detectEvents(ImageZoomView _wr, MotionEvent e) {
      _currentView = _wr;
      gDetector.onTouchEvent(e);
    }
  }

}
