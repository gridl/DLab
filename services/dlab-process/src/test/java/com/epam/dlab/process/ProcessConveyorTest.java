/*
Copyright 2016 EPAM Systems, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.epam.dlab.process;

import com.epam.dlab.process.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.Assert.*;


public class ProcessConveyorTest {

    private static String OS = System.getProperty("os.name").toLowerCase();

    private static boolean windows;

    static {
        windows = OS.contains("win");
    }

    private String user = "user";

    //@Test
    public void testLs() throws Exception {
		CompletableFuture<ProcessInfo> cf = DlabProcess.getInstance().start(user, "ls", ProcessType.MOCKED_PROCESS,
				"", (windows ? "cmd /c \"dir " + ".\"" : "ls .").split(" "));
        ProcessInfo pi = cf.get();
        System.out.println("--- "+pi);
        assertEquals(ProcessStatus.FINISHED, pi.getStatus());

    }

    //@Test
    public void testLsErr() throws Exception {
		ProcessData ls = new ProcessData(user, "ls");
		CompletableFuture<ProcessInfo> cf = DlabProcess.getInstance().start(user, "ls", ProcessType.MOCKED_PROCESS,
				"", "l/");
        ProcessInfo pi = cf.get();
        System.out.println("--- "+pi);
        assertEquals(ProcessStatus.FAILED, pi.getStatus());

    }

    //@Test
    public void testPingsWithLimitedThreadCapacity() throws Exception {

        String pingCommand;

        if(windows) {
            pingCommand = "ping -n 1 localhost";
        } else {
            pingCommand = "ping -c 1 localhost";
        }

        ArrayList<CompletableFuture<ProcessInfo>> cf = new ArrayList<>();
        DlabProcess.getInstance().setMaxProcessesPerBox(2);
        for(int i = 0; i < 5; i++) {
			cf.add(DlabProcess.getInstance().start(user, "ping " + i, ProcessType.MOCKED_PROCESS, "",
					pingCommand.split(" " + "")));
        }
        Thread.sleep(100);
		Collection<ProcessData> pIds = DlabProcess.getInstance().getActiveProcesses();
        System.out.println(pIds);
        Thread.sleep(3000);
        for (CompletableFuture<ProcessInfo> f:cf){
            ProcessInfo pi = f.get();
			System.out.println("RES: " + pi.getProcessData() + " " + (pi.getStdOut().length() > 0 ? "true" : "false"));
            assertTrue(pi.getStdOut().length() > 0);
            assertEquals(ProcessStatus.FINISHED, pi.getStatus());
        }
        DlabProcess.getInstance().setMaxProcessesPerBox(50);
     }

    //@Test
    public void testPingsWithDuplicates() throws Exception {

        String pingCommand;

        if(windows) {
            pingCommand = "ping -n 5 localhost";
        } else {
            pingCommand = "ping -c 5 localhost";
        }

        ArrayList<CompletableFuture<ProcessInfo>> cf = new ArrayList<>();
        for(int i = 0; i < 5; i++) {
			cf.add(DlabProcess.getInstance().start(user, "ping", ProcessType.MOCKED_PROCESS, "",
					pingCommand.split(" ")));
        }
        Thread.sleep(100);
		Collection<ProcessData> pIds = DlabProcess.getInstance().getActiveProcesses();
        System.out.println(pIds);
        Thread.sleep(5000);
        int errCount = 0;
        for (CompletableFuture<ProcessInfo> f:cf){
            try {
                ProcessInfo pi = f.get();
				System.out.println("RES: " + pi.getProcessData() + " " + (pi.getStdOut().length() > 0 ? "true" :
						"false"));
                assertTrue(pi.getStdOut().length() > 0);
                assertEquals(ProcessStatus.FINISHED, pi.getStatus());
            } catch( CancellationException e) {
                errCount++;
            }
        }
        assertEquals(4,errCount);
    }


    //@Test
    public void testPingsWithManyProcesses() throws Exception {

        String pingCommand;

        if(windows) {
            pingCommand = "ping -n 5 localhost";
        } else {
            pingCommand = "ping -c 5 localhost";
        }

		ProcessData ping = new ProcessData(user, "ping");
        ArrayList<CompletableFuture<ProcessInfo>> cf = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            for(int ii=0; ii<3; ii++) {
				ping = new ProcessData(user + i, "ping " + (i * 10 + ii));
				cf.add(DlabProcess.getInstance().start(user + i, "ping " + (i * 10 + ii),
						ProcessType.MOCKED_PROCESS, "", pingCommand.split(" ")));
            }
        }
        Thread.sleep(3000);
		Collection<ProcessData> pIds = DlabProcess.getInstance().getActiveProcesses();
        assertEquals(9, pIds.size());

        Supplier<? extends ProcessInfo> s = DlabProcess.getInstance().getProcessInfoSupplier(ping);
        assertNotNull(s);
        ProcessInfo processInfo = s.get();
        assertNotNull(processInfo);
        System.out.println(pIds);
        System.out.println(processInfo);
        for (CompletableFuture<ProcessInfo> f:cf){
            ProcessInfo pi = f.get();
			System.out.println("RES: " + pi.getProcessData() + " " + (pi.getStdOut().length() > 0 ? "true" : "false"));
            assertTrue(pi.getStdOut().length() > 0);
            assertEquals(ProcessStatus.FINISHED, pi.getStatus());
        }
    }


    //@Test
    public void testStopPing() throws Exception {

        String pingCommand;

        if(windows) {
            pingCommand = "ping -n 50 localhost";
        } else {
            pingCommand = "ping -c 50 localhost";
        }
		CompletableFuture<ProcessInfo> cf = DlabProcess.getInstance().start(user, "ping",
				ProcessType.MOCKED_PROCESS, "", pingCommand.split(" "));
        Thread.sleep(3000);
		DlabProcess.getInstance().stop(user, "ping");
        ProcessInfo pi = cf.get();
        System.out.println("STOPPED: "+pi);
        assertEquals(ProcessStatus.STOPPED, pi.getStatus());
    }

    //@Test
    public void testKillPing() throws Exception {

        String pingCommand;

        if(windows) {
            pingCommand = "ping -n 50 localhost";
        } else {
            pingCommand = "ping -c 50 localhost";
        }
		CompletableFuture<ProcessInfo> cf = DlabProcess.getInstance().start(user, "ping", ProcessType.MOCKED_PROCESS,
				"", pingCommand.split(" "));
        Thread.sleep(3000);
		DlabProcess.getInstance().kill(user, "ping");
        ProcessInfo pi = cf.get();
        System.out.println("KILLED: "+pi);
        assertEquals(ProcessStatus.KILLED, pi.getStatus());
    }

    //@Test
    public void testTimeoutPing() throws Exception {

        String pingCommand;

        if(windows) {
            pingCommand = "ping -t localhost";
        } else {
            pingCommand = "ping localhost";
        }
        DlabProcess.getInstance().setProcessTimeout(5, TimeUnit.SECONDS);
		CompletableFuture<ProcessInfo> cf = DlabProcess.getInstance().start(user, "ping", ProcessType.MOCKED_PROCESS,
				"", pingCommand.split(" "));
        ProcessInfo pi = cf.get();
        System.out.println("TIMEOUT: "+pi);
        assertEquals(ProcessStatus.TIMEOUT, pi.getStatus());
    }

}