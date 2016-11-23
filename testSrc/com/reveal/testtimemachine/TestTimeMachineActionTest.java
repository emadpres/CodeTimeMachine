package com.reveal.testtimemachine;

import org.junit.*;
import static org.junit.Assert.*;


public class TestTimeMachineActionTest
{

    @Test
    public void testInstantiating()
    {
        TestTimeMachineAction testTimeMachineAction = new TestTimeMachineAction();
        assertNotEquals(testTimeMachineAction, null);
    }
}
