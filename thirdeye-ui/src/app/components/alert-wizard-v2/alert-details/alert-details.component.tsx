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
import { Alert, EditableAlert } from "../../../rest/dto/alert.interfaces";
import { useAlertWizardV2Styles } from "../alert-wizard-v2.styles";
import { AlertDetailsProps } from "./alert-details.interfaces";
import { AlertFrequency } from "./alert-frequency/alert-frequency.component";

function AlertDetails<NewOrExistingAlert extends EditableAlert | Alert>({
    alert,
    onAlertPropertyChange,
}: AlertDetailsProps<NewOrExistingAlert>): JSX.Element {
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
                        <Typography variant="h6">
                            {t("label.alert-details")}
                        </Typography>
                    </Box>
                </Grid>

                <Grid container item xs={12}>
                    <Grid item lg={2} md={4} sm={12} xs={12}>
                        <InputLabel
                            shrink
                            className={classes.label}
                            data-testid="name-input-label"
                            error={nameHasError}
                        >
                            {t("label.name-of-your-alert")}
                        </InputLabel>
                    </Grid>
                    <Grid item lg={3} md={5} sm={12} xs={12}>
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
                    </Grid>
                </Grid>

                <Grid container item xs={12}>
                    <Grid item lg={2} md={4} sm={12} xs={12}>
                        <Typography variant="body2">
                            {t("label.description")}
                        </Typography>

                        <Typography variant="caption">
                            ({t("label.optional")})
                        </Typography>
                    </Grid>
                    <Grid item lg={3} md={5} sm={12} xs={12}>
                        <TextField
                            fullWidth
                            multiline
                            rows={6}
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
                    </Grid>
                </Grid>

                <AlertFrequency<NewOrExistingAlert>
                    alert={alert}
                    onAlertPropertyChange={onAlertPropertyChange}
                />
            </Grid>
        </PageContentsCardV1>
    );
}

export { AlertDetails };
