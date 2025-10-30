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
 * Facade for building and executing JCR SQL2 queries in a fluent, type-safe style.
 * <p>
 * This class groups three categories of static builder helpers:
 * </p>
 * <ul>
 *   <li><b>Query</b>: High level convenience methods returning node or row query builders and executing common queries.</li>
 *   <li><b>Statement</b>: Entry points for constructing SELECT statements with predefined Magnolia node types.</li>
 *   <li><b>Condition</b>: Rich collection of operand and constraint builders for composing WHERE and JOIN conditions (date, numeric, path, string, full-text).</li>
 * </ul>
 * <p>
 * Key features:
 * </p>
 * <ul>
 *   <li>Fluent DSL minimizing boilerplate for typical Magnolia queries.</li>
 *   <li>Separation of statement, query and condition concerns for readability.</li>
 *   <li>Support for common Magnolia node type shortcuts (pages, components, areas, etc.).</li>
 *   <li>Convenience methods for identifier, template, path and date comparisons.</li>
 * </ul>
 * <p>
 * Usage preconditions: Call one of the {@code Statement.select*} methods first to obtain a statement builder if you do not use the
 * higher level {@code Query.nodesFrom(..)} shortcuts. Selector names may be required for full-text conditions.
 * </p>
 * <p>
 * Side effects: This facade performs no repository modifications; it only builds and executes read queries.
 * </p>
 * <p>
 * Null handling: Varargs accepting methods return an empty list if the provided array is null or empty. Condition builder methods
 * expect non-null parameter values; passing null will typically cause a runtime exception in underlying builders.
 * </p>
 * <p>
 * Thread-safety: All methods are stateless and thread-safe. Builders returned are not guaranteed to be thread-safe and should be
 * confined to the thread that constructs them.
 * </p>
 * <p>
 * Example:
 * </p>
 * <pre>
 * List&lt;Node&gt; pages = Sql2.Query.nodesFromWebsite()
 *   .withStatement(Sql2.Statement.selectPages().whereAll(
 *       Sql2.Condition.Path.isDescendant("/foo"),
 *       Sql2.Condition.String.templateEquals("mgnl:page")
 *   ))
 *   .getResultNodes();
 * </pre>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-08-10
 */
public final class Sql2 {

    /**
     * Private constructor to prevent instantiation of utility facade.
     */
    private Sql2() {
    }

    /**
     * High level query helpers returning preconfigured node or row query builders and executing common queries.
     * Provides convenience shortcuts for frequent selection patterns (by identifiers, templates, workspace).
     *
     * @author wolf.bubenik@ibmix.de
     * @since 2020-08-10
     */
    public static final class Query {

        /**
         * Query nodes by JCR identifiers in the given workspace.
         * Returns an empty list if no identifiers are provided.
         *
         * @param workspace the JCR workspace name
         * @param ids one or more JCR identifiers
         * @return list of matching nodes or empty list if ids is null/empty
         */
        public static List<Node> nodesByIdentifiers(final String workspace, final String... ids) {
            return ArrayUtils.isNotEmpty(ids) ? nodesFrom(workspace).withStatement(
                Statement.select().whereAny(Condition.String.identifierEquals(ids))
            ).getResultNodes() : Collections.emptyList();
        }

        /**
         * Query website nodes below a path filtered by template names.
         * Returns an empty list if no templates are provided.
         *
         * @param path the ancestor path to search below
         * @param templates one or more template names
         * @return list of matching nodes or empty list if templates is null/empty
         */
        public static List<Node> nodesByTemplates(final String path, final String... templates) {
            return ArrayUtils.isNotEmpty(templates) ? nodesFromWebsite().withStatement(
                Statement.select().whereAll(
                    Condition.Path.isDescendant(path),
                    Condition.String.templateEquals(templates)
                )
            ).getResultNodes() : Collections.emptyList();
        }

