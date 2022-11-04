import React, { FunctionComponent } from "react";
import { PluralizeProps } from "./pluralize.interfaces";

export const Pluralize: FunctionComponent<PluralizeProps> = ({
    count,
    singular,
    plural,
}) => {
    if (count === 0) {
        return (
            <span>
                {count} {plural}
            </span>
        );
    }

    return count > 1 ? (
        <span>
            {count} {plural}
        </span>
    ) : (
        <span>
            {count} {singular}
        </span>
    );
};
