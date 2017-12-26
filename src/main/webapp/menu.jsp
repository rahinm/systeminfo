<!-- 

Title : menu.html

Purpose : This is the navigation menu for the site.

Classes : None

External Links : None

-->
<?xml version="1.0"?>
<%@page contentType="text/html" %>
<%@page import="java.io.IOException,java.net.InetAddress" %>
<%@page import="net.dollmar.systeminfo.SystemInfoServlet" %>


<!DOCTYPE html
PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%
    String hostname = "";
    try {
        hostname = InetAddress.getLocalHost().getHostName();
    } catch (IOException e) {
    }
    
    String versionNum = SystemInfoServlet.getVersion();
%>


<html>
 <head>
  <title>SystemInfo Menu </title>
  
  <link rel="stylesheet" type="text/css" href="StyleSheet/SystemInfo.css" />
  <meta http-equiv="cache-control" content="no-cache"/>        
 </head>
        
 <body bgcolor="#b8d6ca" leftmargin="10" rightmargin="10" topmargin="20">

  <table width="225" cellspacing="10" cellpadding="0" border="0">
   <tr>
    <td> </td>
   </tr>
   <tr> <td><h3>Version: <%= versionNum%></h3></td> </tr>
   <tr> <td>Host: <%= hostname%></td> </tr>
  </table>

  <table width="225" cellspacing="10" cellpadding="0" border="0">
   <tr>
    <td><h2>Info Choices</h2></td>
   </tr>
   <tr>
    <td>
     <li><a href="SystemInfo?function=home" target="main_frame">Info Home</a></li>
     <li><a href="SystemInfo?function=hostInfo" target="main_frame">Host Info</a></li>
     <li><a href="SystemInfo?function=jvmInfo" target="main_frame">JVM Info</a></li>
     <li><a href="SystemInfo?function=propsInfo" target="main_frame">Properties Info</a></li>
     <li><a href="SystemInfo?function=classLoadingInfo" target="main_frame">ClassLoading Info</a></li>
     <li><a href="SystemInfo?function=memInfo" target="main_frame">Memory Info</a></li>
     <li><a href="SystemInfo?function=gcInfo" target="main_frame">GC Info</a></li>
     <li><a href="SystemInfo?function=threadsInfo" target="main_frame">Threads Info</a></li>     
    </td>
   </tr>
</table>        
</html>
