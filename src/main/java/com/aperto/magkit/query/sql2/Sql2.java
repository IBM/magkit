package com.aperto.magkit.query.sql2;

import com.aperto.magkit.query.sql2.condition.Sql2CalendarCondition;
import com.aperto.magkit.query.sql2.condition.Sql2CompareNot;
import com.aperto.magkit.query.sql2.condition.Sql2ConstraintGroup;
import com.aperto.magkit.query.sql2.condition.Sql2DoubleCondition;
import com.aperto.magkit.query.sql2.condition.Sql2DynamicOperand;
import com.aperto.magkit.query.sql2.condition.Sql2JoinConstraint;
import com.aperto.magkit.query.sql2.condition.Sql2LongCondition;
import com.aperto.magkit.query.sql2.condition.Sql2NullCondition;
import com.aperto.magkit.query.sql2.condition.Sql2PathCondition;
import com.aperto.magkit.query.sql2.condition.Sql2PathJoinCondition;
import com.aperto.magkit.query.sql2.condition.Sql2StringCondition;
import com.aperto.magkit.query.sql2.statement.Sql2From;
import com.aperto.magkit.query.sql2.statement.Sql2Statement;

import javax.jcr.Node;
import java.util.Calendar;
import java.util.List;

/**
 * A facade class for all sql2 query builders.
 *
 * @author wolf.bubenik@aperto.com
 * @since (10.08.2020)
 */
public final class Sql2 {

    /**
     * Sql2 query builders.
     *
     * @author wolf.bubenik@aperto.com
     * @since (10.08.2020)
     */
    public static final class Query {

        public static List<Node> nodesByIdentifiers(String workspace, String... ids) {
            return nodesFrom(workspace).withStatement(Statement.selectAll().whereAny(Condition.identifier(ids))).getResultNodes();
        }

        public static List<Node> nodesByTemplates(String path, String... templates) {
            return nodesFromWebsite().withStatement(Statement.selectAll().whereAll(Condition.Path.isDescendant(path), Condition.template(templates))).getResultNodes();
        }

        public static QueryNodesStatement<NodesQueryBuilder> nodesFrom(String workspace) {
            return Sql2NodesQueryBuilder.forNodes().fromWorkspace(workspace);
        }

        public static QueryNodesStatement<NodesQueryBuilder> nodesFromWebsite() {
            return Sql2NodesQueryBuilder.forNodes().fromWebsite();
        }

        public static QueryRowsStatement<RowsQueryBuilder> rowsFrom(String workspace) {
            return Sql2RowsQueryBuilder.forRows().fromWorkspace(workspace);
        }

        public static QueryRowsStatement<RowsQueryBuilder> rowsFromWebsite() {
            return Sql2RowsQueryBuilder.forRows().fromWebsite();
        }

        private Query() {}
    }


    /**
     * Sql2 statement builders.
     *
     * @author wolf.bubenik@aperto.com
     * @since (10.08.2020)
     */
    public static final class Statement {

        public static Sql2From selectAll() {
            return Sql2Statement.selectAll();
        }

        public static Sql2From selectAttributes(String... attributes) {
            return Sql2Statement.selectAttributes(attributes);
        }

        private Statement() {}
    }

    /**
     * Sql2 condition builders.
     *
     * @author wolf.bubenik@aperto.com
     * @since (10.08.2020)
     */
    public static final class Condition {

        public static Sql2ConstraintGroup and() {
            return Sql2ConstraintGroup.and();
        }

        public static Sql2ConstraintGroup or() {
            return Sql2ConstraintGroup.or();
        }

        public static Sql2JoinConstraint isNull(final java.lang.String propertyName) {
            return Sql2NullCondition.isNull(propertyName);
        }

        public static Sql2JoinConstraint isNotNull(final java.lang.String propertyName) {
            return Sql2NullCondition.isNotNull(propertyName);
        }

        public static Sql2CompareNot<Calendar> created() {
            return Sql2CalendarCondition.created();
        }

        public static Sql2JoinConstraint createdBefore(java.util.Calendar date) {
            return Sql2CalendarCondition.created().lowerThan().value(date);
        }

        public static Sql2JoinConstraint createdAfter(java.util.Calendar date) {
            return Sql2CalendarCondition.created().greaterThan().value(date);
        }

        public static Sql2CompareNot<Calendar> lastActivated() {
            return Sql2CalendarCondition.lastActivated();
        }

        public static Sql2JoinConstraint lastActivatedBefore(java.util.Calendar date) {
            return Sql2CalendarCondition.lastActivated().lowerThan().value(date);
        }

