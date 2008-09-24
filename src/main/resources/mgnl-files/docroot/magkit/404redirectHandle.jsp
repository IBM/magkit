<%@ page import="org.apache.commons.lang.StringUtils,
        java.util.MissingResourceException, java.util.ResourceBundle"
%><%
    ResourceBundle resourceBundle = ResourceBundle.getBundle("environment");
    if (resourceBundle != null) {
        try {
            String language = request.getLocale().getLanguage();
            String contextPath = request.getContextPath();
            String originUri = pageContext.getErrorData().getRequestURI();
            if (StringUtils.isNotBlank(originUri)) {
                int langLevel = 1;
                if (StringUtils.isNotBlank(contextPath) && originUri.startsWith(contextPath)) {
                    langLevel = 2;
                }
                String[] parts = StringUtils.split(originUri, '/');
                if (parts.length > langLevel + 1) {
                    language = parts[langLevel];
                }
            }
            String handle = "";
            try {
                handle = resourceBundle.getString("errorpage.404." + language);
            } catch (MissingResourceException mre) {
                handle = resourceBundle.getString("errorpage.404");
            }
            String newLocn = contextPath + handle;
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