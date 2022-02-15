// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
export const parseBooleanV1 = (booleanString: string): boolean => {
    if (!booleanString) {
        return false;
    }

    return booleanString.trim().toLocaleLowerCase() === "true";
};
