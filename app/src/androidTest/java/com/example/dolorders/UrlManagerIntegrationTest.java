package com.example.dolorders;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests d'intégration avec le contexte Android
 * Vérifie le fonctionnement réel avec le système de fichiers interne Android.
 */
@RunWith(AndroidJUnit4.class)
public class UrlManagerIntegrationTest {

    private UrlManager manager;
    private Context context;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        manager = new UrlManager(context);
        manager.clearAllUrls();
    }

    @Test
    public void testAddUrlRealContext() {
        assertTrue(manager.addUrl("https://site.com"));
        List<String> urls = manager.getAllUrls();

        assertEquals(1, urls.size());
        assertEquals("https://site.com", urls.get(0));
    }

    @Test
    public void testPersistenceBetweenInstances() {

        manager.addUrl("http://persist.com");

        UrlManager newManager = new UrlManager(context);

        List<String> urls = newManager.getAllUrls();
        assertEquals(1, urls.size());
        assertEquals("http://persist.com", urls.get(0));
    }

    @Test
    public void testRemoveUrlAndroid() {
        manager.addUrl("A");
        manager.addUrl("B");

        assertTrue(manager.removeUrl("A"));

        List<String> urls = manager.getAllUrls();
        assertEquals(1, urls.size());
        assertEquals("B", urls.get(0));
    }

    @Test
    public void testClearAllAndroid() {
        manager.addUrl("A");
        manager.addUrl("B");
        assertTrue(manager.clearAllUrls());
        assertTrue(manager.getAllUrls().isEmpty());
    }

    @Test
    public void testLimit10Android() {
        for (int i = 1; i <= 15; i++) {
            manager.addUrl("U" + i);
        }

        List<String> urls = manager.getAllUrls();

        assertEquals(10, urls.size());          // limité à 10
        assertEquals("U15", urls.get(0));       // dernier ajouté first
        assertEquals("U6", urls.get(9));        // 6 à 15 conservés
    }
}
