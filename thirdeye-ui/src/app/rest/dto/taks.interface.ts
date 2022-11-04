export enum TaskStatus {
    COMPLETE = "COMPLETE",
    FAILED = "FAILED",
    WAITING = "WAITING",
    TIMEOUT = "TIMEOUT",
}

export enum TaskType {
    DETECTION = "DETECTION",
    MONITOR = "MONITOR",
    ONBOARDING = "ONBOARDING",
    NOTIFICATION = "NOTIFICATION",
}

export interface Task {
    id: number;
    created: number;
    updated: number;
    taskType: TaskType;
    workerId: number;
    job: {
        jobName: string;
        scheduleStartTime: number;
        scheduleEndTime: number;
        windowStartTime: number;
        windowEndTime: number;
        configId: number;
    };
    status: TaskStatus;
    startTime: number;
    endTime: number;
    taskInfo: string;
    message: string;
    lastActive: number;
}
