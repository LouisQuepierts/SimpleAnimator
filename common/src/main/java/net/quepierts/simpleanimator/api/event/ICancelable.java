package net.quepierts.simpleanimator.api.event;

@SuppressWarnings("unused")
public interface ICancelable {
    void setCancel(boolean cancel);

    boolean isCanceled();
}