        /**
         * Query nodes from a workspace filtered by node type and additional join constraints.
         *
         * @param workspace the JCR workspace name
         * @param nodeType the primary node type name
         * @param conditions optional additional join constraints
         * @return list of matching nodes
         */
        public static List<Node> nodesFrom(final String workspace, final String nodeType, Sql2JoinConstraint... conditions) {
            return nodesFrom(workspace).withStatement(
                Sql2.Statement.select().from(nodeType).whereAll(
                    conditions
                )
            ).getResultNodes();
        }

        /**
         * Obtain a nodes query builder for the given workspace.
         *
         * @param workspace the JCR workspace name
         * @return nodes query statement builder
         */
        public static QueryNodesStatement<NodesQueryBuilder> nodesFrom(final String workspace) {
            return Sql2NodesQueryBuilder.forNodes().fromWorkspace(workspace);
        }

        /**
         * Obtain a nodes query builder targeting the default website workspace.
         *
         * @return nodes query statement builder for website
         */
        public static QueryNodesStatement<NodesQueryBuilder> nodesFromWebsite() {
            return Sql2NodesQueryBuilder.forNodes().fromWebsite();
        }

        /**
         * Obtain a rows query builder for the given workspace.
         *
         * @param workspace the JCR workspace name
         * @return rows query statement builder
         */
        public static QueryRowsStatement<RowsQueryBuilder> rowsFrom(final String workspace) {
            return Sql2RowsQueryBuilder.forRows().fromWorkspace(workspace);
        }

        /**
         * Obtain a rows query builder targeting the default website workspace.
         *
         * @return rows query statement builder for website
         */
        public static QueryRowsStatement<RowsQueryBuilder> rowsFromWebsite() {
            return Sql2RowsQueryBuilder.forRows().fromWebsite();
        }

        /**
         * Private constructor to prevent instantiation.
         */
        private Query() {
        }
    }

    /**
     * Statement builder entry points for constructing SELECT clauses with optional predefined Magnolia node types.
     * Offers shortcuts for common content related node type selections (content nodes, contents, pages, areas, components).
     *
     * @author wolf.bubenik@ibmix.de
     * @since 2020-08-10
     */
    public static final class Statement {

        /**
         * Start a SELECT statement optionally specifying attributes (columns).
         *
         * @param attributes optional attribute names to select; if empty defaults apply
         * @return FROM builder to continue statement construction
         */
        public static Sql2From select(String... attributes) {
            return Sql2Statement.select(attributes);
        }

        /**
         * Start a SELECT statement constrained to {@code mgnl:contentNode}.
         *
         * @param attributes optional attribute names to select
         * @return AS builder for further refinement
         */
        public static Sql2As selectContentNodes(String... attributes) {
            return Sql2Statement.select(attributes).from(NodeTypes.ContentNode.NAME);
        }

        /**
         * Start a SELECT statement constrained to {@code mgnl:content}.
         *
         * @param attributes optional attribute names to select
         * @return AS builder for further refinement
         */
        public static Sql2As selectContents(String... attributes) {
            return Sql2Statement.select(attributes).from(NodeTypes.Content.NAME);
        }

        /**
         * Start a SELECT statement constrained to {@code mgnl:page} nodes.
         *
         * @param attributes optional attribute names to select
         * @return AS builder for further refinement
         */
        public static Sql2As selectPages(String... attributes) {
            return Sql2Statement.select(attributes).from(NodeTypes.Page.NAME);
        }

        /**
         * Start a SELECT statement constrained to {@code mgnl:area} nodes.
         *
         * @param attributes optional attribute names to select
         * @return AS builder for further refinement
         */
        public static Sql2As selectAreas(String... attributes) {
            return Sql2Statement.select(attributes).from(NodeTypes.Area.NAME);
        }

        /**
         * Start a SELECT statement constrained to {@code mgnl:component} nodes.
         *
         * @param attributes optional attribute names to select
         * @return AS builder for further refinement
         */
        public static Sql2As selectComponents(String... attributes) {
            return Sql2Statement.select(attributes).from(NodeTypes.Component.NAME);
        }

        /**
         * Private constructor to prevent instantiation.
         */
        private Statement() {
        }
    }

