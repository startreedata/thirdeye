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
import React, { ReactElement, ReactNode } from "react";
import { useTranslation } from "react-i18next";
import { TextHighlighter } from "../../text-highlighter/text-highlighter.component";
import { NameValueDisplayCardProps } from "./name-value-display-card.interfaces";
import { useNameValueDisplayCardStyles } from "./name-value-display-card.styles";

export function NameValueDisplayCard<T>(
    props: NameValueDisplayCardProps<T>
): ReactElement {
    const nameValueDisplayCardClasses = useNameValueDisplayCardStyles();
    const { t } = useTranslation();

    const getValue = (value: T): ReactNode => {
        if (props.valueRenderer) {
            return props.valueRenderer(value);
        }

        if (typeof value === "string") {
            return value;
        }

        return "";
    };

    return (
        <Card
            className={nameValueDisplayCardClasses.nameValueDisplayCard}
            variant="outlined"
        >
            <CardContent
                className={
                    nameValueDisplayCardClasses.nameValueDisplayCardContent
                }
            >
                <Grid container spacing={1}>
                    {/* Name and count */}
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

                    {/* Values */}
                    <Grid item xs={12}>
                        <List
                            disablePadding
                            className={nameValueDisplayCardClasses.list}
                        >
                            {/* Values */}
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
                                                        <>
                                                            {/* Value as string */}
                                                            {typeof getValue(
                                                                value
                                                            ) === "string" && (
                                                                <TextHighlighter
                                                                    searchWords={
                                                                        props.searchWords
                                                                    }
                                                                    text={
                                                                        getValue(
                                                                            value
                                                                        ) as string
                                                                    }
                                                                />
                                                            )}

                                                            {/* Value as component */}
                                                            {typeof getValue(
                                                                value
                                                            ) !== "string" && (
                                                                <>
                                                                    {getValue(
                                                                        value
                                                                    )}
                                                                </>
                                                            )}
                                                        </>
                                                    )}

                                                    {/* Value as link */}
                                                    {props.link && (
                                                        <Link
                                                            display="block"
                                                            noWrap={!props.wrap}
                                                            onClick={() =>
                                                                props.onClick &&
                                                                props.onClick(
                                                                    value
                                                                )
                                                            }
                                                        >
                                                            <>
                                                                {/* Value as string */}
                                                                {typeof getValue(
                                                                    value
                                                                ) ===
                                                                    "string" && (
                                                                    <TextHighlighter
                                                                        searchWords={
                                                                            props.searchWords
                                                                        }
                                                                        text={
                                                                            getValue(
                                                                                value
                                                                            ) as string
                                                                        }
                                                                    />
                                                                )}

                                                                {/* Value as component */}
                                                                {typeof getValue(
                                                                    value
                                                                ) !==
                                                                    "string" && (
                                                                    <>
                                                                        {getValue(
                                                                            value
                                                                        )}
                                                                    </>
                                                                )}
                                                            </>
                                                        </Link>
                                                    )}
                                                </>
                                            }
                                            primaryTypographyProps={{
                                                variant: "body2",
                                                noWrap: !props.wrap,
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
                                        primary={t("label.no-data-marker")}
                                        primaryTypographyProps={{
                                            variant: "body2",
                                            noWrap: !props.wrap,
                                            className: props.valueClassName,
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
