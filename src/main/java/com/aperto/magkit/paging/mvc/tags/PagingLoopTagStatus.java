package com.aperto.magkit.paging.mvc.tags;

import javax.servlet.jsp.jstl.core.LoopTagStatus;
import java.io.Serializable;

/**
 * @author wolf.bubenik
 * Date: 14.01.2009
 */
public interface PagingLoopTagStatus extends LoopTagStatus, Serializable {
    /**
     * Accessor for the loop index of the actual page (not the loop status).
     * @return an int
     */
    int getCurrentPageIndex();
}
