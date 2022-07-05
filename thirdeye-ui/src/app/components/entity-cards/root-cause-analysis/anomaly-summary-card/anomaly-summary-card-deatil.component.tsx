import { Typography } from "@material-ui/core";
import React, { FC } from "react";

interface Props {
    label: string;
    value: string;
    valueClassName?: string;
}

export const AnomalySummaryCardDetail: FC<Props> = ({
    label,
    value,
    valueClassName,
}: Props) => {
    return (
        <>
            <Typography className={valueClassName} variant="subtitle1">
                {value}
            </Typography>
            <Typography color="textSecondary" variant="body2">
                {label}
            </Typography>
        </>
    );
};