    /**
     * Condition builder factory grouping logical, null, name, date, numeric, path, string and full-text helper classes.
     * Enables fluent composition of complex WHERE and JOIN clauses.
     *
     * @author wolf.bubenik@ibmix.de
     * @since 2020-08-10
     */
    public static final class Condition {

        /**
         * Private constructor to prevent instantiation.
         */
        private Condition() {
        }

        /**
         * Create an AND constraint group for combining multiple constraints.
         *
         * @return AND constraint group builder
         */
        public static Sql2ConstraintGroup and() {
            return Sql2ConstraintGroup.and();
        }

        /**
         * Create an OR constraint group for combining multiple constraints.
         *
         * @return OR constraint group builder
         */
        public static Sql2ConstraintGroup or() {
            return Sql2ConstraintGroup.or();
        }

        /**
         * Build an IS NULL constraint for a property.
         *
         * @param propertyName property name
         * @return join constraint representing IS NULL
         */
        public static Sql2JoinConstraint isNull(final java.lang.String propertyName) {
            return Sql2NullCondition.isNull(propertyName);
        }

        /**
         * Build an IS NOT NULL constraint for a property.
         *
         * @param propertyName property name
         * @return join constraint representing IS NOT NULL
         */
        public static Sql2JoinConstraint isNotNull(final java.lang.String propertyName) {
            return Sql2NullCondition.isNotNull(propertyName);
        }

        /**
         * Obtain a name operand for building name-based comparisons.
         *
         * @return name operand builder
         */
        public static Sql2NameOperand name() {
            return new Sql2NameCondition();
        }

        /**
         * Create a constraint matching any of the provided node names.
         *
         * @param values one or more node names
         * @return join constraint representing equality on names
         */
        public static Sql2JoinConstraint nameEquals(java.lang.String... values) {
            return name().equalsAny().values(values);
        }

        /**
         * Date related comparison builders for created, modified, activated and deleted timestamps.
         *
         * @author wolf.bubenik@ibmix.de
         * @since 2020-08-10
         */
        public static final class Date {

            /**
             * Create a date property operand for custom property comparisons.
             *
             * @param name property name
             * @return date compare builder
             */
            public static Sql2CompareNot<Calendar> property(final java.lang.String name) {
                return Sql2CalendarCondition.property(name);
            }

            /**
             * Operand for created date.
             *
             * @return created compare builder
             */
            public static Sql2CompareNot<Calendar> created() {
                return Sql2CalendarCondition.created();
            }

            /**
             * Constraint for created before specified date.
             *
             * @param date upper exclusive bound
             * @return join constraint
             */
            public static Sql2JoinConstraint createdBefore(Calendar date) {
                return created().lowerThan().value(date);
            }

            /**
             * Constraint for created after specified date.
             *
             * @param date lower exclusive bound
             * @return join constraint
             */
            public static Sql2JoinConstraint createdAfter(Calendar date) {
                return created().greaterThan().value(date);
            }

            /**
             * Operand for last activation date.
             *
             * @return activation compare builder
             */
            public static Sql2CompareNot<Calendar> lastActivated() {
                return Sql2CalendarCondition.lastActivated();
            }

            /**
             * Constraint for last activation before specified date.
             *
             * @param date upper exclusive bound
             * @return join constraint
             */
            public static Sql2JoinConstraint lastActivatedBefore(Calendar date) {
                return lastActivated().lowerThan().value(date);
            }

            /**
             * Constraint for last activation after specified date.
             *
             * @param date lower exclusive bound
             * @return join constraint
             */
            public static Sql2JoinConstraint lastActivatedAfter(Calendar date) {
                return lastActivated().greaterThan().value(date);
            }

            /**
             * Operand for last modified date.
             *
             * @return last modified compare builder
             */
            public static Sql2CompareNot<Calendar> lastModified() {
                return Sql2CalendarCondition.lastModified();
            }

            /**
             * Constraint for last modified before specified date.
             *
             * @param date upper exclusive bound
             * @return join constraint
             */
            public static Sql2JoinConstraint lastModifiedBefore(Calendar date) {
                return lastModified().lowerThan().value(date);
            }

