<%@ page import="org.apache.commons.lang.StringUtils,
                 java.util.MissingResourceException,
                 java.util.ResourceBundle"
%><%
    ResourceBundle resourceBundle = ResourceBundle.getBundle("environment");
    if (resourceBundle != null) {
        try {
            String mandant = "";
            String language = request.getLocale().getLanguage();
            String contextPath = request.getContextPath();
            String originUri = pageContext.getErrorData().getRequestURI();
            if (StringUtils.isNotBlank(originUri)) {
                int startLevel = 0;
                if (StringUtils.isNotBlank(contextPath) && originUri.startsWith(contextPath)) {
                    startLevel++;
                }
                String[] parts = StringUtils.split(originUri, '/');
                if (parts.length > startLevel) {
                    mandant = parts[startLevel];
                }
                startLevel++;
                if (parts.length > startLevel) {
                    language = parts[startLevel];
                }
            }
            String handle = resourceBundle.getString("errorpage.404");
            try {
                handle = resourceBundle.getString("errorpage.404." + mandant + "." + language);
            } catch (MissingResourceException mre) {
                try {
                    handle = resourceBundle.getString("errorpage.404." + mandant);
                } catch (MissingResourceException e) {
                    //do nothing
                }
            }
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            pageContext.forward(handle);
        } catch (MissingResourceException mre) {
%>
    404-Seite ist nicht konfiguriert ("errorpage.404").
<%
        }
    } else {
%>
    Die 404-Seite ist nicht konfiguriert ("environment").
<%
    }
%>