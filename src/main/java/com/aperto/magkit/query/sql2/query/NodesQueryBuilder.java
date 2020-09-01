package com.aperto.magkit.query.sql2.query;

import com.aperto.magkit.query.sql2.query.jcrwrapper.NodesQuery;

import javax.jcr.Node;
import java.util.List;

/**
 * The NodesQueryBuilder interface declaring methods for the last step: building and executing.
 *
 * @author wolf.bubenik@aperto.com
 * @since (27.04.20)
 */
public interface NodesQueryBuilder extends QueryLimit<NodesQueryBuilder> {
    NodesQuery buildNodesQuery();
    List<Node> getResultNodes();
}
