import { Typography } from "@material-ui/core";
import { Skeleton } from "@material-ui/lab";
import classNames from "classnames";
import React, { FunctionComponent, useEffect } from "react";
import { usePageV1 } from "../../page-v1/page-v1.component";
import { PageHeaderTextV1Props } from "./page-header-text-v1.interfaces";
import { usePageHeaderTextV1Styles } from "./page-header-text-v1.styles";

export const PageHeaderTextV1: FunctionComponent<PageHeaderTextV1Props> = ({
    className,
    children,
    ...otherProps
}) => {
    const pageHeaderTextV1Classes = usePageHeaderTextV1Styles();
    const { headerVisible, setHeaderText } = usePageV1();

    useEffect(() => {
        // Set children as header text
        setHeaderText(children);
    }, [children]);

    return (
        <>
            {headerVisible && (
                // Visible only when header is visible
                <Typography
                    {...otherProps}
                    noWrap
                    className={classNames(
                        {
                            [pageHeaderTextV1Classes.pageHeaderText]: !children,
                        },
                        className,
                        "page-header-text-v1"
                    )}
                    variant="h4"
                >
                    {children}

                    {/* Loading indicator */}
                    {!children && <Skeleton />}
                </Typography>
            )}
        </>
    );
};
