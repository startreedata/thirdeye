import { useHTTPAction } from "../create-rest-action";
import { Task } from "../dto/taks.interface";
import { GetTasks, GetTasksProps } from "./tasks.interfaces";
import { getTasks as getTasksREST } from "./tasks.rest";

export const useGetTasks = (): GetTasks => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<Task[]>(getTasksREST);

    const getTasks = (
        getTasksParams: GetTasksProps = {}
    ): Promise<Task[] | undefined> => {
        return makeRequest(getTasksParams);
    };

    return {
        tasks: data,
        getTasks,
        status,
        errorMessages,
    };
};
