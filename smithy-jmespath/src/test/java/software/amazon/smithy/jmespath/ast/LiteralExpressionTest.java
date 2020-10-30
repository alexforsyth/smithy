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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.jmespath.JmespathException;
import software.amazon.smithy.jmespath.RuntimeType;

public class LiteralExpressionTest {
    @Test
    public void containsNullValues() {
        LiteralExpression node = new LiteralExpression(null);

        assertThat(node.isNullValue(), is(true));
        assertThat(node.getType(), equalTo(RuntimeType.NULL));
    }

    @Test
    public void throwsWhenNotString() {
        LiteralExpression node = new LiteralExpression(10);

        Assertions.assertThrows(JmespathException.class, node::asStringValue);
    }

    @Test
    public void getsAsString() {
        LiteralExpression node = new LiteralExpression("foo");

        node.asStringValue();
        assertThat(node.isStringValue(), is(true));
        assertThat(node.isNullValue(), is(false)); // not null
        assertThat(node.getType(), equalTo(RuntimeType.STRING));
    }

    @Test
    public void throwsWhenNotArray() {
        LiteralExpression node = new LiteralExpression("hi");

        Assertions.assertThrows(JmespathException.class, node::asArrayValue);
    }

    @Test
    public void getsAsArray() {
        LiteralExpression node = new LiteralExpression(Collections.emptyList());

        node.asArrayValue();
        assertThat(node.isArrayValue(), is(true));
        assertThat(node.getType(), equalTo(RuntimeType.ARRAY));
    }

    @Test
    public void getsNegativeArrayIndex() {
        LiteralExpression node = new LiteralExpression(Arrays.asList(1, 2, 3));

        assertThat(node.getArrayIndex(-1).getValue(), equalTo(3));
        assertThat(node.getArrayIndex(-2).getValue(), equalTo(2));
        assertThat(node.getArrayIndex(-3).getValue(), equalTo(1));
        assertThat(node.getArrayIndex(-4).getValue(), equalTo(null));
    }

    @Test
    public void throwsWhenNotNumber() {
        LiteralExpression node = new LiteralExpression("hi");

        Assertions.assertThrows(JmespathException.class, node::asNumberValue);
    }

    @Test
    public void getsAsNumber() {
        LiteralExpression node = new LiteralExpression(10);

        node.asNumberValue();
        assertThat(node.isNumberValue(), is(true));
        assertThat(node.getType(), equalTo(RuntimeType.NUMBER));
    }

    @Test
    public void throwsWhenNotBoolean() {
        LiteralExpression node = new LiteralExpression("hi");

        Assertions.assertThrows(JmespathException.class, node::asBooleanValue);
    }

    @Test
    public void getsAsBoolean() {
        LiteralExpression node = new LiteralExpression(true);

        node.asBooleanValue();
        assertThat(node.isBooleanValue(), is(true));
        assertThat(node.getType(), equalTo(RuntimeType.BOOLEAN));
    }

    @Test
    public void getsAsBoxedBoolean() {
        LiteralExpression node = new LiteralExpression(new Boolean(true));

        node.asBooleanValue();
        assertThat(node.isBooleanValue(), is(true));
    }

    @Test
    public void throwsWhenNotMap() {
        LiteralExpression node = new LiteralExpression("hi");

        Assertions.assertThrows(JmespathException.class, node::asObjectValue);
    }

    @Test
    public void getsAsMap() {
        LiteralExpression node = new LiteralExpression(Collections.emptyMap());

        node.asObjectValue();
        assertThat(node.isObjectValue(), is(true));
        assertThat(node.getType(), equalTo(RuntimeType.OBJECT));
    }

    @Test
    public void expressionReferenceTypeIsExpref() {
        assertThat(LiteralExpression.EXPREF.getType(), equalTo(RuntimeType.EXPRESSION_REFERENCE));
    }

    @Test
    public void anyValueIsAnyType() {
        assertThat(LiteralExpression.ANY.getType(), equalTo(RuntimeType.ANY));
    }

    @Test
    public void determinesTruthyValues() {
        assertThat(new LiteralExpression(0).isTruthy(), is(true));
        assertThat(new LiteralExpression(1).isTruthy(), is(true));
        assertThat(new LiteralExpression(true).isTruthy(), is(true));
        assertThat(new LiteralExpression("hi").isTruthy(), is(true));
        assertThat(new LiteralExpression(Arrays.asList(1, 2)).isTruthy(), is(true));
        assertThat(new LiteralExpression(Collections.singletonMap("a", "b")).isTruthy(), is(true));

        assertThat(new LiteralExpression(false).isTruthy(), is(false));
        assertThat(new LiteralExpression("").isTruthy(), is(false));
        assertThat(new LiteralExpression(Collections.emptyList()).isTruthy(), is(false));
        assertThat(new LiteralExpression(Collections.emptyMap()).isTruthy(), is(false));
    }
}
