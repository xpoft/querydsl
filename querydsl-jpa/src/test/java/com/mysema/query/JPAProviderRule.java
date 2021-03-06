package com.mysema.query;

import java.lang.annotation.Annotation;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.mysema.testutil.EmptyStatement;

/**
 * @author tiwe
 *
 */
public class JPAProviderRule implements MethodRule {

    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        boolean noEclipseLink = hasAnnotation(method, NoEclipseLink.class);
        boolean noOpenJPA = hasAnnotation(method, NoOpenJPA.class);
        String mode = Mode.mode.get();
        if (noEclipseLink && mode.contains("-eclipselink")) {
            return EmptyStatement.DEFAULT;
        } else if (noOpenJPA && mode.contains("-openjpa")) {
            return EmptyStatement.DEFAULT;
        } else {
            return base;
        }
    }
    
    private <T extends Annotation> boolean hasAnnotation(FrameworkMethod method, Class<T> clazz) {
        T rv = method.getMethod().getAnnotation(clazz);
        if (rv == null) {
            rv = method.getMethod().getDeclaringClass().getAnnotation(clazz);
        }
        return rv != null;
    }

}
