/* Licensed Materials - Property of IBM                                   */
/*                                                                        */
/* SAMPLE                                                                 */
/*                                                                        */
/* (c) Copyright IBM Corp. 2016 All Rights Reserved                       */
/*                                                                        */
/* US Government Users Restricted Rights - Use, duplication or disclosure */
/* restricted by GSA ADP Schedule Contract with IBM Corp                  */
/*                                                                        */

package com.ibm.cicsdev.tsq;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

import com.ibm.cics.server.CicsConditionException;
import com.ibm.cics.server.ItemHolder;
import com.ibm.cics.server.TSQ;
import com.ibm.cics.server.TSQType;
import com.ibm.cics.server.Task;

/**
 * Simple example to demonstrate writing and reading a Java String to
 * and from a TSQ using JCICS, with manual maintenance of codepages.
 */
public class TSQExample2 extends TSQCommon
{
    /**
     * Name of the TSQ to use.
     */
    private static final String TSQ_NAME = "MYTSQ";

    /**
     * Number of items to write to the queue.
     */
    private static final int DEPTH_COUNT = 5;

    /**
     * Resolved at runtime to be the CCSID of the currently-executing JVM.
     */
    private static final String CCSID =
            System.getProperty("com.ibm.cics.jvmserver.local.ccsid");

    /**
     * Main entry point to a CICS OSGi program.
     * 
     * The FQ name of this class should be added to the CICS-MainClass entry in
     * the parent OSGi bundle's manifest.
     */
    public static void main(String[] args)
    {
        // Get details about our current CICS task
        Task task = Task.getTask();
        task.out.println(" - Starting TSQExample2");

        // Create a reference to the TSQ
        TSQ tsq = new TSQ();
        
        // Specify the queue name and that we are using main storage        
        tsq.setName(TSQ_NAME);
        tsq.setType(TSQType.MAIN);

        // Create a new instance of this class
        TSQExample2 ex = new TSQExample2(tsq);
        
        // Write text to the queue
        ex.writeToQueue();
        
        // Read text from the queue
        ex.readFromQueue();
        
        // Completion message
        task.out.println("Completed TSQExample2");
    }
    
    /**
     * Constructor used to pass data to superclass constructor.
     * 
     * @param tsq - the temporary storage queue reference we will
     * be manipulating in this example.
     */
    public TSQExample2(TSQ tsq)
    {
        super(tsq);
    }

    /**
     * Write of Java string data to a TSQ.
     */
    public void writeToQueue()
    {
        // Write several items to the queue
        for (int i = 1; i <= DEPTH_COUNT; i++) {

            // Data to write to the TSQ
            byte[] data;
            
            // Construct a message for writing to the queue
            String msg = MessageFormat.format("TSQ write from JCICS item {0}", i);
        
            try {
                // Convert the string to a byte[]
                data = msg.getBytes(CCSID);
            }
            catch (UnsupportedEncodingException uee) {
                // Crude error handling - propagate an exception back to caller
                throw new RuntimeException(uee);
            }

            try {
                // Write the data to the TSQ
                this.tsq.writeItem(data);
            }
            catch (CicsConditionException cce) {
                // Crude error handling - propagate an exception back to caller
                throw new RuntimeException(cce);
            }
        }
    }
    
    /**
     * Simple read of string data from a TSQ.
     */
    public void readFromQueue()
    {
        // A holder object to receive the data from CICS
        ItemHolder holder = new ItemHolder();
        
        // Read data from the queue
        for (int i = 1; i <= DEPTH_COUNT; i++) {
            
            try {
                // Read the data from CICS (TSQ indices are 1-based)
                this.tsq.readItem(i, holder);
            }
            catch (CicsConditionException cce) {
                // Crude error handling - propagate an exception back to caller
                throw new RuntimeException(cce);
            }

            // Extract the byte[] data
            byte[] data = holder.getValue();
            
            // String object to create
            String strData;

            try {
                // Assume this is a valid character string in the CICS local CCSID
                strData = new String(data, CCSID);
            }
            catch (UnsupportedEncodingException uee) {
                // Crude error handling - propagate an exception back to caller
                throw new RuntimeException(uee);
            }
            
            // Write out to the console
            String msg = MessageFormat.format("Read data from queue \"{0}\"", strData);
            this.task.out.println(msg);
        }
    }    
}
