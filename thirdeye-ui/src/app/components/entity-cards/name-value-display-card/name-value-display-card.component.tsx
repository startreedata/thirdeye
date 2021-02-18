import {
    Card,
    CardContent,
    Grid,
    Link,
    List,
    ListItem,
    ListItemText,
    Typography,
} from "@material-ui/core";
import { isEmpty } from "lodash";
import React, { ReactElement } from "react";
import { useTranslation } from "react-i18next";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { NameValueDisplayCardProps } from "./name-value-display-card.interfaces";
import { useNameValueDisplayCardStyles } from "./name-value-display-card.styles";

export function NameValueDisplayCard<T>(
    props: NameValueDisplayCardProps<T>
): ReactElement {
    const nameValueDisplayCardClasses = useNameValueDisplayCardStyles();
    const { t } = useTranslation();

    const getValueText = (value: T): string => {
        if (!value) {
            return "";
        }

        if (typeof value === "string") {
            return value;
        }

        if (props.valueTextFn) {
            return props.valueTextFn(value);
        }

        return "";
    };

    return (
        <Card
            className={nameValueDisplayCardClasses.nameValueDisplayCard}
            variant="outlined"
        >
            <CardContent>
                <Grid container>
                    <Grid item xs={12}>
                        <Grid
                            container
                            alignItems="center"
                            justify="space-between"
                        >
                            {/* Name */}
                            <Grid item>
                                <Typography variant="subtitle1">
                                    {props.name}
                                </Typography>
                            </Grid>

                            {/* Count */}
                            {props.showCount && (
                                <Grid item>
                                    <Typography
                                        color="textSecondary"
                                        variant="body2"
                                    >
                                        {props.values &&
                                            props.values.length > 0 &&
                                            props.values.length}
                                    </Typography>
                                </Grid>
                            )}
                        </Grid>
                    </Grid>

                    {/* Value */}
                    <Grid item xs={12}>
                        <List
                            disablePadding
                            className={nameValueDisplayCardClasses.list}
                        >
                            {props.values &&
                                props.values.map((value, index) => (
                                    <ListItem
                                        disableGutters
                                        className={
                                            nameValueDisplayCardClasses.listItem
                                        }
                                        key={index}
                                    >
                                        <ListItemText
                                            className={
                                                nameValueDisplayCardClasses.listItemText
                                            }
                                            primary={
                                                <>
                                                    {/* Value as text */}
                                                    {!props.link && (
                                                        <TextHighlighter
                                                            searchWords={
                                                                props.searchWords
                                                            }
                                                            text={getValueText(
                                                                value
                                                            )}
                                                        />
                                                    )}

                                                    {/* Value as link */}
                                                    {props.link && (
                                                        <Link
                                                            noWrap
                                                            display="block"
                                                            onClick={() =>
                                                                props.onClick &&
                                                                props.onClick(
                                                                    value
                                                                )
                                                            }
                                                        >
                                                            <TextHighlighter
                                                                searchWords={
                                                                    props.searchWords
                                                                }
                                                                text={getValueText(
                                                                    value
                                                                )}
                                                            />
                                                        </Link>
                                                    )}
                                                </>
                                            }
                                            primaryTypographyProps={{
                                                variant: "body1",
                                                noWrap: true,
                                                className: props.valueClassName,
                                            }}
                                        />
                                    </ListItem>
                                ))}

                            {/* No data available */}
                            {isEmpty(props.values) && (
                                <ListItem
                                    disableGutters
                                    className={
                                        nameValueDisplayCardClasses.listItem
                                    }
                                >
                                    <ListItemText
                                        className={
                                            nameValueDisplayCardClasses.listItemText
                                        }
                                        primary={
                                            <TextHighlighter
                                                searchWords={props.searchWords}
                                                text={t("label.no-data-marker")}
                                            />
                                        }
                                        primaryTypographyProps={{
                                            variant: "body1",
                                            noWrap: true,
                                        }}
                                    />
                                </ListItem>
                            )}
                        </List>
                    </Grid>
                </Grid>
            </CardContent>
        </Card>
    );
}
