// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { makeStyles } from "@material-ui/core";

export const useNavBarLinkTextV1Styles = makeStyles((theme) => ({
    navBarLinkTextRegular: {
        color: theme.palette.secondary.main,
    },
    navBarLinkTextHover: {
        color: theme.palette.primary.contrastText,
    },
}));
