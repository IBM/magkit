<%@ page import="java.util.MissingResourceException,
        java.util.ResourceBundle"
%><%
    ResourceBundle resourceBundle = ResourceBundle.getBundle("environment");
    if (resourceBundle != null) {
        try {
            String handle = resourceBundle.getString("errorpage.404." + request.getLocale().getLanguage());
            String contextName = request.getContextPath();
            String newLocn = contextName + handle;
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            pageContext.forward(newLocn);
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