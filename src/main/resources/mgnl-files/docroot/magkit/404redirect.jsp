<%@ page import="com.aperto.webkit.utils.ExceptionEater,
        java.util.MissingResourceException,
        java.util.ResourceBundle"
%><%
    ResourceBundle resourceBundle = ResourceBundle.getBundle("environment");
    if (resourceBundle != null) {
        try {
            String handle = resourceBundle.getString("errorpage.404." + request.getLocale().getLanguage());
            String contextName = request.getContextPath();
            String newLocn = contextName + handle;
            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            response.setHeader("Location", newLocn);
        } catch (MissingResourceException mre) {
%>
            404-Seite ist nicht konfiguriert.
<%
        }
    } else {
%>
    Die 404-Seite ist nicht konfiguriert.
<%
    }
%>