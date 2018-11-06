package org.ogerardin.b2b.util;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ClassUtils;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class CandidateClassScanner extends ClassPathScanningCandidateComponentProvider {

    public CandidateClassScanner(TypeFilter... includeFilters) {
        super(false);
        for (TypeFilter includeFilter : includeFilters) {
            addIncludeFilter(includeFilter);
        }
    }

    @SuppressWarnings("unchecked")
    public final <T> Collection<Class<? extends T>> getCandidateClasses(String basePackage) {
        ClassLoader defaultClassLoader = ClassUtils.getDefaultClassLoader();
        Set<Class<? extends T>> classes = findCandidateComponents(basePackage).stream()
                .map(BeanDefinition::getBeanClassName)
                .map(bcn -> ClassUtils.resolveClassName(bcn, defaultClassLoader))
                .map(c -> (Class<? extends T>) c)
                .collect(Collectors.toSet());
        return classes;
    }

    public static <T> Collection<Class<? extends T>> getAssignableClasses(Class<T> clazz, String basePackage) {
        CandidateClassScanner candidateClassScanner = new CandidateClassScanner(
                new AssignableTypeFilter(clazz)
        );
        return candidateClassScanner.getCandidateClasses(basePackage);
    }

}