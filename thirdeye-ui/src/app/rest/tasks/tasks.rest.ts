import axios from "axios";
import { Task } from "../dto/taks.interface";
import { GetTasksProps } from "./tasks.interfaces";

const BASE_URL_TASKS = "/api/tasks";
const LIST_SEPARATOR = ",";

export const getTasks = async ({
    status,
    type,
    startTime,
    endTime,
}: GetTasksProps = {}): Promise<Task[]> => {
    const queryParams = new URLSearchParams();

    if (status) {
        queryParams.set("status", `[in]${status.join(LIST_SEPARATOR)}`);
    }

    if (type) {
        queryParams.set("type", `[in]${type.join(LIST_SEPARATOR)}`);
    }

    if (startTime) {
        queryParams.append("startTime", `[gte]${startTime}`);
    }

    if (endTime) {
        queryParams.append("startTime", `[lte]${endTime}`);
    }

    const response = await axios.get(
        `${BASE_URL_TASKS}?${queryParams.toString()}`
    );

    return response.data;
};
