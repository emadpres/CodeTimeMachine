package com.reveal.codetimemachine;


import org.junit.Test;

import static org.junit.Assert.*;


public class CodeTimeMachineWindowTest
{
    @Test
    public void testInstantiating()
    {
        TTMSingleFileView TTMSingleFileView = new TTMSingleFileView(null, null, null);
        assertNotEquals(TTMSingleFileView.getComponent(), null);
    }
}
