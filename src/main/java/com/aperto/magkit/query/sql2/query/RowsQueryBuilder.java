package com.aperto.magkit.query.sql2.query;

import com.aperto.magkit.query.sql2.query.jcrwrapper.RowsQuery;

import javax.jcr.query.Row;
import java.util.List;

/**
 * The RowsQueryBuilder interface declaring methods for the last building step.
 *
 * @author wolf.bubenik@aperto.com
 * @since (27.04.20)
 */
public interface RowsQueryBuilder extends QueryLimit<RowsQueryBuilder> {
    RowsQuery buildRowsQuery();
    List<Row> getResultRows();
}
