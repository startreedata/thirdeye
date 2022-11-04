import { ActionHook } from "../actions.interfaces";
import { EnumerationItem } from "../dto/enumeration-item.interfaces";

export interface GetEnumerationItems extends ActionHook {
    enumerationItems: EnumerationItem[] | null;
    getEnumerationItems: (
        getEnumerationItemsParams?: GetEnumerationItemsProps
    ) => Promise<EnumerationItem[] | undefined>;
}
export interface GetEnumerationItem extends ActionHook {
    enumerationItem: EnumerationItem | null;
    getEnumerationItem: (id: number) => Promise<EnumerationItem | undefined>;
}

export interface GetEnumerationItemsProps {
    ids?: number[];
}
