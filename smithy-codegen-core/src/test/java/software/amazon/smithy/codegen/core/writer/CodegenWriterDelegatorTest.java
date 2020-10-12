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

package software.amazon.smithy.codegen.core.writer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.build.MockManifest;
import software.amazon.smithy.codegen.core.Symbol;
import software.amazon.smithy.codegen.core.SymbolDependency;
import software.amazon.smithy.codegen.core.SymbolProvider;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.StringShape;

public class CodegenWriterDelegatorTest {

    private static final class OnUse implements UseShapeWriterObserver<MyWriter> {
        private boolean onShapeWriterUseCalled;

        @Override
        public void observe(Shape shape, Symbol symbol, SymbolProvider symbolProvider, MyWriter writer) {
            onShapeWriterUseCalled = true;
        }
    }

    @Test
    public void createsSymbolsAndFilesForShapeWriters() {
        MockManifest mockManifest = new MockManifest();
        SymbolProvider provider = (shape) -> Symbol.builder()
                .namespace("com.foo", ".")
                .name("Baz")
                .definitionFile("com/foo/Baz.bam")
                .build();
        CodegenWriterDelegator<MyWriter> delegator = new CodegenWriterDelegator<>(
                mockManifest, provider, (f, n) -> new MyWriter(n));
        OnUse observer = new OnUse();
        delegator.setOnShaperWriterUseObserver(observer);
        Shape shape = StringShape.builder().id("com.foo#Baz").build();
        delegator.useShapeWriter(shape, writer -> { });

        assertThat(observer.onShapeWriterUseCalled, is(true));
        assertThat(delegator.getWriters(), hasKey("com/foo/Baz.bam"));
    }

    @Test
    public void canObserveAndWriteBeforeEachFile() {
        MockManifest mockManifest = new MockManifest();
        SymbolProvider provider = (shape) -> Symbol.builder()
                .namespace("com.foo", ".")
                .name("Baz")
                .definitionFile("com/foo/Baz.bam")
                .build();
        CodegenWriterDelegator<MyWriter> delegator = new CodegenWriterDelegator<>(
                mockManifest, provider, (f, n) -> new MyWriter(n));
        MyWriter.MyObserver observer = new MyWriter.MyObserver();
        delegator.setOnShaperWriterUseObserver(observer);
        Shape shape = StringShape.builder().id("com.foo#Baz").build();
        delegator.useShapeWriter(shape, writer -> {
            writer.write("Hello");
        });

        assertThat(delegator.getWriters().get("com/foo/Baz.bam").toString(),
                   equalTo("/// Writing com.foo#Baz\nHello\n"));
    }

    @Test
    public void aggregatesDependencies() {
        MockManifest mockManifest = new MockManifest();
        SymbolProvider provider = (shape) -> null;
        CodegenWriterDelegator<MyWriter> delegator = new CodegenWriterDelegator<>(
                mockManifest, provider, (f, n) -> new MyWriter(n));
        SymbolDependency dependency = SymbolDependency.builder()
                .packageName("x")
                .version("123")
                .build();

        delegator.useFileWriter("foo/baz", writer -> {
            writer.addDependency(dependency);
        });

        assertThat(delegator.getDependencies(), contains(dependency));
    }

    @Test
    public void writesNewlineBetweenFiles() {
        MockManifest mockManifest = new MockManifest();
        SymbolProvider provider = (shape) -> null;
        CodegenWriterDelegator<MyWriter> delegator = new CodegenWriterDelegator<>(
                mockManifest, provider, (f, n) -> new MyWriter(n));

        delegator.useFileWriter("foo/baz", writer -> {
            writer.write(".");
        });

        delegator.useFileWriter("foo/baz", writer -> {
            writer.write(".");
        });

        assertThat(delegator.getWriters().get("foo/baz").toString(), equalTo(".\n\n.\n"));
    }

    @Test
    public void canDisableNewlineBetweenFiles() {
        MockManifest mockManifest = new MockManifest();
        SymbolProvider provider = (shape) -> null;
        CodegenWriterDelegator<MyWriter> delegator = new CodegenWriterDelegator<>(
                mockManifest, provider, (f, n) -> new MyWriter(n));
        delegator.setAutomaticSeparator("");

        delegator.useFileWriter("foo/baz", writer -> {
            writer.writeInline(".");
        });

        delegator.useFileWriter("foo/baz", writer -> {
            writer.writeInline(".");
        });

        assertThat(delegator.getWriters().get("foo/baz").toString(), equalTo("..\n"));
    }

    @Test
    public void flushesAllWriters() {
        MockManifest mockManifest = new MockManifest();
        SymbolProvider provider = (shape) -> Symbol.builder()
                .namespace("com.foo", ".")
                .name("Baz")
                .definitionFile("com/foo/Baz.bam")
                .build();
        CodegenWriterDelegator<MyWriter> delegator = new CodegenWriterDelegator<>(
                mockManifest, provider, (f, n) -> new MyWriter(n));
        Shape shape = StringShape.builder().id("com.foo#Baz").build();
        delegator.useShapeWriter(shape, writer -> {
            writer.write("Hi!");
        });

        delegator.flushWriters();

        assertThat(mockManifest.getFileString("com/foo/Baz.bam"), equalTo(Optional.of("Hi!\n")));
    }
}
