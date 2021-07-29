import {
    Accordion,
    AccordionDetails,
    AccordionSummary,
    Typography,
} from "@material-ui/core";
import ExpandMoreIcon from "@material-ui/icons/ExpandMore";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import {
    getUiDatasetDatasourceId,
    getUiDatasetDatasourceName,
} from "../../utils/datasets/datasets.util";
import { getDatasourcesViewPath } from "../../utils/routes/routes.util";
import { TransferList } from "../transfer-list/transfer-list.component";
import { DatasetDatasourcesAccordianProps } from "./dataset-datasources-accordian.interfaces";

export const DatasetDatasourcesAccordian: FunctionComponent<DatasetDatasourcesAccordianProps> = (
    props: DatasetDatasourcesAccordianProps
) => {
    const history = useHistory();
    const { t } = useTranslation();

    const handleDatasourceClick = (datasource: Datasource): void => {
        history.push(getDatasourcesViewPath(datasource.id));
    };

    return (
        <Accordion defaultExpanded={props.defaultExpanded} variant="outlined">
            {/* Header */}
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography variant="h6">{props.title}</Typography>
            </AccordionSummary>

            {/* Dataset datasources transfer list */}
            <AccordionDetails>
                <TransferList
                    link
                    fromLabel={t("label.all-entity", {
                        entity: t("label.datasources"),
                    })}
                    fromList={props.datasources}
                    listItemKeyFn={getUiDatasetDatasourceId}
                    listItemTextFn={getUiDatasetDatasourceName}
                    loading={!props.dataset}
                    toLabel={t("label.associated-datasources")}
                    toList={
                        (props.dataset &&
                            (props.dataset.datasources as Datasource[])) ||
                        []
                    }
                    onChange={props.onChange}
                    onClick={handleDatasourceClick}
                />
            </AccordionDetails>
        </Accordion>
    );
};
