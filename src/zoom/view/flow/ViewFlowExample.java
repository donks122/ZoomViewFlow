package zoom.view.flow;

import java.io.File;

import zoom.view.flow.libs.ViewFlow;
import zoom.view.flow.libs.WrapMotionEvent;
import zoom.view.flow.libs.zoom.DynamicZoomControl;
import zoom.view.flow.libs.zoom.ImageZoomView;
import zoom.view.flow.libs.zoom.PinchZoomListener;
import zoom.view.flow.utils.Utils;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

public class ViewFlowExample extends Activity {
  private ViewFlow viewFlow;
  Context con;
  LibAdapter adapter;
  FrameLayout fl;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    fl = (FrameLayout) findViewById(R.id.viewflowfl);
    viewFlow = (ViewFlow) findViewById(R.id.viewflow);
    adapter = new LibAdapter(ViewFlowExample.this);
    viewFlow.setAdapter(adapter, 0);

  }


  public class LibAdapter extends BaseAdapter {

    private LayoutInflater mInflater;

    Display display;
    int width;
    int height;
    ViewHolder tmp;
    String[] content;

    private class ViewHolder {
      ImageZoomView imageView;
      View mContent;
    }

    public LibAdapter(Activity context) {

      mInflater = (LayoutInflater) context
      .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      content = getThumbs();

      display = getWindowManager().getDefaultDisplay();
      width = display.getWidth();
      height = display.getHeight();
    }

    public String getItem(int position) {
      return content[position];
    }

    public long getItemId(int position) {
      return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      return drawView(position, convertView, parent);
    }

    private View drawView(int position, View view, ViewGroup parent) {
      ViewHolder holder = null;
      if (view == null) {
        view = mInflater.inflate(R.layout.item, null);
        holder = new ViewHolder();
        holder.imageView =(ImageZoomView) view.findViewById(R.id.img1);
        holder.mContent = (View) view.findViewById(R.id.content);
        view.setTag(holder);
      } else {
        holder = (ViewHolder) view.getTag();
      }

      final String o = getItem(position);

      Bitmap b = null;
      File f = new File(o);
      b = Utils.decodeFile(f, 600, width);
      PinchZoomListener pc = new PinchZoomListener((Activity) con);

      DynamicZoomControl zc = new DynamicZoomControl();
      zc.resetZoomState();
      holder.imageView.setZoomState(zc.getZoomState());
      holder.imageView.setImage(b);
      holder.imageView.setFile(o);
      zc.setAspectQuotient(holder.imageView.getAspectQuotient());

      // Set view flow Hackety Hack
      pc.setViewFlow(viewFlow);

      pc.setZoomControl(zc);
      holder.imageView.setOnTouchListener(pc);
      holder.mContent.setVisibility(View.VISIBLE);
      return view;
    }

    public int getCount() {
      if (content == null)
        return 0;
      return content.length;
    }

    public float spacing(WrapMotionEvent event) {
      float x = event.getX(0) - event.getX(1);
      float y = event.getY(0) - event.getY(1);
      return FloatMath.sqrt(x * x + y * y);
    }

    private String[] getThumbs() {
      String[] bm = null;
      
      //populate image file path from FS to bm
      
      return bm;
    }

  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    finish();
    return super.onKeyDown(keyCode, event);
  }

  @Override
  protected void onDestroy() {
    viewFlow.removeCallbacks(null);
    System.gc();
    super.onDestroy();
  }

}
