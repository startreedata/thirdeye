import { yupResolver } from "@hookform/resolvers/yup";
import { Box, Button, Grid, useTheme } from "@material-ui/core";
import { lowerCase } from "lodash";
import { default as React, FunctionComponent, useEffect } from "react";
import { FormProvider, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import * as yup from "yup";
import { ReactComponent as AlertIcon } from "../../../assets/images/alert.svg";
import { ReactComponent as AnomalyIcon } from "../../../assets/images/anomaly.svg";
import { ReactComponent as ConfigurationIcon } from "../../../assets/images/configuration.svg";
import { ReactComponent as MetricIcon } from "../../../assets/images/metric.svg";
import { ReactComponent as SubscriptionGroupIcon } from "../../../assets/images/subscription-group.svg";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { ButtonTile } from "../../components/button-tile/button-tile.component";
import { MultiFieldDropdown } from "../../components/multi-field-dropdown/multi-field-dropdown.component";
import { MultiFieldValueOption } from "../../components/multi-field-dropdown/multi-value-dropdown.interface";
import { PageContents } from "../../components/page-contents/page-contents.component";
import {
    getAlertsPath,
    getAnomaliesPath,
    getConfigurationPath,
    getMetricsPath,
    getSubscriptionGroupsPath,
} from "../../utils/routes/routes.util";
import { useHomePageStyles } from "./home-page.styles";

export const HomePage: FunctionComponent = () => {
    const homePageClasses = useHomePageStyles();
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const theme = useTheme();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    const handleAlertsClick = (): void => {
        history.push(getAlertsPath());
    };

    const handleAnomaliesClick = (): void => {
        history.push(getAnomaliesPath());
    };

    const handleConfigurationClick = (): void => {
        history.push(getConfigurationPath());
    };

    const handleSubscriptionGroupsClick = (): void => {
        history.push(getSubscriptionGroupsPath());
    };

    const handleMetricsClick = (): void => {
        history.push(getMetricsPath());
    };
    const schema = yup.object().shape({
        recurrence: yup.string().required(),
        month: yup.string().when("recurrence", {
            is: "FREQUENCY_EVERY_YEAR",
            then: yup.string().required(),
        }),
        dayOfMonth: yup.number().when("recurrence", {
            is: "FREQUENCY_EVERY_YEAR",
            then: yup
                .number()
                .typeError("Should be between 0-31")
                .required("Should be between 0-31")
                .min(1, "Should be between 0-31")
                .max(31, "Should be between 0-31"),
        }),
        dayOfWeek: yup.string().when("recurrence", {
            is: "FREQUENCY_EVERY_WEEK",
            then: yup.string().required(),
        }),
        hour: yup
            .number()
            .typeError("Should be between 0-23")
            .min(0, "Should be between 0-23")
            .max(23, "Should be between 0-23"),
        minute: yup
            .number()
            .typeError("Should be between 0-59")
            .min(0, "Should be between 0-59")
            .max(59, "Should be between 0-59"),
    });
    const methods = useForm({
        resolver: yupResolver(schema),
        defaultValues: {
            recurrence: "",
            month: "",
            dayOfWeek: "",
        },
    });
    const onSubmit = (data: { [x: string]: string }): void => console.log(data);

    return (
        <PageContents centered hideAppBreadcrumbs title={t("label.home")}>
            <FormProvider {...methods}>
                <form onSubmit={methods.handleSubmit(onSubmit)}>
                    <Grid container alignItems="center" justify="center">
                        <Grid item xs={12}>
                            <MultiFieldDropdown
                                label="Recurrence"
                                name="recurrence"
                                options={RecurrenceOptions}
                            />
                        </Grid>
                        <Grid item alignContent="flex-end" xs={12}>
                            <Button
                                color="primary"
                                type="submit"
                                variant="contained"
                            >
                                Submit
                            </Button>
                        </Grid>
                    </Grid>
                </form>
            </FormProvider>

            <Box padding={24} />

            <Grid
                container
                alignItems="center"
                className={homePageClasses.homePage}
                justify="center"
            >
                <Grid container justify="center" spacing={4}>
                    {/* Alerts */}
                    <Grid item>
                        <ButtonTile
                            icon={AlertIcon}
                            iconColor={theme.palette.primary.main}
                            text={t("label.alerts")}
                            onClick={handleAlertsClick}
                        />
                    </Grid>

                    {/* Anomalies */}
                    <Grid item>
                        <ButtonTile
                            icon={AnomalyIcon}
                            iconColor={theme.palette.primary.main}
                            text={t("label.anomalies")}
                            onClick={handleAnomaliesClick}
                        />
                    </Grid>

                    {/* Configuration */}
                    <Grid item>
                        <ButtonTile
                            icon={ConfigurationIcon}
                            iconColor={theme.palette.primary.main}
                            text={t("label.configuration")}
                            onClick={handleConfigurationClick}
                        />
                    </Grid>

                    {/* Subscription groups */}
                    <Grid item>
                        <ButtonTile
                            icon={SubscriptionGroupIcon}
                            iconColor={theme.palette.primary.main}
                            text={t("label.subscription-groups")}
                            onClick={handleSubscriptionGroupsClick}
                        />
                    </Grid>

                    {/* Metrics */}
                    <Grid item>
                        <ButtonTile
                            icon={MetricIcon}
                            iconColor={theme.palette.primary.main}
                            text={t("label.metrics")}
                            onClick={handleMetricsClick}
                        />
                    </Grid>
                </Grid>
            </Grid>
        </PageContents>
    );
};

export const MONTHS = [
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December",
];
export const WEEK_DAYS = [
    "Sunday",
    "Monday",
    "Tuesday",
    "Wednesday",
    "Thursday",
    "Friday",
    "Saturday",
];

const RecurrenceOptions: MultiFieldValueOption[] = [
    {
        fields: [
            {
                label: "Month",
                name: "month",
                options: MONTHS.map((month) => ({
                    label: month,
                    value: lowerCase(month),
                })),
                type: "select",
            },
            {
                label: "Day of Month",
                name: "dayOfMonth",
                type: "number",
            },
            {
                label: "Hour",
                name: "hour",
                type: "number",
            },
            {
                label: "Minute",
                name: "minute",
                type: "number",
            },
        ],
        value: "FREQUENCY_EVERY_YEAR",
        label: "Every year",
    },
    {
        fields: [
            {
                label: "Day of Week",
                name: "dayOfWeek",
                options: WEEK_DAYS.map((month) => ({
                    label: month,
                    value: lowerCase(month),
                })),
                type: "select",
            },
            {
                label: "Hour",
                name: "hour",
                type: "number",
            },
            {
                label: "Minute",
                name: "minute",
                type: "number",
            },
        ],
        value: "FREQUENCY_EVERY_WEEK",
        label: "Every week",
    },
    {
        fields: [
            {
                label: "Hour",
                name: "hour",
                type: "number",
            },
            {
                label: "Minute",
                name: "minute",
                type: "number",
            },
        ],
        value: "FREQUENCY_EVERY_DAY",
        label: "Every day",
    },
    {
        fields: [
            {
                label: "Minute",
                name: "minute",
                type: "number",
            },
        ],
        value: "FREQUENCY_EVERY_HOUR",
        label: "Every hour",
    },
    {
        value: "FREQUENCY_SIX_HOURS",
        label: "Every six hours",
    },
];
