/*
 * Copyright 2022 StarTree Inc
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
import { EnumerationItem } from "../dto/enumeration-item.interfaces";
import { GetEnumerationItemsProps } from "./enumeration-items.interfaces";

const BASE_URL_ENUMERATION_ITEM = "/api/enumeration-items";

export const getEnumerationItems = async ({
    ids,
}: GetEnumerationItemsProps = {}): Promise<EnumerationItem[]> => {
    const queryParams = new URLSearchParams([]);
    let url = BASE_URL_ENUMERATION_ITEM;

    if (ids) {
        queryParams.set("id", `[in]${ids.join(",")}`);
    }

    if (queryParams.toString()) {
        url += `?${queryParams.toString()}`;
    }

    const response = await axios.get(url);

    return response.data;
};

export const getEnumerationItem = async (
    id: number
): Promise<EnumerationItem> => {
    const response = await axios.get(`${BASE_URL_ENUMERATION_ITEM}/${id}`);

    return response.data;
};
