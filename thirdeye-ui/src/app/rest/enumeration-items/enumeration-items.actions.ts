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
