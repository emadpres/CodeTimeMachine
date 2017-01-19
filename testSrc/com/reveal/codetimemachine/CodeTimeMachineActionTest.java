package com.reveal.codetimemachine;

import org.junit.*;
import static org.junit.Assert.*;


public class CodeTimeMachineActionTest
{

    @Test
    public void testInstantiating()
    {
        CodeTimeMachineAction codeTimeMachineAction = new CodeTimeMachineAction();
        assertNotEquals(codeTimeMachineAction, null);
    }
}
