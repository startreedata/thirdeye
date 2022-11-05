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
