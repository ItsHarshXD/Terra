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

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class ReflectionUtilsTest {
    @Test
    void findsPrivateField() throws IllegalAccessException {
        Field field = ReflectionUtils.getField(PrivateFieldHolder.class, "value");

        assertNotNull(field);
        ReflectionUtils.setFieldToPublic(field);
        assertEquals(42, field.get(new PrivateFieldHolder()));
    }

    @Test
    void findsInheritedPrivateField() {
        assertNotNull(ReflectionUtils.getField(Child.class, "value"));
    }

    private static class PrivateFieldHolder {
        private final int value = 42;
    }

    private static final class Child extends PrivateFieldHolder {
    }
}
