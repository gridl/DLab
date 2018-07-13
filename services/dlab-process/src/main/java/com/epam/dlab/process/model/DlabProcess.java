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
package com.epam.dlab.process.model;

import com.epam.dlab.process.ProcessConveyor;
import com.epam.dlab.process.builder.ProcessInfoBuilder;
import com.epam.dlab.process.exception.DlabProcessException;
import com.epam.dlab.util.SecurityUtils;
import io.dropwizard.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class DlabProcess {

	private static final DlabProcess INSTANCE = new DlabProcess();

	private ExecutorService executorService = Executors.newFixedThreadPool(50 * 3);
	private Map<String, ExecutorService> perUserService = new ConcurrentHashMap<>();
	private int userMaxparallelism = 5;
	private long expirationTime = TimeUnit.HOURS.toMillis(3);

	public static DlabProcess getInstance() {
		return INSTANCE;
	}

	private final ProcessConveyor processConveyor;

	private DlabProcess() {
		this.processConveyor = new ProcessConveyor();
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public void setMaxProcessesPerBox(int parallelism) {
		this.executorService.shutdown();
		this.executorService = Executors.newFixedThreadPool(3 * parallelism);
	}

	public void setMaxProcessesPerUser(int parallelism) {
		this.userMaxparallelism = parallelism;
		this.perUserService.forEach((k, e) -> e.shutdown());
		this.perUserService = new ConcurrentHashMap<>();
	}

	public ExecutorService getUsersExecutorService(String user) {
		perUserService.putIfAbsent(user, Executors.newFixedThreadPool(userMaxparallelism));
		return perUserService.get(user);
	}

	public CompletableFuture<ProcessInfo> start(String username, String uuid, ProcessType processType,
												String processDescription, String... command) {
		return start(new ProcessData(username, uuid, processType, processDescription), command);
	}

	private CompletableFuture<ProcessInfo> start(ProcessData processData, String... command) {
		log.debug("Run OS command for user {} with UUID {}: {}", processData.getUser(), processData.getUuid(),
				SecurityUtils.hideCreds(command));
		CompletableFuture<ProcessInfo> future =
				processConveyor.createBuildFuture(processData, () -> new ProcessInfoBuilder(processData,
						expirationTime));
		processConveyor.add(processData, future, ProcessStep.FUTURE);
		processConveyor.add(processData, command, ProcessStep.START);
		return future;
	}

	public CompletableFuture<Boolean> stop(String username, String uuid) {
		return stop(new ProcessData(username, uuid));
	}

	private CompletableFuture<Boolean> stop(ProcessData processData) {
		return processConveyor.add(processData, "STOP", ProcessStep.STOP);
	}

	public ProcessConveyor getProcessConveyor() {
		return processConveyor;
	}

	public CompletableFuture<Boolean> cancel(String username, String uuid) {
		ProcessData processData = new ProcessData(username, uuid);
		return processConveyor.add(processData, "CANCEL", ProcessStep.CANCEL);
	}

	public CompletableFuture<Boolean> kill(String username, String uuid) {
		return kill(new ProcessData(username, uuid));
	}

	private CompletableFuture<Boolean> kill(ProcessData processData) {
		return processConveyor.add(processData, "KILL", ProcessStep.KILL);
	}

	public CompletableFuture<Boolean> failed(ProcessData processData) {
		return processConveyor.add(processData, "FAILED", ProcessStep.FAILED);
	}

	public CompletableFuture<Boolean> finish(ProcessData processData, Integer exitStatus) {
		return processConveyor.add(processData, exitStatus, ProcessStep.FINISH);
	}

	public CompletableFuture<Boolean> toStdOut(ProcessData processData, String msg) {
		return processConveyor.add(processData, msg, ProcessStep.STD_OUT);
	}

	public CompletableFuture<Boolean> toStdErr(ProcessData processData, String msg) {
		return processConveyor.add(processData, msg, ProcessStep.STD_ERR);
	}

	public CompletableFuture<Boolean> toStdErr(ProcessData processData, String msg, Exception err) {
		StringWriter sw = new StringWriter();
		sw.append(msg);
		sw.append("\n");
		PrintWriter pw = new PrintWriter(sw);
		err.printStackTrace(pw);
		return processConveyor.add(processData, sw.toString(), ProcessStep.STD_ERR);
	}

	public Collection<ProcessData> getActiveProcesses() {
		Collection<ProcessData> pList = new ArrayList<>();
		processConveyor.forEachKeyAndBuilder((k, b) -> pList.add(k));
		return pList;
	}

	private Collection<ProcessData> getActiveProcesses(String username) {
		return getActiveProcesses().stream().filter(processData -> processData.getUser().equals(username))
				.collect(Collectors.toList());
	}

	public List<ProcessInfo> getProcessInfoData(String username) {
		return getActiveProcesses(username).stream()
				.map(pd -> getProcessInfoSupplier(pd).get()).collect(Collectors.toList());
	}

	public Supplier<? extends ProcessInfo> getProcessInfoSupplier(ProcessData processData) {
		return processConveyor.getInfoSupplier(processData);
	}

	private Supplier<? extends ProcessInfo> getProcessInfoSupplier(String username, String uuid) {
		return getProcessInfoSupplier(new ProcessData(username, uuid));
	}

	public void setProcessTimeout(long time, TimeUnit unit) {
		this.expirationTime = unit.toMillis(time);
	}

	public void setProcessTimeout(Duration duration) {
		this.expirationTime = duration.toMilliseconds();
	}

	public ProcessStatus getProcessStatus(String username, String uuid) {
		return Optional.ofNullable(getProcessInfoSupplier(username, uuid).get())
				.map(ProcessInfo::getStatus).orElseThrow(() ->
						new DlabProcessException("Active process with id " + uuid + " for user " + username + " not " +
								"found."));
	}

}
