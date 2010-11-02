<%@ page pageEncoding="Windows-1252"
         contentType="text/html; charset=UTF-8"
         session="false"
         import="com.aperto.webkit.utils.StringTools,
                 info.magnolia.cms.core.Content,
                 info.magnolia.cms.core.HierarchyManager,
                 info.magnolia.cms.core.ItemType,
                 info.magnolia.context.MgnlContext, java.util.Collection"
%><%
    String currentNode = request.getParameter("currentNode");
    Content content = null;
    if (!StringTools.isBlank(currentNode)) {
        String[] path = currentNode.split("\\.");
        String repository = path[0];
        HierarchyManager hm = MgnlContext.getHierarchyManager(repository);
        StringBuilder nodePath = new StringBuilder("/");
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
            content = hm.getContent(nodePath.toString());
        }
    }
    if (content != null) {
        out.print(content.getName() + ";");
        boolean hasNodeData = content.getNodeDataCollection().size() > 0;
        if (ItemType.CONTENT.equals(content.getItemType()) && !hasNodeData) {
            Collection<Content> children = content.getChildren(ItemType.CONTENTNODE);
            children.addAll(content.getChildren(ItemType.CONTENT));
            for (Object o : children) {
                Content child = (Content) o;
                ItemType type = child.getItemType();
                if (ItemType.CONTENT.equals(type)|| ItemType.CONTENTNODE.equals(type)) {
                    out.print(child.getName() + ";");
                }
            }
        }
    }
%>