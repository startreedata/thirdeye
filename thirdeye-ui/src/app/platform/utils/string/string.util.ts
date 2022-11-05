export const parseBooleanV1 = (booleanString: string): boolean => {
    if (!booleanString) {
        return false;
    }

    return booleanString.trim().toLocaleLowerCase() === "true";
};
