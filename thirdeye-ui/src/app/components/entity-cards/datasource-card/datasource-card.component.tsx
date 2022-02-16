import {
    Card,
    CardContent,
    CardHeader,
    Grid,
    IconButton,
    Link,
    Menu,
    MenuItem,
} from "@material-ui/core";
import MoreVertIcon from "@material-ui/icons/MoreVert";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import {
    getDatasourcesUpdatePath,
    getDatasourcesViewPath,
} from "../../../utils/routes/routes.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { NameValueDisplayCard } from "../name-value-display-card/name-value-display-card.component";
import { DatasourceCardProps } from "./datasource-card.interfaces";

export const DatasourceCard: FunctionComponent<DatasourceCardProps> = (
    props: DatasourceCardProps
) => {
    const [datasourceOptionsAnchorElement, setDatasourceOptionsAnchorElement] =
        useState<HTMLElement | null>();
    const navigate = useNavigate();
    const { t } = useTranslation();

    const handleDatasourceOptionsClick = (
        event: MouseEvent<HTMLElement>
    ): void => {
        setDatasourceOptionsAnchorElement(event.currentTarget);
    };

    const handleDatasourceOptionsClose = (): void => {
        setDatasourceOptionsAnchorElement(null);
    };

    const handleDatasourceViewDetails = (): void => {
        if (!props.uiDatasource) {
            return;
        }

        navigate(getDatasourcesViewPath(props.uiDatasource.id));
        handleDatasourceOptionsClose();
    };

    const handleDatasourceEdit = (): void => {
        if (!props.uiDatasource) {
            return;
        }

        navigate(getDatasourcesUpdatePath(props.uiDatasource.id));
        handleDatasourceOptionsClose();
    };

    const handleDatasourceDelete = (): void => {
        if (!props.uiDatasource) {
            return;
        }

        props.onDelete && props.onDelete(props.uiDatasource);
        handleDatasourceOptionsClose();
    };

    return (
        <Card variant="outlined">
            {props.uiDatasource && (
                <CardHeader
                    action={
                        <Grid container alignItems="center" spacing={0}>
                            <Grid item>
                                {/* Datasource options button */}
                                <IconButton
                                    onClick={handleDatasourceOptionsClick}
                                >
                                    <MoreVertIcon />
                                </IconButton>

                                {/* Datasource options */}
                                <Menu
                                    anchorEl={datasourceOptionsAnchorElement}
                                    open={Boolean(
                                        datasourceOptionsAnchorElement
                                    )}
                                    onClose={handleDatasourceOptionsClose}
                                >
                                    {/* View details */}
                                    {props.showViewDetails && (
                                        <MenuItem
                                            onClick={
                                                handleDatasourceViewDetails
                                            }
                                        >
                                            {t("label.view-details")}
                                        </MenuItem>
                                    )}

                                    {/* Edit datasource */}
                                    <MenuItem onClick={handleDatasourceEdit}>
                                        {t("label.edit-entity", {
                                            entity: t("label.datasource"),
                                        })}
                                    </MenuItem>

                                    {/* Delete datasource */}
                                    <MenuItem onClick={handleDatasourceDelete}>
                                        {t("label.delete-entity", {
                                            entity: t("label.datasource"),
                                        })}
                                    </MenuItem>
                                </Menu>
                            </Grid>
                        </Grid>
                    }
                    title={
                        <>
                            {/* Datasource name */}
                            {props.showViewDetails && (
                                <Link onClick={handleDatasourceViewDetails}>
                                    <TextHighlighter
                                        searchWords={props.searchWords}
                                        text={props.uiDatasource.name}
                                    />
                                </Link>
                            )}

                            {/* Summary */}
                            {!props.showViewDetails && t("label.summary")}
                        </>
                    }
                    titleTypographyProps={{ variant: "h6" }}
                />
            )}

            <CardContent>
                {props.uiDatasource && (
                    <Grid container>
                        {/* Datasource type */}
                        <Grid item md={3} sm={6} xs={12}>
                            <NameValueDisplayCard<string>
                                name={t("label.type")}
                                searchWords={props.searchWords}
                                values={[props.uiDatasource.type]}
                            />
                        </Grid>
                    </Grid>
                )}

                {/* No data available */}
                {!props.uiDatasource && <NoDataIndicator />}
            </CardContent>
        </Card>
    );
};
