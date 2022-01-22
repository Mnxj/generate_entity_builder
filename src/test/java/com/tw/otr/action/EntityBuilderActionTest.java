package com.tw.otr.action;

import org.junit.jupiter.api.Test;

import static com.tw.otr.util.Utils.getVariable;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EntityBuilderActionTest {

    @Test
    void should_return_Refund_when_given_isRefund(){
        String variable = "isRefund";
        assertEquals("refund",getVariable(variable));
    }

    @Test
    void should_return_Refund_when_given_refund(){
        String variable = "refund";
        assertEquals("refund",getVariable(variable));
    }
}
