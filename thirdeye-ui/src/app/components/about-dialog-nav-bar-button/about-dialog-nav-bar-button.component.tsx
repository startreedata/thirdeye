/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
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
    useDialogProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { getThirdEyeUiVersion } from "../../utils/version/version.util";
import { useAboutDialogNavBarButtonStyles } from "./about-dialog-nav-bar-button.styles";

export const AboutDialogNavBarButton: FunctionComponent = () => {
    const aboutDialogNavBarButtonClasses = useAboutDialogNavBarButtonStyles();
    const { t } = useTranslation();
    const { showDialog, hideDialog, visible } = useDialogProviderV1();

    const handleButtonClick = (): void => {
        showDialog({
            type: DialogType.CUSTOM,
            headerText: t("label.thirdeye"),
            contents: (
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
                        {/* ThirdEye UI version */}
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
            okButtonText: t("label.ok"),
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
