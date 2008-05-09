<%@ page import="info.magnolia.cms.core.Content,
        info.magnolia.cms.util.ContentUtil"
%><%
    Content content = ContentUtil.getContent("config", "/modules/magkit/config/404");
    if (content != null && content.hasNodeData("handle")) {
        String handle = content.getNodeData("handle").getString();
        response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        String contextName = request.getContextPath();
        String newLocn = contextName + handle;
        response.setHeader("Location", newLocn);
    } else {
%>
    Die 404-Seite ist nicht konfiguriert.
<%
    }
%>