package de.ibmix.magkit.query.sql2;

/*-
 * #%L
 * IBM iX Magnolia Kit
 * %%
 * Copyright (C) 2023 IBM iX
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import de.ibmix.magkit.query.sql2.condition.Sql2CalendarCondition;
import de.ibmix.magkit.query.sql2.condition.Sql2CompareNot;
import de.ibmix.magkit.query.sql2.condition.Sql2ConstraintGroup;
import de.ibmix.magkit.query.sql2.condition.Sql2ContainsCondition;
import de.ibmix.magkit.query.sql2.condition.Sql2DoubleCondition;
import de.ibmix.magkit.query.sql2.condition.Sql2DynamicOperand;
import de.ibmix.magkit.query.sql2.condition.Sql2JoinConstraint;
import de.ibmix.magkit.query.sql2.condition.Sql2LongCondition;
import de.ibmix.magkit.query.sql2.condition.Sql2NameCondition;
import de.ibmix.magkit.query.sql2.condition.Sql2NameOperand;
import de.ibmix.magkit.query.sql2.condition.Sql2NullCondition;
import de.ibmix.magkit.query.sql2.condition.Sql2PathCondition;
import de.ibmix.magkit.query.sql2.condition.Sql2PathJoinCondition;
import de.ibmix.magkit.query.sql2.condition.Sql2StringCondition;
import de.ibmix.magkit.query.sql2.query.NodesQueryBuilder;
import de.ibmix.magkit.query.sql2.query.QueryNodesStatement;
import de.ibmix.magkit.query.sql2.query.QueryRowsStatement;
import de.ibmix.magkit.query.sql2.query.RowsQueryBuilder;
import de.ibmix.magkit.query.sql2.query.Sql2NodesQueryBuilder;
import de.ibmix.magkit.query.sql2.query.Sql2RowsQueryBuilder;
import de.ibmix.magkit.query.sql2.statement.Sql2As;
import de.ibmix.magkit.query.sql2.statement.Sql2From;
import de.ibmix.magkit.query.sql2.statement.Sql2Statement;
import info.magnolia.jcr.util.NodeTypes;
import org.apache.commons.lang3.ArrayUtils;

import javax.jcr.Node;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * A facade class for all sql2 query builders.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-08-10
 */
public final class Sql2 {

    private Sql2() {
    }

    /**
     * Sql2 query builders.
     *
     * @author wolf.bubenik@ibmix.de
     * @since 2020-08-10
     */
    public static final class Query {

        public static List<Node> nodesByIdentifiers(final String workspace, final String... ids) {
            return ArrayUtils.isNotEmpty(ids) ? nodesFrom(workspace).withStatement(
                Statement.select().whereAny(Condition.String.identifierEquals(ids))
            ).getResultNodes() : Collections.emptyList();
        }

        public static List<Node> nodesByTemplates(final String path, final String... templates) {
            return ArrayUtils.isNotEmpty(templates) ? nodesFromWebsite().withStatement(
                Statement.select().whereAll(
                    Condition.Path.isDescendant(path),
                    Condition.String.templateEquals(templates)
                )
            ).getResultNodes() : Collections.emptyList();
        }

        public static List<Node> nodesFrom(final String workspace, final String nodeType, Sql2JoinConstraint... conditions) {
            return nodesFrom(workspace).withStatement(
                Sql2.Statement.select().from(nodeType).whereAll(
                    conditions
                )
            ).getResultNodes();
        }

        public static QueryNodesStatement<NodesQueryBuilder> nodesFrom(final String workspace) {
            return Sql2NodesQueryBuilder.forNodes().fromWorkspace(workspace);
        }

        public static QueryNodesStatement<NodesQueryBuilder> nodesFromWebsite() {
            return Sql2NodesQueryBuilder.forNodes().fromWebsite();
        }

        public static QueryRowsStatement<RowsQueryBuilder> rowsFrom(final String workspace) {
            return Sql2RowsQueryBuilder.forRows().fromWorkspace(workspace);
        }

        public static QueryRowsStatement<RowsQueryBuilder> rowsFromWebsite() {
            return Sql2RowsQueryBuilder.forRows().fromWebsite();
        }

        private Query() {
        }
    }

