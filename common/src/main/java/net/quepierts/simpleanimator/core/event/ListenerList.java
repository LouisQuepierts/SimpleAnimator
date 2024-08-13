package net.quepierts.simpleanimator.core.event;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.quepierts.simpleanimator.api.event.SAEvent;

import java.util.List;
import java.util.function.Consumer;

public class ListenerList {
    private final List<Consumer<SAEvent>> listeners;

    public ListenerList() {
        listeners = new ObjectArrayList<>();
    }

    public ListenerList(Class<?> clazz) {
        listeners = new ObjectArrayList<>();
    }

    public List<Consumer<SAEvent>> getListeners() {
        return listeners;
    }

    @SuppressWarnings("unchecked")
    public <T extends SAEvent> void addListener(Consumer<T> listener) {
        this.listeners.add((Consumer<SAEvent>) listener);
    }
}
