/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

/**
 * An enum that includes an abstract method (because it triggers a corner case).
 */
public enum EnumWithAbstractMethod {
    ONE {
        @Override
        public String bar() { return "One"; }
    },
    TWO {
        @Override
        public String bar() { return "Two";}
    };

    public abstract String bar();
}
