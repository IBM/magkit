package com.aperto.magkit.dialogs;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.module.admininterface.dialogs.ParagraphEditDialog;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Dialog handler that allows execution of arbitrary JS on closing dialog.
 * @author jan haderka
 */
public class CallbackEditDialog extends ParagraphEditDialog {
    private static final Logger LOGGER = Logger.getLogger(CallbackEditDialog.class);

    /**
     * JS to be executed on close.
     */
    private String _jsExecutedAfterSaving;

    /**
     * Config node.
     */
    private Content _configNode;
    
    /**
     * Creates new edit dialog.
     * @param name Dialog name.
     * @param request Incoming http request.
     * @param response Outgoing http response.
     * @param configNode Configuration node.
     */
    public CallbackEditDialog(String name, HttpServletRequest request, HttpServletResponse response, Content configNode) {
        super(name, request, response, configNode);
        _configNode = configNode;
    }

    /**
     * Renders dialog html.
     * @see info.magnolia.cms.servlets.MVCServletHandler#renderHtml(java.lang.String)
     */
    public void renderHtml(String view) throws IOException {
        PrintWriter out = getResponse().getWriter();
        
        // after saving
        if (VIEW_CLOSE_WINDOW.equals(view)) {
            out.println("<html>"); 
            out.println(new Sources(getRequest().getContextPath()).getHtmlJs());
            out.println("<script type=\"text/javascript\">"); 
            out.println("var path = '" + path + "'");
            out.println(StringUtils.defaultIfEmpty(_configNode.getNodeData("jsExecutedAfterSaving").getString(), "mgnlDialogReloadOpener();")); 
            out.println("</script></html>"); 
        } else if (VIEW_SHOW_DIALOG.equals(view)) {
            try {
                getDialog().drawHtml(out);
            } catch (IOException e) {
                LOGGER.error("Exception caught", e);
            }
        }
    }
    
    /**
     * Gets arbitrary java script to execute when closing edit dialog.
     * @return script to be executed.
     */
    public String getJsExecutedAfterSaving() {
        return _jsExecutedAfterSaving;
    }


    /**
     * Sets arbitrary java script to execute when closing edit dialog.
     * @param jsExecutedAfterSaving script to be executed.
     */
    public void setJsExecutedAfterSaving(String jsExecutedAfterSaving) {
        _jsExecutedAfterSaving = jsExecutedAfterSaving;
    }

}
