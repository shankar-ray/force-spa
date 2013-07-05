/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.spa.core.testbeans;

import com.force.spa.SalesforceField;

@SuppressWarnings("ALL")
public class EnumBean {

    private State state;

    public State getState() {
        return state;
    }

    @SalesforceField
    public void setState(State state) {
        this.state = state;
    }

    // An enum that includes an abstract method (because it triggers a corner case).
    public static enum State {
        ONE {
            @Override
            public void bar() {}
        },
        TWO {
            @Override
            public void bar() {}
        };

        public abstract void bar();
    }
}
