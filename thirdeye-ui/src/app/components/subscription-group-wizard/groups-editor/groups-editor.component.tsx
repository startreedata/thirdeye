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
import { Icon } from "@iconify/react";
import {
    Box,
    Button,
    Card,
    CardActionArea,
    CardContent,
    Divider,
    Grid,
} from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    NotificationSpec,
    SpecType,
} from "../../../rest/dto/subscription-group.interfaces";
import { Email } from "./email/email.component";
import { GroupsEditorProps } from "./groups-editor.interfaces";
import { availableSpecTypes, specTypeToUIConfig } from "./groups-editor.utils";

export const GroupsEditor: FunctionComponent<GroupsEditorProps> = ({
    subscriptionGroup,
    onSubscriptionGroupEmailsChange,
    onSpecsChange,
}) => {
    const [currentSpecs, setCurrentSpecs] = useState<NotificationSpec[]>(
        subscriptionGroup.specs || []
    );
    const { t } = useTranslation();

    useEffect(() => {
        onSpecsChange(currentSpecs);
    }, [currentSpecs]);

    const hasSomeConfig =
        (subscriptionGroup.notificationSchemes.email &&
            subscriptionGroup.notificationSchemes.email.to.length > 0) ||
        currentSpecs.length > 0;

    const handleShortcutCreateOnclick = (id: number | string): void => {
        switch (id) {
            case SpecType.EmailSendgrid:
                setCurrentSpecs((specs) => {
                    return [
                        ...specs,
                        {
                            type: SpecType.EmailSendgrid,
                            params: {
                                apiKey: "${SENDGRID_API_KEY}",
                                emailRecipients: {
                                    from: "thirdeye-alerts@startree.ai",
                                    to: [],
                                },
                            },
                        },
                    ];
                });

                break;
            case SpecType.Slack:
                setCurrentSpecs((specs) => {
                    return [
                        ...specs,
                        {
                            type: SpecType.Slack,
                            params: {
                                webhookUrl: "",
                            },
                        },
                    ];
                });

                break;

            case SpecType.Webhook:
                setCurrentSpecs((specs) => {
                    return [
                        ...specs,
                        {
                            type: SpecType.Webhook,
                            params: {
                                url: "",
                            },
                        },
                    ];
                });

                break;
            default:
                break;
        }
    };

    const handleSpecChange = (
        updatedSpec: NotificationSpec,
        idx: number
    ): void => {
        setCurrentSpecs((current) => {
            return current.map((spec, specIdx) => {
                if (specIdx === idx) {
                    return {
                        ...spec,
                        ...updatedSpec,
                    };
                }

                return spec;
            });
        });
    };

    const handleSpecDelete = (idx: number): void => {
        setCurrentSpecs((current) => {
            return current.filter((_, specIdx) => {
                return specIdx !== idx;
            });
        });
    };

    const handleEmailDelete = (): void => {
        onSubscriptionGroupEmailsChange([]);
    };

    return (
        <>
            {!hasSomeConfig && (
                <Grid container>
                    <Grid item xs={12}>
                        <Box textAlign="center">
                            {t("message.to-get-started-select-type")}
                        </Box>
                    </Grid>
                    {availableSpecTypes.map((item) => (
                        <Grid item key={item.id} sm={4} xs={12}>
                            <Card>
                                <CardActionArea
                                    onClick={() =>
                                        handleShortcutCreateOnclick(item.id)
                                    }
                                >
                                    <CardContent>
                                        <Box color="primary" textAlign="center">
                                            <Icon
                                                height={48}
                                                icon={item.icon}
                                            />
                                        </Box>

                                        <Box color="primary" textAlign="center">
                                            {t(item.internationalizationString)}
                                        </Box>
                                    </CardContent>
                                </CardActionArea>
                            </Card>
                        </Grid>
                    ))}
                </Grid>
            )}
            {/** Legacy Email Configuration */}
            {hasSomeConfig &&
                subscriptionGroup.notificationSchemes.email.to.length > 0 && (
                    <Box marginBottom={3}>
                        <Email
                            subscriptionGroup={subscriptionGroup}
                            onDeleteClick={handleEmailDelete}
                            onSubscriptionGroupEmailsChange={
                                onSubscriptionGroupEmailsChange
                            }
                        />
                    </Box>
                )}

            {hasSomeConfig &&
                currentSpecs.map((spec: NotificationSpec, idx: number) => {
                    if (specTypeToUIConfig[spec.type]) {
                        return (
                            <Box marginBottom={3}>
                                {React.createElement(
                                    specTypeToUIConfig[spec.type].formComponent,
                                    {
                                        configuration: spec,
                                        onDeleteClick: () =>
                                            handleSpecDelete(idx),
                                        onSpecChange: (
                                            updatedSpec: NotificationSpec
                                        ) => handleSpecChange(updatedSpec, idx),
                                    }
                                )}
                            </Box>
                        );
                    }

                    return null;
                })}

            {hasSomeConfig && (
                <>
                    <Box
                        marginTop={3}
                        paddingBottom={3}
                        paddingTop={3}
                        textAlign="center"
                    >
                        <Divider />
                    </Box>
                    <Box textAlign="center">
                        <span>{t("label.add")}: </span>
                        {availableSpecTypes.map((item) => (
                            <Button
                                color="primary"
                                key={item.id}
                                size="small"
                                startIcon={
                                    <Icon height={12} icon={item.icon} />
                                }
                                onClick={() =>
                                    handleShortcutCreateOnclick(item.id)
                                }
                            >
                                {t(item.internationalizationString)}
                            </Button>
                        ))}
                    </Box>
                </>
            )}
        </>
    );
};
