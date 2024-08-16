/*
 * Copyright 2024 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { UseQueryResult, useQuery } from "@tanstack/react-query";
import { useAuthV1 } from "../../platform/stores/auth-v1/auth-v1.store";
import { QueryConfig } from "./fetch-query.interfaces";

export function useFetchQuery<T, E>({
    enabled = true,
    queryKey,
    queryFn,
}: QueryConfig<T>): UseQueryResult<T, E> {
    const { workspace } = useAuthV1();
    if (workspace.id) {
        queryKey.push(workspace.id);
    }

    return useQuery({
        enabled,
        queryKey,
        queryFn,
    });
}
