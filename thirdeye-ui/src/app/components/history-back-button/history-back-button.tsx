import { Button, ButtonProps } from "@material-ui/core";
import ArrowBackIcon from "@material-ui/icons/ArrowBack";
import React, { FC, ReactNode, useCallback } from "react";
import { useNavigate } from "react-router-dom";

interface HistoryBackButtonProps {
    buttonText?: string;
    hideIcon?: boolean;
    icon?: ReactNode;
    onClick?: () => void;
    preventDefault?: boolean;
    buttonProps?: Partial<ButtonProps>;
}

export const HistoryBackButton: FC<HistoryBackButtonProps> = ({
    buttonText = "Go back",
    icon,
    hideIcon,
    onClick,
    preventDefault,
    buttonProps,
}: HistoryBackButtonProps) => {
    const backIcon = icon ?? <ArrowBackIcon />;
    const navigate = useNavigate();

    const handleBackClick = useCallback(() => {
        !preventDefault && navigate(-1);
        onClick && onClick();
    }, [navigate, onClick, preventDefault]);

    return (
        <Button
            color="primary"
            size="small"
            startIcon={!hideIcon && backIcon}
            variant="text"
            onClick={handleBackClick}
            {...buttonProps}
        >
            {buttonText}
        </Button>
    );
};
