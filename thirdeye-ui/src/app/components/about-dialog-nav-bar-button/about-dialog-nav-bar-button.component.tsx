import { Grid, Typography } from "@material-ui/core";
import InfoIcon from "@material-ui/icons/Info";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { ReactComponent as StarTreeIcon } from "../../platform/assets/images/startree-icon-light.svg";
import {
    LinkV1,
    NavBarLinkIconV1,
    NavBarLinkTextV1,
    NavBarLinkV1,
} from "../../platform/components";
import { getThirdEyeUiVersion } from "../../utils/version/version.util";
import { useDialog } from "../dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../dialogs/dialog-provider/dialog-provider.interfaces";
import { useAboutDialogNavBarButtonStyles } from "./about-dialog-nav-bar-button.styles";

export const AboutDialogNavBarButton: FunctionComponent = () => {
    const aboutDialogNavBarButtonClasses = useAboutDialogNavBarButtonStyles();
    const { t } = useTranslation();
    const { hideDialog, visible, showDialog } = useDialog();

    const handleButtonClick = (): void => {
        showDialog({
            type: DialogType.CUSTOM,
            title: t("label.thirdeye"),
            children: (
                <Grid container alignItems="center">
                    {/* App logo */}
                    <Grid
                        container
                        item
                        justifyContent="center"
                        spacing={0}
                        xs={3}
                    >
                        <Grid item>
                            <StarTreeIcon
                                className={
                                    aboutDialogNavBarButtonClasses.appLogo
                                }
                            />
                        </Grid>
                    </Grid>

                    <Grid container item xs={9}>
                        {/* Remote Managed UI version */}
                        <Grid item xs={12}>
                            <Typography variant="subtitle2">
                                {t("label.thirdeye")}
                            </Typography>

                            <Typography variant="caption">
                                {getThirdEyeUiVersion()}
                            </Typography>
                        </Grid>

                        {/* Website */}
                        <Grid item xs={12}>
                            <LinkV1
                                externalLink
                                href={t("label.thirdeye-website-url")}
                                target="_blank"
                                variant="body2"
                            >
                                {t("label.thirdeye-website-url")}
                            </LinkV1>
                        </Grid>
                    </Grid>
                </Grid>
            ),
            hideCancelButton: true,
            okButtonLabel: t("label.ok"),
            onOk: hideDialog,
        });
    };

    return (
        <NavBarLinkV1 selected={visible} onClick={handleButtonClick}>
            <NavBarLinkIconV1>
                <InfoIcon />
            </NavBarLinkIconV1>

            <NavBarLinkTextV1>{t("label.about")}</NavBarLinkTextV1>
        </NavBarLinkV1>
    );
};
