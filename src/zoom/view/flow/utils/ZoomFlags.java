package zoom.view.flow.utils;

public class ZoomFlags {
  
  private static boolean _zoomed = false;
    
  public static boolean isZoomed() {
    return _zoomed;
  }
  
  public static void setZoomed() {
    _zoomed = true;
  }
  
  public static void unsetZoomed() {
    _zoomed = false;
  }

}
