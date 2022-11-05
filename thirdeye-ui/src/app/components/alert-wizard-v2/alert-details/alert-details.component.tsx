import {
    Box,
    FormHelperText,
    Grid,
    InputLabel,
    TextField,
    Typography,
} from "@material-ui/core";
import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { PageContentsCardV1 } from "../../../platform/components";
import { InputSection } from "../../form-basics/input-section/input-section.component";
import { useAlertWizardV2Styles } from "../alert-wizard-v2.styles";
import { AlertDetailsProps } from "./alert-details.interfaces";
import { AlertFrequency } from "./alert-frequency/alert-frequency.component";

function AlertDetails({
    alert,
    onAlertPropertyChange,
}: AlertDetailsProps): JSX.Element {
    const [name, setName] = useState(alert.name);
    const [description, setDescription] = useState(alert.description);
    const [nameHasError, setNameHasError] = useState(false);
    const { t } = useTranslation();
    const classes = useAlertWizardV2Styles();

    const handleNameChange = (
        e: React.ChangeEvent<HTMLTextAreaElement | HTMLInputElement>
    ): void => {
        const newValue = e.currentTarget.value;

        setName(newValue);

        if (newValue && newValue.length > 0) {
            setNameHasError(false);
            onAlertPropertyChange({
                name: newValue,
            });
        } else {
            setNameHasError(true);
        }
    };

    return (
        <PageContentsCardV1>
            <Grid container>
                <Grid item xs={12}>
                    <Box marginBottom={2}>
                        <Typography variant="h5">
                            {t("label.alert-details")}
                        </Typography>
                    </Box>
                </Grid>

                <InputSection
                    inputComponent={
                        <>
                            <TextField
                                fullWidth
                                data-testid="name-input-container"
                                error={nameHasError}
                                value={name}
                                variant="outlined"
                                onChange={handleNameChange}
                            />
                            {nameHasError && (
                                <FormHelperText error>
                                    {t("message.please-enter-valid-name")}
                                </FormHelperText>
                            )}
                        </>
                    }
                    labelComponent={
                        <InputLabel
                            shrink
                            className={classes.label}
                            data-testid="name-input-label"
                            error={nameHasError}
                        >
                            {t("label.name-of-your-alert")}
                        </InputLabel>
                    }
                />

                <InputSection
                    helperLabel={t("label.optional")}
                    inputComponent={
                        <TextField
                            fullWidth
                            multiline
                            minRows={6}
                            value={description}
                            variant="outlined"
                            onChange={(
                                e: React.ChangeEvent<
                                    HTMLTextAreaElement | HTMLInputElement
                                >
                            ) => {
                                setDescription(e.currentTarget.value);
                                onAlertPropertyChange({
                                    description: e.currentTarget.value,
                                });
                            }}
                        />
                    }
                    label={t("label.description")}
                />

                <AlertFrequency
                    alert={alert}
                    onAlertPropertyChange={onAlertPropertyChange}
                />
            </Grid>
        </PageContentsCardV1>
    );
}

export { AlertDetails };
