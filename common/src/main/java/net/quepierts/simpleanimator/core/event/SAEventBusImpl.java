package net.quepierts.simpleanimator.core.event;

import com.mojang.logging.LogUtils;
import net.quepierts.simpleanimator.api.event.ISAEventBus;
import net.quepierts.simpleanimator.api.event.SAEvent;
import org.slf4j.Logger;

import java.util.IdentityHashMap;
import java.util.function.Consumer;

public class SAEventBusImpl implements ISAEventBus {
    private final Logger logger = LogUtils.getLogger();
    private final IdentityHashMap<Class<?>, ListenerList> map;

    public SAEventBusImpl() {
        map = new IdentityHashMap<>();
    }

    @Override
    public <T extends SAEvent> void addListener(Class<T> clazz, Consumer<T> listener) {
        this.map.computeIfAbsent(clazz, ListenerList::new).addListener(listener);
    }

    @Override
    public <T extends SAEvent> T post(T event) {
        ListenerList list = this.map.get(event.getClass());
        if (list == null)
            return event;

        try {
            for (Consumer<SAEvent> listener : list.getListeners()) {
                listener.accept(event);
            }
        } catch (Exception e) {
            logger.warn("Simple Animator Event Bus Error", e);
        }

        return event;
    }
}
