/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashMultimap
 *  com.google.common.collect.Multimap
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public interface ProblemReporter {
    public static final ProblemReporter DISCARDING = new ProblemReporter(){

        @Override
        public ProblemReporter forChild(PathElement pathElement) {
            return this;
        }

        @Override
        public void report(Problem problem) {
        }
    };

    public ProblemReporter forChild(PathElement var1);

    public void report(Problem var1);

    public static class ScopedCollector
    extends Collector
    implements AutoCloseable {
        private final Logger logger;

        public ScopedCollector(Logger logger) {
            this.logger = logger;
        }

        public ScopedCollector(PathElement pathElement, Logger logger) {
            super(pathElement);
            this.logger = logger;
        }

        @Override
        public void close() {
            if (!this.isEmpty()) {
                this.logger.warn("[{}] Serialization errors:\n{}", (Object)this.logger.getName(), (Object)this.getTreeReport());
            }
        }
    }

    public static class Collector
    implements ProblemReporter {
        public static final PathElement EMPTY_ROOT = () -> "";
        private final @Nullable Collector parent;
        private final PathElement element;
        private final Set<Entry> problems;

        public Collector() {
            this(EMPTY_ROOT);
        }

        public Collector(PathElement pathElement) {
            this.parent = null;
            this.problems = new LinkedHashSet<Entry>();
            this.element = pathElement;
        }

        private Collector(Collector collector, PathElement pathElement) {
            this.problems = collector.problems;
            this.parent = collector;
            this.element = pathElement;
        }

        @Override
        public ProblemReporter forChild(PathElement pathElement) {
            return new Collector(this, pathElement);
        }

        @Override
        public void report(Problem problem) {
            this.problems.add(new Entry(this, problem));
        }

        public boolean isEmpty() {
            return this.problems.isEmpty();
        }

        public void forEach(BiConsumer<String, Problem> biConsumer) {
            ArrayList<PathElement> list = new ArrayList<PathElement>();
            StringBuilder stringBuilder = new StringBuilder();
            for (Entry entry : this.problems) {
                Collector collector = entry.source;
                while (collector != null) {
                    list.add(collector.element);
                    collector = collector.parent;
                }
                for (int i = list.size() - 1; i >= 0; --i) {
                    stringBuilder.append(((PathElement)list.get(i)).get());
                }
                biConsumer.accept(stringBuilder.toString(), entry.problem());
                stringBuilder.setLength(0);
                list.clear();
            }
        }

        public String getReport() {
            HashMultimap multimap = HashMultimap.create();
            this.forEach((arg_0, arg_1) -> ((Multimap)multimap).put(arg_0, arg_1));
            return multimap.asMap().entrySet().stream().map(entry -> " at " + (String)entry.getKey() + ": " + ((Collection)entry.getValue()).stream().map(Problem::description).collect(Collectors.joining("; "))).collect(Collectors.joining("\n"));
        }

        public String getTreeReport() {
            ArrayList<PathElement> list = new ArrayList<PathElement>();
            ProblemTreeNode problemTreeNode = new ProblemTreeNode(this.element);
            for (Entry entry : this.problems) {
                Collector collector = entry.source;
                while (collector != this) {
                    list.add(collector.element);
                    collector = collector.parent;
                }
                ProblemTreeNode problemTreeNode2 = problemTreeNode;
                for (int i = list.size() - 1; i >= 0; --i) {
                    problemTreeNode2 = problemTreeNode2.child((PathElement)list.get(i));
                }
                list.clear();
                problemTreeNode2.problems.add(entry.problem);
            }
            return String.join((CharSequence)"\n", problemTreeNode.getLines());
        }

        static final class Entry
        extends Record {
            final Collector source;
            final Problem problem;

            Entry(Collector collector, Problem problem) {
                this.source = collector;
                this.problem = problem;
            }

            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "source;problem", "source", "problem"}, this);
            }

            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "source;problem", "source", "problem"}, this);
            }

            public final boolean equals(Object object) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "source;problem", "source", "problem"}, this, object);
            }

            public Collector source() {
                return this.source;
            }

            public Problem problem() {
                return this.problem;
            }
        }

        static final class ProblemTreeNode
        extends Record {
            private final PathElement element;
            final List<Problem> problems;
            private final Map<PathElement, ProblemTreeNode> children;

            public ProblemTreeNode(PathElement pathElement) {
                this(pathElement, new ArrayList<Problem>(), new LinkedHashMap<PathElement, ProblemTreeNode>());
            }

            private ProblemTreeNode(PathElement pathElement, List<Problem> list, Map<PathElement, ProblemTreeNode> map) {
                this.element = pathElement;
                this.problems = list;
                this.children = map;
            }

            public ProblemTreeNode child(PathElement pathElement) {
                return this.children.computeIfAbsent(pathElement, ProblemTreeNode::new);
            }

            public List<String> getLines() {
                int i = this.problems.size();
                int j = this.children.size();
                if (i == 0 && j == 0) {
                    return List.of();
                }
                if (i == 0 && j == 1) {
                    ArrayList<String> list = new ArrayList<String>();
                    this.children.forEach((pathElement, problemTreeNode) -> list.addAll(problemTreeNode.getLines()));
                    list.set(0, this.element.get() + (String)list.get(0));
                    return list;
                }
                if (i == 1 && j == 0) {
                    return List.of((Object)(this.element.get() + ": " + ((Problem)this.problems.getFirst()).description()));
                }
                ArrayList<String> list = new ArrayList<String>();
                this.children.forEach((pathElement, problemTreeNode) -> list.addAll(problemTreeNode.getLines()));
                list.replaceAll(string -> "  " + string);
                for (Problem problem : this.problems) {
                    list.add("  " + problem.description());
                }
                list.addFirst(this.element.get() + ":");
                return list;
            }

            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{ProblemTreeNode.class, "element;problems;children", "element", "problems", "children"}, this);
            }

            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ProblemTreeNode.class, "element;problems;children", "element", "problems", "children"}, this);
            }

            public final boolean equals(Object object) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ProblemTreeNode.class, "element;problems;children", "element", "problems", "children"}, this, object);
            }

            public PathElement element() {
                return this.element;
            }

            public List<Problem> problems() {
                return this.problems;
            }

            public Map<PathElement, ProblemTreeNode> children() {
                return this.children;
            }
        }
    }

    public record ElementReferencePathElement(ResourceKey<?> id) implements PathElement
    {
        @Override
        public String get() {
            return "->{" + String.valueOf(this.id.identifier()) + "@" + String.valueOf(this.id.registry()) + "}";
        }
    }

    public record IndexedPathElement(int index) implements PathElement
    {
        @Override
        public String get() {
            return "[" + this.index + "]";
        }
    }

    public record IndexedFieldPathElement(String name, int index) implements PathElement
    {
        @Override
        public String get() {
            return "." + this.name + "[" + this.index + "]";
        }
    }

    public record FieldPathElement(String name) implements PathElement
    {
        @Override
        public String get() {
            return "." + this.name;
        }
    }

    public record RootElementPathElement(ResourceKey<?> id) implements PathElement
    {
        @Override
        public String get() {
            return "{" + String.valueOf(this.id.identifier()) + "@" + String.valueOf(this.id.registry()) + "}";
        }
    }

    public record RootFieldPathElement(String name) implements PathElement
    {
        @Override
        public String get() {
            return this.name;
        }
    }

    @FunctionalInterface
    public static interface PathElement {
        public String get();
    }

    public static interface Problem {
        public String description();
    }
}

