package org.jbehave.container.pico;

import static java.text.MessageFormat.format;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import org.jbehave.container.ComponentNotFoundException;
import org.jbehave.container.Container;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.script.ScriptedContainerBuilder;

/**
 * <p>
 * Abstract implementation of Container which uses a PicoContainer as delegate
 * container.
 * </p>
 * 
 * @author Mauro Talevi
 */
public abstract class AbstractPicoContainer implements Container {

    private final ClassLoader classLoader;
    private final PicoContainer container;

    protected AbstractPicoContainer(String resource) {
        this(resource, Thread.currentThread().getContextClassLoader());
    }

    protected AbstractPicoContainer(String resource, ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.container = buildContainer(resource);
    }

    public <T> T getComponent(Class<T> type) {
        return getPicoComponent(type, null);
    }

    public <T> T getComponent(Class<T> type, Object key) {
        return getPicoComponent(type, key);
    }

    public <T> Collection<Object> getComponentKeys(Class<T> type) {
        List<ComponentAdapter<T>> adapters = container.getComponentAdapters(type);
        List<Object> keys = new ArrayList<Object>();
        for (ComponentAdapter<T> adapter : adapters) {
            keys.add(adapter.getComponentKey());
        }
        return keys;
    }

    /**
     * Returns an instance of a component of a given type and key from the
     * delegate PicoContainer.
     * 
     * @param type the component Class
     * @param key the component key
     * @return A component instance for the given type and key, if provided
     * @throws NoComponentOfTypeException if no Component can be found of type
     * @throws ComponentNotFoundException if Component not found for key
     */
    private <T> T getPicoComponent(Class<T> type, Object key) {
        List<ComponentAdapter<T>> adapters = container.getComponentAdapters(type);
        if (adapters.isEmpty()) {
            String message = format("No component registered in container of type {0}", type);
            throw new ComponentNotFoundException(message);
        }
        if (key != null) {
            // a key has been provided: return the component for that key
            for (ComponentAdapter<T> adapter : adapters) {
                if (key.equals(adapter.getComponentKey())) {
                    return adapter.getComponentInstance(container, type);
                }
            }
            String message = format("No component registered in container of type {0} and for key {1}", type, key);
            throw new ComponentNotFoundException(message);
        } else {
            // no key has been found:
            // return first of registered components
            return adapters.iterator().next().getComponentInstance(container, type);
        }
    }

    /**
     * Builds PicoContainer from a given resource
     * 
     * @param resource the String encoding the script path
     * @return A PicoContainer
     */
    private PicoContainer buildContainer(String resource) {
        Reader script = getReader(resource, classLoader);
        ScriptedContainerBuilder builder = createContainerBuilder(script, classLoader);
        return builder.buildContainer(null, null, true);
    }

    private Reader getReader(String resource, ClassLoader classLoader) {
        InputStream is = classLoader.getResourceAsStream(resource);
        if (is == null) {
            String message = format("Resource {0} not found in ClassLoader {1}", resource, classLoader.getClass(),
                    classLoader);
            throw new NoSuchElementException(message);
        }
        return new InputStreamReader(is);
    }

    /**
     * Allow concrete implementations to specify a ScriptedContainerBuilder
     * 
     * @param script the Reader containing the container script
     * @param classLoader the ClassLoader
     * @return A ScriptedContainerBuilder
     */
    protected abstract ScriptedContainerBuilder createContainerBuilder(Reader script, ClassLoader classLoader);

}