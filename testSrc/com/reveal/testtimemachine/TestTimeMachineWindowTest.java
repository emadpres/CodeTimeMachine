package com.reveal.testtimemachine;


import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;


public class TestTimeMachineWindowTest
{
    @Test
    public void testInstantiating()
    {
        TestTimeMachineWindow testTimeMachineWindow = new TestTimeMachineWindow(null, null, null);
        assertNotEquals(testTimeMachineWindow.getComponent(), null);
    }
}
