<%@ page import="info.magnolia.cms.core.Content,
        info.magnolia.cms.util.ContentUtil,
        info.magnolia.cms.beans.config.ContentRepository"
%><%
    Content content = ContentUtil.getContent(ContentRepository.WEBSITE, "/config/" + request.getLocale().getLanguage() + "centralHandles");
    if (content != null && content.hasNodeData("404Link")) {
        String handle = content.getNodeData("404Link").getString();
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