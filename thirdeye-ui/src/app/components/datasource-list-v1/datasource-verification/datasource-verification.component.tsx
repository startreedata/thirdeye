import { Box, Typography } from "@material-ui/core";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { TooltipV1 } from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetDatasourceStatus } from "../../../rest/datasources/datasources.actions";
import { OK_STATUS } from "../../../rest/datasources/datasources.rest";
import { ActiveIndicator } from "../../active-indicator/active-indicator.component";
import { DatasourceVerificationProps } from "./datasource-verification.interfaces";

export const DatasourceVerification: FunctionComponent<
    DatasourceVerificationProps
> = ({ datasourceName }) => {
    const {
        getDatasourceStatus,
        healthStatus,
        errorMessages,
        status: requestStatus,
    } = useGetDatasourceStatus();
    const { t } = useTranslation();

    useEffect(() => {
        getDatasourceStatus(datasourceName);
    }, [datasourceName]);

    if (requestStatus === ActionStatus.Working) {
        return <span>{t("message.checking-datasource-health")}</span>;
    }

    // Server Error
    if (requestStatus === ActionStatus.Error) {
        return (
            <>
                <Typography color="error" variant="body2">
                    {t(
                        "message.error-encountered-while-fetching-datasource-status"
                    )}
                </Typography>
                {errorMessages.map((msg, idx) => (
                    <Typography color="error" key={`${idx}`} variant="body2">
                        {msg}
                    </Typography>
                ))}
            </>
        );
    }

    if (requestStatus === ActionStatus.Done && healthStatus) {
        if (healthStatus.code === OK_STATUS) {
            return <ActiveIndicator active />;
        } else if (healthStatus.list) {
            const content = healthStatus.list.map((errorStatus, idx) => (
                <Box key={`${idx}`} marginBottom={1}>
                    {errorStatus.msg}
                </Box>
            ));

            return (
                <TooltipV1 placement="top" title={content}>
                    <span>
                        <ActiveIndicator active={false} />
                    </span>
                </TooltipV1>
            );
        }
    }

    return null;
};
