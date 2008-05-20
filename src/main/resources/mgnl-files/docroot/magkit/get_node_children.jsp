<%@
    page pageEncoding="Windows-1252"
         contentType="text/html; charset=UTF-8"
         session="false"
         import="com.aperto.webkit.utils.StringTools,
                 info.magnolia.cms.core.Content,
                 info.magnolia.cms.core.HierarchyManager,
                 info.magnolia.cms.core.ItemType,
                 info.magnolia.context.MgnlContext,
                 java.util.Iterator"
%><%@
    include file="/WEB-INF/jspf/begin.jspf"
%><%
    String currentNode = request.getParameter("currentNode");
    LOGGER.info("currentNode: " + currentNode);
    Content content = null;
    if (!StringTools.isBlank(currentNode)) {
        String[] path = currentNode.split("\\.");
        LOGGER.info("Path: " + path.length);
        String repository = path[0];
        LOGGER.info("get HierarchyManager: " + repository);
        HierarchyManager hm = MgnlContext.getHierarchyManager(repository);
        StringBuffer nodePath = new StringBuffer("/");
        for (int i = 1; i < path.length; i++) {
            String s = path[i];
            if (i > 1) {
                nodePath.append("/");
            }
            nodePath.append(s);
        }
        if ("/".equals(nodePath.toString())) {
            content = hm.getRoot();   
        } else {
            content = hm.getContent( nodePath.toString());
        }
    }
    if (content != null) {
        out.print(content.getName() + ";");
        boolean hasNodeData = content.getNodeDataCollection().size() > 0; // deprecated type: ItemType.NT_NODEDATA
        LOGGER.info("hasNodeData: " + hasNodeData);
        if (ItemType.CONTENT.equals(content.getItemType()) && !hasNodeData) {
            LOGGER.info("about to iterate " + content.getChildren(null, "*").size());
            for (Iterator i = content.getChildren(null, "*").iterator(); i.hasNext();) {
                Content child = (Content) i.next();
                if (ItemType.CONTENT.equals(child.getItemType())|| ItemType.CONTENTNODE.equals(child.getItemType())) {
    //                out.println(child.getName() + " - " + child.getItemType().toString() + ";");
                    out.print(child.getName() + ";");
                    LOGGER.info("child name: " + child.getName());
                }
            }
        } else {
            // root - just return list of subnodes
            for (Object o :content.getChildren()) {
                LOGGER.info("child name: " + o);
                out.print(((Content)o).getName() + ";");
            }
        }
    }
%><%@
    include file="/WEB-INF/jspf/end.jspf"
%>
