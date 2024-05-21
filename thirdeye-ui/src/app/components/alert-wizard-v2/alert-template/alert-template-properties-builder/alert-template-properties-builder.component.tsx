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
import {
    Box,
    Button,
    Divider,
    Grid,
    Link,
    Typography,
} from "@material-ui/core";
import ExpandLess from "@material-ui/icons/ExpandLess";
import ExpandMore from "@material-ui/icons/ExpandMore";
import InfoIcon from "@material-ui/icons/Info";
import Alert from "@material-ui/lab/Alert";
import { debounce } from "lodash";
import React, {
    FunctionComponent,
    useCallback,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { groupPropertyByStepAndSubStep } from "../../../../utils/alerts/alerts.util";
import { getAlertTemplatesUpdatePath } from "../../../../utils/routes/routes.util";
import { useAlertWizardV2Styles } from "../../alert-wizard-v2.styles";
import { setUpFieldInputRenderConfig } from "../alert-template.utils";
import {
    AlertTemplatePropertiesBuilderProps,
    PropertyRenderConfig,
} from "./alert-template-properties-builder.interfaces";
import { AlertTemplateSectionedProperties } from "./alert-template-sectioned-properties/alert-template-sectioned-properties.component";

export const AlertTemplatePropertiesBuilder: FunctionComponent<AlertTemplatePropertiesBuilderProps> =
    ({
        alertTemplateId,
        templateProperties,
        onPropertyValueChange,
        availableFields,
    }) => {
        const { t } = useTranslation();
        const classes = useAlertWizardV2Styles();

        const [showMore, setShowMore] = useState<boolean>(false);

        const [requiredKeys, setRequiredKeys] = useState<
            PropertyRenderConfig[]
        >([]);
        const [optionalKeys, setOptionalKeys] = useState<
            PropertyRenderConfig[]
        >([]);

        useEffect(() => {
            const [requiredKeys, optionalKeys] = setUpFieldInputRenderConfig(
                availableFields,
                templateProperties
            );

            setRequiredKeys(requiredKeys);
            setOptionalKeys(optionalKeys);
        }, [availableFields]);

        const handlePropertyValueChange = useCallback(
            debounce((key, value) => {
                onPropertyValueChange({
                    [key]: value,
                });
            }, 100),
            [onPropertyValueChange]
        );

        const groupedRequiredKeys = groupPropertyByStepAndSubStep(requiredKeys);
        const groupedOptionalKeys = groupPropertyByStepAndSubStep(optionalKeys);

        return (
            <>
                <Grid item xs={12}>
                    <Box paddingBottom={2} paddingTop={2}>
                        <Alert
                            className={classes.infoAlert}
                            icon={<InfoIcon />}
                            severity="info"
                        >
                            {t("message.changes-added-to-template-properties")}
                            <Link
                                href={getAlertTemplatesUpdatePath(
                                    alertTemplateId
                                )}
                                target="_blank"
                            >
                                {t(
                                    "label.template-configuration"
                                ).toLowerCase()}
                            </Link>
                        </Alert>
                    </Box>
                </Grid>
                <Grid item xs={12}>
                    <Box paddingBottom={2}>
                        <Typography variant="h6">
                            {t("label.template-properties")}
                        </Typography>
                        <Typography variant="body2">
                            {t("message.setup-properties-based-on-your-needs")}
                        </Typography>
                    </Box>
                </Grid>
                <AlertTemplateSectionedProperties
                    groupedProperties={groupedRequiredKeys}
                    handlePropertyValueChange={handlePropertyValueChange}
                />
                {!showMore && optionalKeys.length > 0 && (
                    <>
                        <Grid item xs={12}>
                            <Divider />
                        </Grid>
                        <Grid item xs={12}>
                            <Button
                                color="primary"
                                data-testid="show-more-btn"
                                variant="text"
                                onClick={() => setShowMore(true)}
                            >
                                {t("label.show-count-optional-properties", {
                                    count: optionalKeys.length,
                                })}
                                <ExpandMore />
                            </Button>
                        </Grid>
                        <Grid item xs={12}>
                            <Divider />
                        </Grid>
                    </>
                )}
                {showMore && (
                    <>
                        <Grid item xs={12}>
                            <Divider />
                        </Grid>
                        <Grid item xs={12}>
                            <Box paddingBottom={2} paddingTop={3}>
                                <Typography variant="body2">
                                    {t(
                                        "message.enter-value-to-override-defaults"
                                    )}
                                </Typography>
                            </Box>
                        </Grid>
                        <AlertTemplateSectionedProperties
                            groupedProperties={groupedOptionalKeys}
                            handlePropertyValueChange={
                                handlePropertyValueChange
                            }
                        />
                    </>
                )}
                {showMore && optionalKeys.length > 0 && (
                    <>
                        <Grid item xs={12}>
                            <Button
                                color="primary"
                                data-testid="hide-more-btn"
                                variant="text"
                                onClick={() => setShowMore(false)}
                            >
                                {t("label.hide-count-optional-properties", {
                                    count: optionalKeys.length,
                                })}
                                <ExpandLess />
                            </Button>
                        </Grid>
                        <Grid item xs={12}>
                            <Divider />
                        </Grid>
                    </>
                )}
            </>
        );
    };
