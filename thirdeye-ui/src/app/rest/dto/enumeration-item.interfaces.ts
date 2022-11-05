import { EnumerationItemParams } from "./detection.interfaces";

export interface EnumerationItem {
    id: number;
    name: string;
    params: EnumerationItemParams;
}
