package org.example.core;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhy
 * @date 2024/8/23 15:33
 */
public enum ANNOTATION_ENUM {
    COMPONENT("Component", Component.class, true),
    REPOSITORY("Repository", Repository.class, true),
    SERVICE("Service", Service.class, true),
    CONTROLLER("Controller", Controller.class, true),
    REST_CONTROLLER("RestController", RestController.class, true),
    OTHER("Other", Object.class, false);

    private String name;
    private Class<?> annotationClass;
    private boolean isAnnotation;

    ANNOTATION_ENUM(String name, Class<?> annotationClass, boolean isAnnotation) {
        this.name = name;
        this.annotationClass = annotationClass;
        this.isAnnotation = isAnnotation;
    }

    public String getName() {
        return name;
    }

    public Class<?> getAnnotationClass() {
        return annotationClass;
    }

    public boolean isAnnotation() {
        return isAnnotation;
    }
}