            /**
             * Constraint for last modified after specified date.
             *
             * @param date lower exclusive bound
             * @return join constraint
             */
            public static Sql2JoinConstraint lastModifiedAfter(Calendar date) {
                return lastModified().greaterThan().value(date);
            }

            /**
             * Operand for deleted date (soft-deletion metadata).
             *
             * @return deleted compare builder
             */
            public static Sql2CompareNot<Calendar> deleted() {
                return Sql2CalendarCondition.deleted();
            }

            /**
             * Constraint for deleted before specified date.
             *
             * @param date upper exclusive bound
             * @return join constraint
             */
            public static Sql2JoinConstraint deletedBefore(final Calendar date) {
                return deleted().lowerThan().value(date);
            }

            /**
             * Constraint for deleted after specified date.
             *
             * @param date lower exclusive bound
             * @return join constraint
             */
            public static Sql2JoinConstraint deletedAfter(final Calendar date) {
                return deleted().greaterThan().value(date);
            }

            /**
             * Private constructor to prevent instantiation.
             */
            private Date() {
            }
        }

        /**
         * Double numeric comparison builders for arbitrary double properties.
         *
         * @author wolf.bubenik@ibmix.de
         * @since 2020-08-10
         */
        public static final class Double {

            /**
             * Create a double property operand.
             *
             * @param name property name
             * @return double compare builder
             */
            public static Sql2CompareNot<java.lang.Double> property(final java.lang.String name) {
                return Sql2DoubleCondition.property(name);
            }

            /**
             * Constraint for property lower than value.
             *
             * @param name property name
             * @param value upper exclusive bound
             * @return join constraint
             */
            public static Sql2JoinConstraint propertyLowerThan(final java.lang.String name, double value) {
                return property(name).lowerThan().value(value);
            }

            /**
             * Constraint for property equals any provided values.
             *
             * @param name property name
             * @param values candidate values
             * @return join constraint
             */
            public static Sql2JoinConstraint propertyEqualsAny(final java.lang.String name, java.lang.Double... values) {
                return property(name).equalsAny().values(values);
            }

            /**
             * Constraint for property greater or equal than value.
             *
             * @param name property name
             * @param value lower inclusive bound
             * @return join constraint
             */
            public static Sql2JoinConstraint propertyGraterOrEqualThan(final java.lang.String name, double value) {
                return property(name).greaterOrEqualThan().value(value);
            }

            /**
             * Constraint for property greater than value.
             *
             * @param name property name
             * @param value lower exclusive bound
             * @return join constraint
             */
            public static Sql2JoinConstraint propertyGraterThan(final java.lang.String name, double value) {
                return property(name).greaterThan().value(value);
            }

            /**
             * Constraint for property between two bounds (inclusive lower, exclusive upper semantics depend on underlying builders).
             *
             * @param name property name
             * @param from lower bound
             * @param to upper bound
             * @return join constraint combining lower/upper checks
             */
            public static Sql2JoinConstraint propertyBetween(final java.lang.String name, double from, double to) {
                return and().matches(propertyGraterOrEqualThan(name, from), propertyLowerThan(name, to));
            }

            /**
             * Private constructor to prevent instantiation.
             */
            private Double() {
            }
        }

        /**
         * Long numeric comparison builders for arbitrary long properties.
         *
         * @author wolf.bubenik@ibmix.de
         * @since 2020-08-10
         */
        public static final class Long {

            /**
             * Create a long property operand.
             *
             * @param name property name
             * @return long compare builder
             */
            public static Sql2CompareNot<java.lang.Long> property(final java.lang.String name) {
                return Sql2LongCondition.property(name);
            }

            /**
             * Constraint for property lower than value.
             *
             * @param name property name
             * @param value upper exclusive bound
             * @return join constraint
             */
            public static Sql2JoinConstraint propertyLowerThan(final java.lang.String name, long value) {
                return property(name).lowerThan().value(value);
            }