    /**
     * Sql2 statement builders.
     *
     * @author wolf.bubenik@ibmix.de
     * @since 2020-08-10
     */
    public static final class Statement {

        public static Sql2From select(String... attributes) {
            return Sql2Statement.select(attributes);
        }

        public static Sql2As selectContentNodes(String... attributes) {
            return Sql2Statement.select(attributes).from(NodeTypes.ContentNode.NAME);
        }

        public static Sql2As selectContents(String... attributes) {
            return Sql2Statement.select(attributes).from(NodeTypes.Content.NAME);
        }

        public static Sql2As selectPages(String... attributes) {
            return Sql2Statement.select(attributes).from(NodeTypes.Page.NAME);
        }

        public static Sql2As selectAreas(String... attributes) {
            return Sql2Statement.select(attributes).from(NodeTypes.Area.NAME);
        }

        public static Sql2As selectComponents(String... attributes) {
            return Sql2Statement.select(attributes).from(NodeTypes.Component.NAME);
        }

        private Statement() {
        }
    }

    /**
     * Sql2 condition builders.
     *
     * @author wolf.bubenik@ibmix.de
     * @since 2020-08-10
     */
    public static final class Condition {

        private Condition() {
        }

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

        public static Sql2NameOperand name() {
            return new Sql2NameCondition();
        }

        public static Sql2JoinConstraint nameEquals(java.lang.String... values) {
            return name().equalsAny().values(values);
        }

        /**
         * Sql2 date condition builders.
         *
         * @author wolf.bubenik@ibmix.de
         * @since 2020-08-10
         */
        public static final class Date {

            public static Sql2CompareNot<Calendar> property(final java.lang.String name) {
                return Sql2CalendarCondition.property(name);
            }

            public static Sql2CompareNot<Calendar> created() {
                return Sql2CalendarCondition.created();
            }

            public static Sql2JoinConstraint createdBefore(Calendar date) {
                return created().lowerThan().value(date);
            }

            public static Sql2JoinConstraint createdAfter(Calendar date) {
                return created().greaterThan().value(date);
            }

            public static Sql2CompareNot<Calendar> lastActivated() {
                return Sql2CalendarCondition.lastActivated();
            }

            public static Sql2JoinConstraint lastActivatedBefore(Calendar date) {
                return lastActivated().lowerThan().value(date);
            }

            public static Sql2JoinConstraint lastActivatedAfter(Calendar date) {
                return lastActivated().greaterThan().value(date);
            }

            public static Sql2CompareNot<Calendar> lastModified() {
                return Sql2CalendarCondition.lastModified();
            }

            public static Sql2JoinConstraint lastModifiedBefore(Calendar date) {
                return lastModified().lowerThan().value(date);
            }

            public static Sql2JoinConstraint lastModifiedAfter(Calendar date) {
                return lastModified().greaterThan().value(date);
            }

            public static Sql2CompareNot<Calendar> deleted() {
                return Sql2CalendarCondition.deleted();
            }

            public static Sql2JoinConstraint deletedBefore(final Calendar date) {
                return deleted().lowerThan().value(date);
            }

            public static Sql2JoinConstraint deletedAfter(final Calendar date) {
                return deleted().greaterThan().value(date);
            }

            private Date() {
            }
        }

        /**
         * Sql2 date condition builders.
         *
         * @author wolf.bubenik@ibmix.de
         * @since 2020-08-10
         */
        public static final class Double {

            public static Sql2CompareNot<java.lang.Double> property(final java.lang.String name) {
                return Sql2DoubleCondition.property(name);
            }

            public static Sql2JoinConstraint propertyLowerThan(final java.lang.String name, double value) {
                return property(name).lowerThan().value(value);
            }

            public static Sql2JoinConstraint propertyEqualsAny(final java.lang.String name, java.lang.Double... values) {
                return property(name).equalsAny().values(values);
            }

            public static Sql2JoinConstraint propertyGraterOrEqualThan(final java.lang.String name, double value) {
                return property(name).greaterOrEqualThan().value(value);
            }

            public static Sql2JoinConstraint propertyGraterThan(final java.lang.String name, double value) {
                return property(name).greaterThan().value(value);
            }

            public static Sql2JoinConstraint propertyBetween(final java.lang.String name, double from, double to) {
                return and().matches(propertyGraterOrEqualThan(name, from), propertyLowerThan(name, to));
            }

