import { alpha, createStyles, makeStyles, Theme } from "@material-ui/core";
import { BorderV1 } from "../../platform/utils";
import { PaletteV1 } from "../../platform/utils/material-ui/palette.util";

export const useAutoCompleteStyles = makeStyles((theme: Theme) =>
    createStyles({
        popper: {
            borderRadius: 3,
            width: 300,
            zIndex: 1,
            fontSize: 13,
            boxShadow: `0 3px 12px ${PaletteV1.BorderColorDefault}`,
            border: BorderV1.BorderDefault,
            backgroundColor: theme.palette.background.default,
        },
        inputBase: {
            padding: 10,
            backgroundColor: theme.palette.common.white,
            "& input": {
                borderRadius: 4,
                backgroundColor: theme.palette.common.white,
                padding: 8,
                transition: theme.transitions.create([
                    "border-color",
                    "box-shadow",
                ]),
                border: BorderV1.BorderDefault,
                fontSize: 14,
                "&:focus": {
                    boxShadow: `${alpha(
                        theme.palette.primary.main,
                        0.25
                    )} 0 0 0 0.2rem`,
                    borderColor: theme.palette.primary.main,
                },
            },
        },
        paper: {
            boxShadow: "none",
            margin: 0,
            color: theme.palette.background.default,
            fontSize: 13,
        },
        popperDisablePortal: {
            position: "relative",
        },
        chipIcon: (props: { open: boolean }) => ({
            transform: props.open ? "rotateX(180deg)" : "rotateX(0deg)",
        }),
        sizeSmall: {
            height: "18px",
            width: "18px",
        },
    })
);
