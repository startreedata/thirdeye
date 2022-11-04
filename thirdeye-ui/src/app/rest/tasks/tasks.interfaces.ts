import { ActionHook } from "../actions.interfaces";
import { Task, TaskStatus, TaskType } from "../dto/taks.interface";

export interface GetTasksProps {
    status?: TaskStatus[];
    type?: TaskType[];
    startTime?: number;
    endTime?: number;
}

export interface GetTasks extends ActionHook {
    tasks: Task[] | null;
    getTasks: (params: GetTasksProps) => Promise<Task[] | undefined>;
}
