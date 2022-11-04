// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { useHTTPAction } from "../create-rest-action";
import { EnumerationItem } from "../dto/enumeration-item.interfaces";
import {
    GetEnumerationItem,
    GetEnumerationItems,
    GetEnumerationItemsProps,
} from "./enumeration-items.interfaces";
import {
    getEnumerationItem as getEnumerationItemREST,
    getEnumerationItems as getEnumerationItemsREST,
} from "./enumeration-items.rest";

export const useGetEnumerationItems = (): GetEnumerationItems => {
    const { data, makeRequest, status, errorMessages } = useHTTPAction<
        EnumerationItem[]
    >(getEnumerationItemsREST);

    const getEnumerationItems = (
        getEnumerationItemsParams: GetEnumerationItemsProps = {}
    ): Promise<EnumerationItem[] | undefined> => {
        return makeRequest(getEnumerationItemsParams);
    };

    return {
        enumerationItems: data,
        getEnumerationItems,
        status,
        errorMessages,
    };
};

export const useGetEnumerationItem = (): GetEnumerationItem => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<EnumerationItem>(getEnumerationItemREST);

    const getEnumerationItem = (
        id: number
    ): Promise<EnumerationItem | undefined> => {
        return makeRequest(id);
    };

    return {
        enumerationItem: data,
        getEnumerationItem,
        status,
        errorMessages,
    };
};
