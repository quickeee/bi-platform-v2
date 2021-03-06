<%@page import="java.util.Locale"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="org.pentaho.platform.util.messages.LocaleHelper"%>
<%@page import="java.net.URLClassLoader"%>
<%@page import="java.net.URL"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="java.io.File"%>

<%
  Locale effectiveLocale = request.getLocale(); 
  if (!StringUtils.isEmpty(request.getParameter("locale"))) {
    effectiveLocale = new Locale(request.getParameter("locale"));
    request.getSession().setAttribute("locale_override", request.getParameter("locale"));
    LocaleHelper.setLocaleOverride(effectiveLocale);
  }

  URLClassLoader loader = new URLClassLoader(new URL[] {application.getResource("/mantle/messages/")});
  ResourceBundle properties = ResourceBundle.getBundle("messages", request.getLocale(), loader); 
%>

<html>
	<head>
		<title>Pentaho User Console</title>
		<meta name="gwt:property" content="locale=<%=effectiveLocale%>">
		<link rel="shortcut icon" href="/pentaho-style/favicon.ico" />
		<link rel='stylesheet' href='mantle/MantleStyle.css'/>
    <script type="text/javascript" src="mantle/pngfix.js"></script>
    <script type="text/javascript">
      if(window.opener && window.opener.reportWindowOpened != undefined){
        window.opener.reportWindowOpened();
      }
      PngFix.baseURL = window.location.href.substring(0,window.location.href.lastIndexOf("/"))+"/mantle/";
      PngFix.spacerURL = PngFix.baseURL+"images/spacer.gif";
      
    </script>
	</head>

	<body oncontextmenu="return false;">

	<div id="loading">
    		<div class="loading-indicator">
    			<img src="mantle/large-loading.gif" width="32" height="32"/><%= properties.getString("loadingConsole") %><a href="http://www.pentaho.com"></a><br/>
    			<span id="loading-msg"><%= properties.getString("pleaseWait") %></span>
    		</div>
	</div>
		
	<!-- OPTIONAL: include this if you want history support -->
	<iframe id="__gwt_historyFrame" style="width:0;height:0;border:0"></iframe>

	</body>

	<script language='javascript' src='mantle/mantle.nocache.js'></script>

</html>
