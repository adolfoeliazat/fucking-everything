package org.junit.runners;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.implementation.MethodDelegation;
import org.junit.Test;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.internal.builders.MyAllDefaultPossibilitiesBuilder;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.lang.annotation.*;
import java.util.Collections;
import java.util.List;

/**
 * Using <code>Suite</code> as a runner allows you to manually
 * build a suite containing tests from many classes. It is the JUnit 4 equivalent of the JUnit 3.8.x
 * static {@link junit.framework.Test} <code>suite()</code> method. To use it, annotate a class
 * with <code>@RunWith(Suite.class)</code> and <code>@SuiteClasses({TestClass1.class, ...})</code>.
 * When you run this class, it will run all the tests in all the suite classes.
 *
 * @since 4.0
 */
public class MySuite2 extends ParentRunner<Runner> {
    /**
     * Returns an empty suite.
     */
    public static Runner emptySuite() {
        try {
            return new MySuite2((Class<?>) null, new Class<?>[0]);
        } catch (InitializationError e) {
            throw new RuntimeException("This shouldn't be possible");
        }
    }

    /**
     * The <code>SuiteClasses</code> annotation specifies the classes to be run when a class
     * annotated with <code>@RunWith(Suite.class)</code> is run.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface SuiteClasses {
        /**
         * @return the classes to be run
         */
        public Class<?>[] value();
    }

    public static class Motherfucker {
        public void goBananas() {
            System.out.println("Hooooooolyyyyyyyyy fuuuuuuuuuck");
        }
    }

    private static Class<?>[] getAnnotatedClasses(Class<?> klass) throws Exception {
        if (MySuite.GeneratedSuite.class.isAssignableFrom(klass)) {
            Class<?> generatedClass = new ByteBuddy()
                    .subclass(Object.class)
                    .name("GeneratedShit_" + System.currentTimeMillis())
                    .defineMethod("pizda", void.class, Visibility.PUBLIC)
                    .intercept(MethodDelegation.to(new Motherfucker()))
                    .annotateMethod(AnnotationDescription.Builder.ofType(Test.class).build())
                    .make()
                    .load(MySuite2.class.getClassLoader())
                    .getLoaded();

            return new Class<?>[] {generatedClass};
        } else {
            throw new IllegalStateException("99d6a855-50f5-437a-ac2c-47feb4460b7d");
        }
    }

    private final List<Runner> runners;

    /**
     * Called reflectively on classes annotated with <code>@RunWith(Suite.class)</code>
     *
     * @param klass the root class
     * @param builder builds runners for classes in the suite
     */
    public MySuite2(Class<?> klass, RunnerBuilder builder) throws Exception {
        this(new MyAllDefaultPossibilitiesBuilder(false), klass, getAnnotatedClasses(klass));
    }

    /**
     * Call this when there is no single root class (for example, multiple class names
     * passed on the command line to {@link org.junit.runner.JUnitCore}
     *
     * @param builder builds runners for classes in the suite
     * @param classes the classes in the suite
     */
    public MySuite2(RunnerBuilder builder, Class<?>[] classes) throws InitializationError {
        this(null, builder.runners(null, classes));
    }

    /**
     * Call this when the default builder is good enough. Left in for compatibility with JUnit 4.4.
     *
     * @param klass the root of the suite
     * @param suiteClasses the classes in the suite
     */
    protected MySuite2(Class<?> klass, Class<?>[] suiteClasses) throws InitializationError {
        this(new AllDefaultPossibilitiesBuilder(true), klass, suiteClasses);
    }

    /**
     * Called by this class and subclasses once the classes making up the suite have been determined
     *
     * @param builder builds runners for classes in the suite
     * @param klass the root of the suite
     * @param suiteClasses the classes in the suite
     */
    protected MySuite2(RunnerBuilder builder, Class<?> klass, Class<?>[] suiteClasses) throws InitializationError {
        this(klass, builder.runners(klass, suiteClasses));
    }

    /**
     * Called by this class and subclasses once the runners making up the suite have been determined
     *
     * @param klass root of the suite
     * @param runners for each class in the suite, a {@link Runner}
     */
    protected MySuite2(Class<?> klass, List<Runner> runners) throws InitializationError {
        super(klass);
        this.runners = Collections.unmodifiableList(runners);
    }

    @Override
    protected List<Runner> getChildren() {
        return runners;
    }

    @Override
    protected Description describeChild(Runner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(Runner runner, final RunNotifier notifier) {
        runner.run(notifier);
    }
}
