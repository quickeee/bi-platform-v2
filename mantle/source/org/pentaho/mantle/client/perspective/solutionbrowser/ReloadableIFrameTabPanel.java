/*
 * Copyright 2008 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.client.perspective.solutionbrowser;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ReloadableIFrameTabPanel extends VerticalPanel implements IReloadableTabPanel {

  String url;
  Frame frame;

  public ReloadableIFrameTabPanel(String url) {
    this.url = url;
    frame = new CustomFrame(url);
    add(frame);
  }

  public void reload() {
    this.frame.setUrl(
        getCurrentUrl()
    );
  }
  
  /*
   * frame.getUrl returns the original URL, but not the current one. This method accesses the
   * DOM directly to get that URL
   */
  private String getCurrentUrl(){
    return IFrameElement.as(this.frame.getElement()).getContentDocument().getURL();
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void openTabInNewWindow() {
    Window.open(getCurrentUrl(), "_blank", "");
  }

  public Frame getFrame() {
    return frame;
  }

  public void setFrame(Frame frame) {
    this.frame = frame;
  }
  
  public class CustomFrame extends Frame{
    private CustomFrame(){
      super();
    }
    
    private CustomFrame(String url){
      super(url);
    }
    
    public native void attachEventListeners(Element ele)/*-{
      var iwind = ele.contentWindow; //IFrame's window instance
      
      var funct = function(event){
        event = iwind.parent.translateInnerMouseEvent(ele, event);
        iwind.parent.sendMouseEvent(event);
      }  
      
      // Hooks up mouse and unload events
      $wnd.hookEvents = function(wind){
        if(wind == null){
          wind = $wnd.watchWindow
        }
        wind.onmouseup = funct;
        wind.onmousedown = funct;
        wind.onmousemove = funct;
        
        wind.onunload = unloader;
        $wnd.previousLocation = null;
        $wnd.watchWindow = null;
      }
      
      // IFrame URL watching code
      
      // Called on iFrame unload, calls containing Window to start monitoring it for Url change
      var unloader = function(event){
        $wnd.startIFrameWatcher(iwind.location.href, iwind);
      }
      
      // Starts the watching loop.
      $wnd.startIFrameWatcher = function(href, wind){
        $wnd.previousLocation = href;
        $wnd.watchWindow = wind;
        $wnd.setTimeout("rehookEventsTimer()", 300);
      }
    
      // loop that's started when an iFrame unloads, when the url changes it adds back in the hooks
      $wnd.rehookEventsTimer = function(){
        if($wnd.watchWindow.location.href != $wnd.previousLocation){
          //location changed hook back up event interceptors
          $wnd.setTimeout("hookEvents()", 300);
        } else {
          $wnd.setTimeout("rehookEventsTimer()", 300);
        }
      }
      
      // Scope helper funct.
      function rehookEventsTimer(){
        $wnd.rehookEventsTimer();
      }
      
      //Hook up the mouse and unload event handlers for iFrame being created
      $wnd.hookEvents(iwind);
      
      
      
    }-*/;
  }
}
