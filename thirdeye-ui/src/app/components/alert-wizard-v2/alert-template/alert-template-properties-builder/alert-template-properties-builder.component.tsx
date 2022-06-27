/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import {
    Box,
    Button,
    Divider,
    Grid,
    Link,
    TextField,
    Typography,
} from "@material-ui/core";
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
import { getAlertTemplatesUpdatePath } from "../../../../utils/routes/routes.util";
import { useAlertWizardV2Styles } from "../../alert-wizard-v2.styles";
import { setUpFieldInputRenderConfig } from "../alert-template.utils";
import {
    AlertTemplatePropertiesBuilderProps,
    PropertyRenderConfig,
} from "./alert-template-properties-builder.interfaces";

export const AlertTemplatePropertiesBuilder: FunctionComponent<
    AlertTemplatePropertiesBuilderProps
> = ({
    alertTemplateId,
    defaultTemplateProperties,
    templateProperties,
    onPropertyValueChange,
    requiredFields,
}) => {
    const { t } = useTranslation();
    const classes = useAlertWizardV2Styles();

    const [showMore, setShowMore] = useState<boolean>(false);

    const [requiredKeys, setRequiredKeys] = useState<PropertyRenderConfig[]>(
        []
    );
    const [optionalKeys, setOptionalKeys] = useState<PropertyRenderConfig[]>(
        []
    );

    useEffect(() => {
        const [requiredKeys, optionalKeys] = setUpFieldInputRenderConfig(
            requiredFields,
            templateProperties,
            defaultTemplateProperties
        );

        setRequiredKeys(requiredKeys);
        setOptionalKeys(optionalKeys);
    }, [requiredFields]);

    const handlePropertyValueChange = useCallback(
        debounce((key, value) => {
            onPropertyValueChange({
                [key]: value,
            });
        }, 100),
        [onPropertyValueChange]
    );

    return (
        <>
            <Grid container item xs={12}>
                <Grid item xs={12}>
                    <Alert
                        className={classes.infoAlert}
                        icon={<InfoIcon />}
                        severity="info"
                    >
                        {t("message.changes-added-to-template-properties")}
                        <Link
                            href={getAlertTemplatesUpdatePath(alertTemplateId)}
                            target="_blank"
                        >
                            {t("label.template-configuration").toLowerCase()}
                        </Link>
                    </Alert>
                    <Box paddingBottom={2} paddingTop={2}>
                        <Divider />
                    </Box>
                </Grid>
                <Grid item xs={6}>
                    <Box paddingBottom={1}>Property name</Box>
                </Grid>
                <Grid item xs={6}>
                    <Box paddingBottom={1}>Property value</Box>
                </Grid>
            </Grid>
            {requiredKeys.map((item, idx) => {
                return (
                    <Grid container item key={item.key} xs={12}>
                        <Grid item xs={5}>
                            <TextField
                                fullWidth
                                inputProps={{ tabIndex: -1 }}
                                value={item.key}
                            />
                        </Grid>
                        <Grid item xs={1} />
                        <Grid item xs={5}>
                            <TextField
                                fullWidth
                                data-testid={`textfield-${item.key}`}
                                defaultValue={item.value}
                                inputProps={{ tabIndex: idx + 1 }}
                                placeholder={t("label.add-property-value")}
                                onChange={(e) => {
                                    handlePropertyValueChange(
                                        item.key,
                                        e.currentTarget.value
                                    );
                                }}
                            />
                        </Grid>
                        <Grid item xs={1} />
                        <Grid item xs={12}>
                            <Divider />
                        </Grid>
                    </Grid>
                );
            })}
            {!showMore && optionalKeys.length > 0 && (
                <Grid item xs={12}>
                    <Box marginBottom={3}>
                        <Button
                            color="primary"
                            data-testid="show-more-btn"
                            variant="text"
                            onClick={() => setShowMore(true)}
                        >
                            {t("label.show-count-more", {
                                count: optionalKeys.length,
                            })}
                        </Button>
                    </Box>
                </Grid>
            )}
            {showMore && (
                <>
                    <Grid item xs={12}>
                        <Typography variant="h6">
                            {t("label.fields-with-default-values")}
                        </Typography>
                        <Typography variant="body2">
                            {t("message.enter-value-to-override-defaults")}
                        </Typography>
                    </Grid>
                    {optionalKeys.map((item, idx) => {
                        return (
                            <Grid container item key={item.key} xs={12}>
                                <Grid item xs={5}>
                                    <TextField
                                        fullWidth
                                        inputProps={{ tabIndex: -1 }}
                                        value={item.key}
                                    />
                                </Grid>
                                <Grid item xs={1} />
                                <Grid item xs={5}>
                                    <TextField
                                        fullWidth
                                        data-testid={`textfield-${item.key}`}
                                        defaultValue={item.value}
                                        inputProps={{
                                            tabIndex:
                                                requiredKeys.length + idx + 1,
                                        }}
                                        placeholder={item.defaultValue}
                                        onChange={(e) =>
                                            handlePropertyValueChange(
                                                item.key,
                                                e.currentTarget.value
                                            )
                                        }
                                    />
                                </Grid>
                                <Grid item xs={1} />
                                <Grid item xs={12}>
                                    <Divider />
                                </Grid>
                            </Grid>
                        );
                    })}
                </>
            )}
            {showMore && optionalKeys.length > 0 && (
                <Grid item xs={12}>
                    <Box marginBottom={3}>
                        <Button
                            color="primary"
                            data-testid="hide-more-btn"
                            variant="text"
                            onClick={() => setShowMore(false)}
                        >
                            {t("label.hide-count-optional-properties", {
                                count: optionalKeys.length,
                            })}
                        </Button>
                    </Box>
                </Grid>
            )}
        </>
    );
};