            /**
             * Constraint for property equals any provided values.
             *
             * @param name property name
             * @param values candidate values
             * @return join constraint
             */
            public static Sql2JoinConstraint propertyEqualsAny(final java.lang.String name, java.lang.Long... values) {
                return property(name).equalsAny().values(values);
            }

            /**
             * Constraint for property greater or equal than value.
             *
             * @param name property name
             * @param value lower inclusive bound
             * @return join constraint
             */
            public static Sql2JoinConstraint propertyGraterOrEqualThan(final java.lang.String name, long value) {
                return property(name).greaterOrEqualThan().value(value);
            }

            /**
             * Constraint for property greater than value.
             *
             * @param name property name
             * @param value lower exclusive bound
             * @return join constraint
             */
            public static Sql2JoinConstraint propertyGraterThan(final java.lang.String name, long value) {
                return property(name).greaterThan().value(value);
            }

            /**
             * Constraint for property between two bounds (inclusive lower, exclusive upper semantics depend on underlying builders).
             *
             * @param name property name
             * @param from lower bound
             * @param to upper bound
             * @return join constraint combining lower/upper checks
             */
            public static Sql2JoinConstraint propertyBetween(final java.lang.String name, long from, long to) {
                return and().matches(propertyGraterOrEqualThan(name, from), propertyLowerThan(name, to));
            }

            /**
             * Private constructor to prevent instantiation.
             */
            private Long() {
            }
        }

        /**
         * Path based condition builders for expressing ancestor/descendant or exact path matches.
         *
         * @author wolf.bubenik@ibmix.de
         * @since 2020-08-10
         */
        public static final class Path {

            /**
             * Begin a path condition chain.
             *
             * @return path condition builder
             */
            public static Sql2PathCondition is() {
                return Sql2PathCondition.is();
            }

            /**
             * Constraint for a path being a direct child of the given path.
             *
             * @param path parent path
             * @return join constraint
             */
            public static Sql2JoinConstraint isChild(final java.lang.String path) {
                return is().child(path);
            }

            /**
             * Constraint for a path being a direct child of the given parent node.
             *
             * @param parent parent node
             * @return join constraint
             */
            public static Sql2JoinConstraint isChild(final Node parent) {
                return is().child(parent);
            }

            /**
             * Constraint for a path being a descendant of the given path.
             *
             * @param path ancestor path
             * @return join constraint
             */
            public static Sql2JoinConstraint isDescendant(final java.lang.String path) {
                return is().descendant(path);
            }

            /**
             * Constraint for a path being a descendant of the given ancestor node.
             *
             * @param ancestor ancestor node
             * @return join constraint
             */
            public static Sql2JoinConstraint isDescendant(final Node ancestor) {
                return is().descendant(ancestor);
            }

            /**
             * Private constructor to prevent instantiation.
             */
            private Path() {
            }
        }

        /**
         * String property comparison builders including template and identifier helpers.
         *
         * @author wolf.bubenik@ibmix.de
         * @since 2020-08-10
         */
        public static final class String {

            /**
             * Create a string property operand.
             *
             * @param name property name
             * @return dynamic operand for string comparison
             */
            public static Sql2DynamicOperand property(final java.lang.String name) {
                return Sql2StringCondition.property(name);
            }

            /**
             * Constraint matching any of the given template values.
             *
             * @param values template names
             * @return join constraint
             */
            public static Sql2JoinConstraint propertyEquals(final java.lang.String name, java.lang.String... values) {
                return property(name).equalsAny().values(values);
            }

            /**
             * Operand for template property.
             *
             * @return dynamic operand for template comparisons
             */
            public static Sql2DynamicOperand template() {
                return Sql2StringCondition.template();
            }

            /**
             * Constraint matching any of the given template values.
             *
             * @param values template names
             * @return join constraint
             */
            public static Sql2JoinConstraint templateEquals(java.lang.String... values) {
                return template().equalsAny().values(values);
            }