            private Double() {
            }
        }

        /**
         * Sql2 date condition builders.
         *
         * @author wolf.bubenik@ibmix.de
         * @since 2020-08-10
         */
        public static final class Long {

            public static Sql2CompareNot<java.lang.Long> property(final java.lang.String name) {
                return Sql2LongCondition.property(name);
            }

            public static Sql2JoinConstraint propertyLowerThan(final java.lang.String name, long value) {
                return property(name).lowerThan().value(value);
            }

            public static Sql2JoinConstraint propertyEqualsAny(final java.lang.String name, java.lang.Long... values) {
                return property(name).equalsAny().values(values);
            }

            public static Sql2JoinConstraint propertyGraterOrEqualThan(final java.lang.String name, long value) {
                return property(name).greaterOrEqualThan().value(value);
            }

            public static Sql2JoinConstraint propertyGraterThan(final java.lang.String name, long value) {
                return property(name).greaterThan().value(value);
            }

            public static Sql2JoinConstraint propertyBetween(final java.lang.String name, long from, long to) {
                return and().matches(propertyGraterOrEqualThan(name, from), propertyLowerThan(name, to));
            }

            private Long() {
            }
        }

        /**
         * Sql2 path condition builders.
         *
         * @author wolf.bubenik@ibmix.de
         * @since 2020-08-10
         */
        public static final class Path {

            public static Sql2PathCondition is() {
                return Sql2PathCondition.is();
            }

            public static Sql2JoinConstraint isChild(final java.lang.String path) {
                return is().child(path);
            }

            public static Sql2JoinConstraint isChild(final Node parent) {
                return is().child(parent);
            }

            public static Sql2JoinConstraint isDescendant(final java.lang.String path) {
                return is().descendant(path);
            }

            public static Sql2JoinConstraint isDescendant(final Node ancestor) {
                return is().descendant(ancestor);
            }

            private Path() {
            }
        }

        /**
         * Sql2 string condition builders.
         *
         * @author wolf.bubenik@ibmix.de
         * @since 2020-08-10
         */
        public static final class String {

            public static Sql2DynamicOperand property(final java.lang.String name) {
                return Sql2StringCondition.property(name);
            }

            public static Sql2DynamicOperand template() {
                return Sql2StringCondition.template();
            }

            public static Sql2JoinConstraint templateEquals(java.lang.String... values) {
                return template().equalsAny().values(values);
            }

            public static Sql2DynamicOperand identifier() {
                return Sql2StringCondition.identifier();
            }

            public static Sql2JoinConstraint identifierEquals(java.lang.String... values) {
                return identifier().equalsAny().values(values);
            }

            private String() {
            }
        }

        /**
         * Sql2fulltext condition builders (contains(..)).
         *
         * @author wolf.bubenik@ibmix.de
         * @since 2022-12-23
         */
        public static final class FullText {

            /**
             * Get a builder for a fulltext condition for a search in all attributes.
             * Note that you must define a selector name in your statement.
             *
             * @return a Sql2ContainsCondition instance for any property
             */
            public static Sql2ContainsCondition contains() {
                return new Sql2ContainsCondition();
            }

            public static Sql2ContainsCondition containsAll(java.lang.String... values) {
                return contains().all(values);
            }

            public static Sql2ContainsCondition containsAny(java.lang.String... values) {
                return contains().any(values);
            }

            /**
             * Get a builder for a fulltext condition for a search in one named attribute.
             * Note that you must define a selector name in your statement.
             *
             * @return a Sql2ContainsCondition instance for one given property
             */
            public static Sql2ContainsCondition contains(java.lang.String property) {
                return new Sql2ContainsCondition(property);
            }

            public static Sql2ContainsCondition propertyContainsAll(java.lang.String property, java.lang.String... values) {
                return contains(property).all(values);
            }

            public static Sql2ContainsCondition propertyContainsAny(java.lang.String property, java.lang.String... values) {
                return contains(property).any(values);
            }

            private FullText() {
            }
        }
    }

    /**
     * Sql2 join condition builders.
     *
     * @author wolf.bubenik@ibmix.de
     * @since 2020-08-10
     */
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

        private JoinOn() {
        }
    }
}
