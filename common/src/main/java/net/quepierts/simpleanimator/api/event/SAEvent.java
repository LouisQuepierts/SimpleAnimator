package net.quepierts.simpleanimator.api.event;

public abstract class SAEvent {
    protected boolean canceled = false;

    protected SAEvent() {}

    public void setCancel(boolean canceled) {
        if (this instanceof ICancelable) {
            this.canceled = canceled;
        }
    }

    public boolean isCanceled() {
        return canceled;
    }
}
