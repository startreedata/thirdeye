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
import { useHTTPAction } from "../create-rest-action";
import { Dataset } from "../dto/dataset.interfaces";
import { GetDataset } from "./dataset.interfaces";
import { getDataset as getDatasetREST } from "./datasets.rest";

export const useGetDataset = (): GetDataset => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<Dataset>(getDatasetREST);

    const getDataset = (id: number): Promise<Dataset | undefined> => {
        return makeRequest(id);
    };

    return { dataset: data, getDataset, status, errorMessages };
};
