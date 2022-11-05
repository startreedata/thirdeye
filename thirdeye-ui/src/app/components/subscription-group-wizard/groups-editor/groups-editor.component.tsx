import { Icon } from "@iconify/react";
import { Box, Button, Divider, Grid, Typography } from "@material-ui/core";
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
    const [legacyEmailList, setLegacyEmailList] = useState<string[]>(
        subscriptionGroup.notificationSchemes.email.to || []
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
        setLegacyEmailList([]);
        onSubscriptionGroupEmailsChange([]);
    };

    return (
        <>
            {/** Legacy Email Configuration */}
            {hasSomeConfig && legacyEmailList.length > 0 && (
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
                            <Box paddingBottom={1} paddingTop={1}>
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
                                <Divider />
                            </Box>
                        );
                    }

                    return null;
                })}

            <Grid container>
                {hasSomeConfig && (
                    <Grid item xs={12}>
                        <Box paddingBottom={1} paddingTop={1}>
                            <Typography variant="h5">
                                {t("label.add-more-channels")}
                            </Typography>
                        </Box>
                    </Grid>
                )}
                {availableSpecTypes.map((item) => (
                    <Grid item key={item.id}>
                        <Button
                            variant="outlined"
                            onClick={() => handleShortcutCreateOnclick(item.id)}
                        >
                            <Box display="block" minWidth={100}>
                                <Box color="primary" textAlign="center">
                                    <Icon height={48} icon={item.icon} />
                                </Box>

                                <Box
                                    color="primary"
                                    marginTop={2}
                                    textAlign="center"
                                >
                                    {t(item.internationalizationString)}
                                </Box>
                            </Box>
                        </Button>
                    </Grid>
                ))}
            </Grid>
        </>
    );
};
