package com.reveal.testtimemachine;


import org.junit.Test;

import static org.junit.Assert.*;


public class TestTimeMachineWindowTest
{
    @Test
    public void testInstantiating()
    {
        TTMSingleFileView TTMSingleFileView = new TTMSingleFileView(null, null, null);
        assertNotEquals(TTMSingleFileView.getComponent(), null);
    }
}