            /**
             * Operand for identifier property.
             *
             * @return dynamic operand for identifier comparisons
             */
            public static Sql2DynamicOperand identifier() {
                return Sql2StringCondition.identifier();
            }

            /**
             * Constraint matching any of the given identifiers.
             *
             * @param values identifiers
             * @return join constraint
             */
            public static Sql2JoinConstraint identifierEquals(java.lang.String... values) {
                return identifier().equalsAny().values(values);
            }

            /**
             * Private constructor to prevent instantiation.
             */
            private String() {
            }
        }

        /**
         * Full-text condition builders (CONTAINS) supporting search across all properties or a single named property.
         * Selector names must be defined in the statement when using these conditions.
         *
         * @author wolf.bubenik@ibmix.de
         * @since 2022-12-23
         */
        public static final class FullText {

            /**
             * Builder for full-text search across all indexed properties.
             *
             * @return contains condition builder
             */
            public static Sql2ContainsCondition contains() {
                return new Sql2ContainsCondition();
            }

            /**
             * Full-text condition requiring that all provided terms match in any property.
             *
             * @param values search terms
             * @return contains condition builder
             */
            public static Sql2ContainsCondition containsAll(java.lang.String... values) {
                return contains().all(values);
            }

            /**
             * Full-text condition requiring that any provided term matches in any property.
             *
             * @param values search terms
             * @return contains condition builder
             */
            public static Sql2ContainsCondition containsAny(java.lang.String... values) {
                return contains().any(values);
            }

            /**
             * Builder for full-text search restricted to one named property.
             *
             * @param property property name
             * @return contains condition builder
             */
            public static Sql2ContainsCondition contains(java.lang.String property) {
                return new Sql2ContainsCondition(property);
            }

            /**
             * Full-text condition requiring that all provided terms match in given property.
             *
             * @param property property name
             * @param values search terms
             * @return contains condition builder
             */
            public static Sql2ContainsCondition propertyContainsAll(java.lang.String property, java.lang.String... values) {
                return contains(property).all(values);
            }

            /**
             * Full-text condition requiring that any provided term matches in given property.
             *
             * @param property property name
             * @param values search terms
             * @return contains condition builder
             */
            public static Sql2ContainsCondition propertyContainsAny(java.lang.String property, java.lang.String... values) {
                return contains(property).any(values);
            }

            /**
             * Private constructor to prevent instantiation.
             */
            private FullText() {
            }
        }
    }

    /**
     * Join condition builders focusing on path relationships between selected and joined selectors (ancestor, descendant, equality).
     * Supports constructing JOIN ON clauses with semantic clarity.
     *
     * @author wolf.bubenik@ibmix.de
     * @since 2020-08-10
     */
    public static final class JoinOn {
        /**
         * Constraint: joined node is a descendant of selected node.
         *
         * @return path join condition
         */
        public static Sql2PathJoinCondition joinedDescendantOfSelected() {
            return Sql2PathJoinCondition.isJoinedDescendantOfSelected();
        }

        /**
         * Constraint: joined node is a child of selected node.
         *
         * @return path join condition
         */
        public static Sql2PathJoinCondition joinedChildOfSelected() {
            return Sql2PathJoinCondition.isJoinedChildOfSelected();
        }

        /**
         * Constraint: joined node path equals selected node path.
         *
         * @return path join condition
         */
        public static Sql2PathJoinCondition joinedEqualsSelected() {
            return Sql2PathJoinCondition.isJoinedEqualsSelected();
        }

        /**
         * Constraint: selected node is a child of joined node.
         *
         * @return path join condition
         */
        public static Sql2PathJoinCondition selectedChildOfJoined() {
            return Sql2PathJoinCondition.isSelectedChildOfJoined();
        }

        /**
         * Constraint: selected node is a descendant of joined node.
         *
         * @return path join condition
         */
        public static Sql2PathJoinCondition selectedDescendantOfJoined() {
            return Sql2PathJoinCondition.isSelectedDescendantOfJoined();
        }

        /**
         * Private constructor to prevent instantiation.
         */
        private JoinOn() {
        }
    }
}
