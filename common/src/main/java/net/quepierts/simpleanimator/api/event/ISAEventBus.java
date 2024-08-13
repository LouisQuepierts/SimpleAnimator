package net.quepierts.simpleanimator.api.event;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public interface ISAEventBus {
    <T extends SAEvent> void addListener(Class<T> clazz, Consumer<T> listener);

    <T extends SAEvent> T post(T event);
}
