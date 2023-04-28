/*
 * Copyright 2023 StarTree Inc
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
import axios from "axios";
import { InfoV1 } from "../dto/info.interfaces";

const BASE_URL_INFO_V1 = "/api/info";

export const getInfoV1 = async (): Promise<InfoV1> => {
    const response = await axios.get(BASE_URL_INFO_V1);

    return response.data;
};
