<%@ page import="info.magnolia.cms.util.Resource,
        info.magnolia.cms.core.Content,
        info.magnolia.cms.beans.config.ServerConfiguration"
%><%--
  Created by frank.sommer (16.04.2008)
--%><%@ page pageEncoding="ISO-8859-1"
         contentType="text/html; charset=UTF-8"
         session="false"
%><%@ include file="begin.jspf" %><%
    ServerConfiguration configuration = ServerConfiguration.getInstance();
    if (!configuration.isAdmin() || Resource.showPreview()) {
        Content actpage = Resource.getActivePage();
        if (actpage != null && actpage.getLevel() > 1) {
            Content parent = actpage.getParent();
            if (parent != null) {
                String handle = request.getContextPath();
                if (!"/".equals(parent.getHandle())) {
                    handle += parent.getHandle();
                    handle += ".";
                    handle += configuration.getDefaultExtension();
                }
                response.sendRedirect(handle);
            }
        }
    }
%><%@ taglib prefix="cms" uri="cms-taglib"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="de" lang="de">
<head>
    <cms:mainBar paragraph="folderProperties"/>
</head>
<body>
<div style="height:20px"></div>
<p>
    This is a folder. There is no content to show. <br />
    For excluding from navigation save once the page properties.
</p>
</body>
</html>
<%@ include file="end.jspf" %>