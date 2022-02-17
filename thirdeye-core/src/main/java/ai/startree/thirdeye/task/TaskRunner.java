/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.task;

import ai.startree.thirdeye.spi.task.TaskInfo;
import java.util.List;

/**
 * Interface for task runner of various types of executors
 */
public interface TaskRunner {

  List<TaskResult> execute(TaskInfo taskInfo, TaskContext taskContext) throws Exception;
}
