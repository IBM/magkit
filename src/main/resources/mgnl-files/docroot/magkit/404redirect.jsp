<%@ page import="java.util.MissingResourceException,
        java.util.ResourceBundle"
%><%
    ResourceBundle resourceBundle = ResourceBundle.getBundle("environment");
    if (resourceBundle != null) {
        try {
            String handle = "";
            try {
                handle = resourceBundle.getString("errorpage.404." + request.getLocale().getLanguage());
            } catch (MissingResourceException mre) {
                handle = resourceBundle.getString("errorpage.404");
            }
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