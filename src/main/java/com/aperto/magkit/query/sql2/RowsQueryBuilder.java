package com.aperto.magkit.query.sql2;

import com.aperto.magkit.query.sql2.jcrwrapper.RowsQuery;

import javax.jcr.query.Row;
import java.util.List;

/**
 * TODO: Comment.
 *
 * @author wolf.bubenik@aperto.com
 * @since (27.04.20)
 */
public interface RowsQueryBuilder extends QueryLimit<RowsQueryBuilder> {
    RowsQuery buildRowsQuery();
    List<Row> getResultRows();
}
