/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.Sizeable;

@SuppressWarnings("serial")
public abstract class AbstractOrderedLayout extends AbstractLayout implements
        Layout.AlignmentHandler, Layout.SpacingHandler {

    private static final Alignment ALIGNMENT_DEFAULT = Alignment.TOP_LEFT;

    /**
     * Custom layout slots containing the components.
     */
    protected LinkedList<Component> components = new LinkedList<Component>();

    /* Child component alignments */

    /**
     * Mapping from components to alignments (horizontal + vertical).
     */
    private final Map<Component, Alignment> componentToAlignment = new HashMap<Component, Alignment>();

    private final Map<Component, Float> componentToExpandRatio = new HashMap<Component, Float>();

    /**
     * Is spacing between contained components enabled. Defaults to false.
     */
    private boolean spacing = false;

    /**
     * Add a component into this container. The component is added to the right
     * or under the previous component.
     * 
     * @param c
     *            the component to be added.
     */
    @Override
    public void addComponent(Component c) {
        super.addComponent(c);
        components.add(c);
        requestRepaint();
    }

    /**
     * Adds a component into this container. The component is added to the left
     * or on top of the other components.
     * 
     * @param c
     *            the component to be added.
     */
    public void addComponentAsFirst(Component c) {
        super.addComponent(c);
        components.addFirst(c);
        requestRepaint();
    }

    /**
     * Adds a component into indexed position in this container.
     * 
     * @param c
     *            the component to be added.
     * @param index
     *            the Index of the component position. The components currently
     *            in and after the position are shifted forwards.
     */
    public void addComponent(Component c, int index) {
        super.addComponent(c);
        components.add(index, c);
        requestRepaint();
    }

    /**
     * Removes the component from this container.
     * 
     * @param c
     *            the component to be removed.
     */
    @Override
    public void removeComponent(Component c) {
        super.removeComponent(c);
        components.remove(c);
        componentToAlignment.remove(c);
        componentToExpandRatio.remove(c);
        requestRepaint();
    }

    /**
     * Gets the component container iterator for going trough all the components
     * in the container.
     * 
     * @return the Iterator of the components inside the container.
     */
    public Iterator getComponentIterator() {
        return components.iterator();
    }

    /**
     * Paints the content of this component.
     * 
     * @param target
     *            the Paint Event.
     * @throws PaintException
     *             if the paint operation failed.
     */
    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);

        // Add spacing attribute (omitted if false)
        if (spacing) {
            target.addAttribute("spacing", spacing);
        }

        // Adds all items in all the locations
        for (Component c : components) {
            // Paint child component UIDL
            c.paint(target);
        }

        // Add child component alignment info to layout tag
        target.addAttribute("alignments", componentToAlignment);
        target.addAttribute("expandRatios", componentToExpandRatio);
    }

    /* Documented in superclass */
    public void replaceComponent(Component oldComponent, Component newComponent) {

        // Gets the locations
        int oldLocation = -1;
        int newLocation = -1;
        int location = 0;
        for (final Iterator i = components.iterator(); i.hasNext();) {
            final Component component = (Component) i.next();

            if (component == oldComponent) {
                oldLocation = location;
            }
            if (component == newComponent) {
                newLocation = location;
            }

            location++;
        }

        if (oldLocation == -1) {
            addComponent(newComponent);
        } else if (newLocation == -1) {
            removeComponent(oldComponent);
            addComponent(newComponent, oldLocation);
        } else {
            if (oldLocation > newLocation) {
                components.remove(oldComponent);
                components.add(newLocation, oldComponent);
                components.remove(newComponent);
                componentToAlignment.remove(newComponent);
                components.add(oldLocation, newComponent);
            } else {
                components.remove(newComponent);
                components.add(oldLocation, newComponent);
                components.remove(oldComponent);
                componentToAlignment.remove(oldComponent);
                components.add(newLocation, oldComponent);
            }

            requestRepaint();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.ui.Layout.AlignmentHandler#setComponentAlignment(com
     * .vaadin.ui.Component, int, int)
     */
    public void setComponentAlignment(Component childComponent,
            int horizontalAlignment, int verticalAlignment) {
        if (components.contains(childComponent)) {
            // Alignments are bit masks
            componentToAlignment.put(childComponent, new Alignment(
                    horizontalAlignment + verticalAlignment));
            requestRepaint();
        } else {
            throw new IllegalArgumentException(
                    "Component must be added to layout before using setComponentAlignment()");
        }
    }

    public void setComponentAlignment(Component childComponent,
            Alignment alignment) {
        if (components.contains(childComponent)) {
            componentToAlignment.put(childComponent, alignment);
            requestRepaint();
        } else {
            throw new IllegalArgumentException(
                    "Component must be added to layout before using setComponentAlignment()");
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.ui.Layout.AlignmentHandler#getComponentAlignment(com
     * .vaadin.ui.Component)
     */
    public Alignment getComponentAlignment(Component childComponent) {
        Alignment alignment = componentToAlignment.get(childComponent);
        if (alignment == null) {
            return ALIGNMENT_DEFAULT;
        } else {
            return alignment;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.ui.Layout.SpacingHandler#setSpacing(boolean)
     */
    public void setSpacing(boolean enabled) {
        spacing = enabled;
        requestRepaint();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.ui.Layout.SpacingHandler#isSpacing()
     */
    @Deprecated
    public boolean isSpacingEnabled() {
        return spacing;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.ui.Layout.SpacingHandler#isSpacing()
     */
    public boolean isSpacing() {
        return spacing;
    }

    /**
     * <p>
     * This method is used to control how excess space in layout is distributed
     * among components. Excess space may exist if layout is sized and contained
     * non relatively sized components don't consume all available space.
     * 
     * <p>
     * Example how to distribute 1:3 (33%) for component1 and 2:3 (67%) for
     * component2 :
     * 
     * <code>
     * layout.setExpandRatio(component1, 1);<br>
     * layout.setExpandRatio(component2, 2);
     * </code>
     * 
     * <p>
     * If no ratios have been set, the excess space is distributed evenly among
     * all components.
     * 
     * <p>
     * Note, that width or height (depending on orientation) needs to be defined
     * for this method to have any effect.
     * 
     * @see Sizeable
     * 
     * @param component
     *            the component in this layout which expand ratio is to be set
     * @param ratio
     */
    public void setExpandRatio(Component component, float ratio) {
        if (components.contains(component)) {
            componentToExpandRatio.put(component, ratio);
            requestRepaint();
        } else {
            throw new IllegalArgumentException(
                    "Component must be added to layout before using setExpandRatio()");
        }
    };

    /**
     * Returns the expand ratio of given component.
     * 
     * @param component
     *            which expand ratios is requested
     * @return expand ratio of given component, 0.0f by default
     */
    public float getExpandRatio(Component component) {
        Float ratio = componentToExpandRatio.get(component);
        return (ratio == null) ? 0 : ratio.floatValue();
    }

    public void setComponentAlignment(Component component, String alignment) {
        AlignmentUtils.setComponentAlignment(this, component, alignment);
    }

}
