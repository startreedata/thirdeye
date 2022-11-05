/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
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
