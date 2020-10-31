/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.smithy.jmespath.ast;

import java.util.Objects;
import software.amazon.smithy.jmespath.ExpressionVisitor;
import software.amazon.smithy.jmespath.JmespathExpression;

public final class FilterProjectionExpression extends JmespathExpression {

    private final JmespathExpression comparison;
    private final JmespathExpression left;
    private final JmespathExpression right;

    public FilterProjectionExpression(
            JmespathExpression left,
            JmespathExpression comparison,
            JmespathExpression right
    ) {
        this(left, comparison, right, 1, 1);
    }

    public FilterProjectionExpression(
            JmespathExpression left,
            JmespathExpression comparison,
            JmespathExpression right,
            int line,
            int column
    ) {
        super(line, column);
        this.left = left;
        this.right = right;
        this.comparison = comparison;
    }

    public JmespathExpression getLeft() {
        return left;
    }

    public JmespathExpression getRight() {
        return right;
    }

    public JmespathExpression getComparison() {
        return comparison;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visitFilterProjection(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof FilterProjectionExpression)) {
            return false;
        }
        FilterProjectionExpression that = (FilterProjectionExpression) o;
        return getComparison().equals(that.getComparison())
               && getLeft().equals(that.getLeft())
               && getRight().equals(that.getRight());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getComparison(), getLeft(), getRight());
    }

    @Override
    public String toString() {
        return "FilterProjectionExpression{"
               + "comparison=" + comparison
               + ", left=" + left
               + ", right=" + right + '}';
    }
}
