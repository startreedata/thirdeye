import { toNumber } from "lodash";

export const isValidNumberId = (param: string): boolean => {
    if (!param) {
        return false;
    }

    const numberId = toNumber(param);

    return !isNaN(numberId) && numberId >= 0;
};
