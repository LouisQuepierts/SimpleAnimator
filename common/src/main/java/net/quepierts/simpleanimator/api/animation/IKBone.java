package net.quepierts.simpleanimator.api.animation;

public enum IKBone {
    HEAD("ikHead"),
    LEFT_ARM("ikLeftArm"),
    RIGHT_ARM("ikRightArm")
    /*,
    LEFT_LEG("ikLeftLeg"),
    RIGHT_LEG("ikRightLeg")*/;

    public final String varName;

    IKBone(String varName) {
        this.varName = varName;
    }
}
