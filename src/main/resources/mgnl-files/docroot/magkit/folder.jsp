<%@
    page import="info.magnolia.cms.util.Resource,
                 info.magnolia.cms.core.Content,
                 info.magnolia.cms.beans.config.ServerConfiguration"
%><%--
  Created by frank.sommer (16.04.2008)
--%><%@ page pageEncoding="ISO-8859-1"
         contentType="text/html; charset=UTF-8"
         session="false"
%><%
    ServerConfiguration configuration = ServerConfiguration.getInstance();
    if (!configuration.isAdmin() || Resource.showPreview()) {
        Content actpage = Resource.getActivePage();
        if (actpage != null && actpage.getLevel() > 0) {
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
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="de" lang="de">
<head>
    <cms:mainBar paragraph="folderProperties"/>
</head>
<body>
<div style="height:20px"></div><%-- set custom text in your properties --%>
<p><fmt:message key="folder.hint" var="folderHint" />
<c:choose>
    <c:when test="${not empty folderHint and not fn:contains(folderHint, 'folder.hint')}">${folderHint}</c:when>
    <c:otherwise>This is a folder. There is no content to show. <br />
    For excluding from navigation save once the page properties.</c:otherwise>
</c:choose>
</p>
</body>
</html>