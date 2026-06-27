package util;

import org.junit.jupiter.api.Test;

import com.dfsek.terra.api.util.collection.TriStateIntCache;

import static org.junit.jupiter.api.Assertions.assertEquals;


class TriStateIntCacheTest {
    @Test
    void storesValuesAcrossWordBoundaries() {
        TriStateIntCache cache = new TriStateIntCache(193);

        cache.set(0, false);
        cache.set(31, true);
        cache.set(32, false);
        cache.set(192, true);

        assertEquals(TriStateIntCache.STATE_FALSE, cache.get(0));
        assertEquals(TriStateIntCache.STATE_TRUE, cache.get(31));
        assertEquals(TriStateIntCache.STATE_FALSE, cache.get(32));
        assertEquals(TriStateIntCache.STATE_TRUE, cache.get(192));
        assertEquals(TriStateIntCache.STATE_UNSET, cache.get(191));
    }

    @Test
    void preservesTheFirstStoredValue() {
        TriStateIntCache cache = new TriStateIntCache(1);

        cache.set(0, false);
        cache.set(0, true);

        assertEquals(TriStateIntCache.STATE_FALSE, cache.get(0));
    }
}
