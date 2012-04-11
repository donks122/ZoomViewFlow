package zoom.view.flow.utils;



/*
 * A class to hold all different static flags
 * used by the class all over the place
 */
public class AppFlags {

  
  /*
   * is_zoomed flag used in Preview and Libview to toggle
   * the event handling between ViewFlow and PinchZoomListener
   */
  private static boolean _zoomed = false;
  
  /*
   * _download_progress flag is set when stuff is being
   * downloaded, should be unset when download settles in
   * should be used by all items in app that needs to know 
   * about any download progress
   */
  
  /*
   * Used to present webviews to ban presentation of events when nesseccary
   */
  public static boolean ban_event = false;
  
  
  public static void banEvents() {
    ban_event = true;
  }
  
  public static void allowEvents() {
    ban_event = false;
  }
  
  public static boolean isEventBanned() {
    return ban_event;
  }
  
  /*
   * returns true if the any view is currently in a zoomed state
   */
  public static boolean isZoomed() {
    return _zoomed;
  }
  
  /*
   * Set when a view enters zoomed state
   */
  public static void setZoomed() {
    _zoomed = true;
  }
  
  /*
   * Unset when a view moves out of zoomed state
   */
  public static void unsetZoomed() {
    _zoomed = false;
  }
  
  
}