        public static Sql2JoinConstraint lastActivatedAfter(java.util.Calendar date) {
            return Sql2CalendarCondition.lastActivated().greaterThan().value(date);
        }

        public static Sql2CompareNot<Calendar> lastModified() {
            return Sql2CalendarCondition.lastModified();
        }

        public static Sql2JoinConstraint lastModifiedBefore(java.util.Calendar date) {
            return Sql2CalendarCondition.lastModified().lowerThan().value(date);
        }

        public static Sql2JoinConstraint lastModifiedAfter(java.util.Calendar date) {
            return Sql2CalendarCondition.lastModified().greaterThan().value(date);
        }

        public static Sql2CompareNot<Calendar> deleted() {
            return Sql2CalendarCondition.deleted();
        }

        public static Sql2JoinConstraint template(java.lang.String... values) {
            return Sql2StringCondition.template().equalsAny().values(values);
        }

        public static Sql2JoinConstraint identifier(java.lang.String... values) {
            return Sql2StringCondition.identifier().equalsAny().values(values);
        }

        public static Sql2JoinConstraint childOf(Node parent) {
            return Path.isChild(parent);
        }

        public static Sql2JoinConstraint childOf(java.lang.String parent) {
            return Path.isChild(parent);
        }

        public static Sql2JoinConstraint descendantOf(Node parent) {
            return Path.isDescendant(parent);
        }

        public static Sql2JoinConstraint descendantOf(java.lang.String parent) {
            return Path.isDescendant(parent);
        }

        /**
         * Sql2 date condition builders.
         *
         * @author wolf.bubenik@aperto.com
         * @since (10.08.2020)
         */
        public static final class Date {

            public static Sql2CompareNot<Calendar> property(final java.lang.String name) {
                return Sql2CalendarCondition.property(name);
            }

            private Date() {}
        }

        /**
         * Sql2 date condition builders.
         *
         * @author wolf.bubenik@aperto.com
         * @since (10.08.2020)
         */
        public static final class Double {

            public static Sql2CompareNot<java.lang.Double> property(final java.lang.String name) {
                return Sql2DoubleCondition.property(name);
            }

            private Double() {}
        }

        /**
         * Sql2 date condition builders.
         *
         * @author wolf.bubenik@aperto.com
         * @since (10.08.2020)
         */
        public static final class Long {

            public static Sql2CompareNot<java.lang.Long> property(final java.lang.String name) {
                return Sql2LongCondition.property(name);
            }

            private Long() {}
        }

        /**
         * Sql2 path condition builders.
         *
         * @author wolf.bubenik@aperto.com
         * @since (10.08.2020)
         */
        public static final class Path {

            public static Sql2PathCondition is() {
                return Sql2PathCondition.is();
            }

            public static Sql2JoinConstraint isChild(final java.lang.String path) {
                return Sql2PathCondition.is().child(path);
            }

            public static Sql2JoinConstraint isChild(final Node parent) {
                return Sql2PathCondition.is().child(parent);
            }

            public static Sql2JoinConstraint isDescendant(final java.lang.String path) {
                return Sql2PathCondition.is().descendant(path);
            }

            public static Sql2JoinConstraint isDescendant(final Node ancestor) {
                return Sql2PathCondition.is().descendant(ancestor);
            }

            private Path() {}
        }

        /**
         * Sql2 string condition builders.
         *
         * @author wolf.bubenik@aperto.com
         * @since (10.08.2020)
         */
        public static final class String {

            public static Sql2DynamicOperand property(final java.lang.String name) {
                return Sql2StringCondition.property(name);
            }

            private String() {}
        }

        private Condition() {}
    }

    public static final class JoinOn {
        public static Sql2PathJoinCondition joinedDescendantOfSelected() {
            return Sql2PathJoinCondition.isJoinedDescendantOfSelected();
        }

        public static Sql2PathJoinCondition joinedChildOfSelected() {
            return Sql2PathJoinCondition.isJoinedChildOfSelected();
        }

        public static Sql2PathJoinCondition joinedEqualsSelected() {
            return Sql2PathJoinCondition.isJoinedEqualsSelected();
        }

        public static Sql2PathJoinCondition selectedChildOfJoined() {
            return Sql2PathJoinCondition.isSelectedChildOfJoined();
        }

        public static Sql2PathJoinCondition selectedDescendantOfJoined() {
            return Sql2PathJoinCondition.isSelectedDescendantOfJoined();
        }
    }

    private Sql2() {}
}
