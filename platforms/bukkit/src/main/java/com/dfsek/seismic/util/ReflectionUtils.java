/*
 * This file is part of Terra.
 *
 * Terra is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Terra is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Terra.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.dfsek.seismic.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;


/**
 * Bukkit distribution override for Seismic 2.5.7's reflection helper.
 *
 * <p>Seismic uses {@link Class#getField(String)} to locate {@code Unsafe.theUnsafe}, but that
 * method only returns public fields. This compatible implementation searches declared fields,
 * including inherited private fields, and avoids the spurious error emitted during pack loading.</p>
 */
public class ReflectionUtils {
    private static final ConcurrentHashMap<String, Class<?>> REFLECTED_CLASSES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<ClassMethod, Method> REFLECTED_METHODS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<ClassField, Field> REFLECTED_FIELDS = new ConcurrentHashMap<>();

    public ReflectionUtils() {
    }

    public static Class<?> getClass(String className) {
        return REFLECTED_CLASSES.computeIfAbsent(className, ReflectionUtils::findClass);
    }

    public static Method getMethod(Class<?> type, String methodName) {
        return REFLECTED_METHODS.computeIfAbsent(new ClassMethod(type, methodName), ReflectionUtils::findMethod);
    }

    public static Field getField(Class<?> type, String fieldName) {
        return REFLECTED_FIELDS.computeIfAbsent(new ClassField(type, fieldName), ReflectionUtils::findField);
    }

    public static void setMethodToPublic(Method method) {
        method.setAccessible(true);
    }

    public static void setFieldToPublic(Field field) {
        field.setAccessible(true);
    }

    private static Field findField(ClassField classField) {
        Class<?> type = classField.type();
        while(type != null) {
            try {
                return type.getDeclaredField(classField.name());
            } catch(NoSuchFieldException ignored) {
                type = type.getSuperclass();
            }
        }
        return null;
    }

    private static Method findMethod(ClassMethod classMethod) {
        try {
            return classMethod.type().getMethod(classMethod.name());
        } catch(NoSuchMethodException e) {
            return null;
        }
    }

    private static Class<?> findClass(String className) {
        try {
            return Class.forName(className);
        } catch(ClassNotFoundException e) {
            return null;
        }
    }

    public static <T extends Annotation> void ifAnnotationPresent(AnnotatedElement element, Class<? extends T> annotation,
                                                                  Consumer<T> operation) {
        T value = element.getAnnotation(annotation);
        if(value != null) operation.accept(value);
    }

    public static Class<?> getRawType(Type type) {
        if(type instanceof Class<?> rawType) {
            return rawType;
        } else if(type instanceof ParameterizedType parameterizedType) {
            return (Class<?>) parameterizedType.getRawType();
        } else if(type instanceof GenericArrayType genericArrayType) {
            return Array.newInstance(getRawType(genericArrayType.getGenericComponentType()), 0).getClass();
        } else if(type instanceof TypeVariable<?>) {
            return Object.class;
        } else if(type instanceof WildcardType wildcardType) {
            return getRawType(wildcardType.getUpperBounds()[0]);
        }

        String className = type == null ? "null" : type.getClass().getName();
        throw new IllegalArgumentException("Expected a Class, ParameterizedType, or GenericArrayType, but <" + type
                                           + "> is of type " + className);
    }

    public static String typeToString(Type type) {
        return type instanceof Class<?> rawType ? rawType.getName() : type.toString();
    }

    public static boolean equals(Type first, Type second) {
        if(first == second) {
            return true;
        } else if(first instanceof Class<?>) {
            return first.equals(second);
        } else if(first instanceof ParameterizedType firstParameterized) {
            if(!(second instanceof ParameterizedType secondParameterized)) return false;
            return Objects.equals(firstParameterized.getOwnerType(), secondParameterized.getOwnerType())
                   && firstParameterized.getRawType().equals(secondParameterized.getRawType())
                   && Arrays.equals(firstParameterized.getActualTypeArguments(), secondParameterized.getActualTypeArguments());
        } else if(first instanceof GenericArrayType firstArray) {
            return second instanceof GenericArrayType secondArray
                   && equals(firstArray.getGenericComponentType(), secondArray.getGenericComponentType());
        } else if(first instanceof WildcardType firstWildcard) {
            return second instanceof WildcardType secondWildcard
                   && Arrays.equals(firstWildcard.getUpperBounds(), secondWildcard.getUpperBounds())
                   && Arrays.equals(firstWildcard.getLowerBounds(), secondWildcard.getLowerBounds());
        } else if(first instanceof TypeVariable<?> firstVariable) {
            return second instanceof TypeVariable<?> secondVariable
                   && firstVariable.getGenericDeclaration() == secondVariable.getGenericDeclaration()
                   && firstVariable.getName().equals(secondVariable.getName());
        }
        return false;
    }

    private record ClassField(Class<?> type, String name) {
    }

    private record ClassMethod(Class<?> type, String name) {
    }
}
