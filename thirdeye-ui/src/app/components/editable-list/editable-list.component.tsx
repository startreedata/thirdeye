import {
    Button,
    Card,
    CardContent,
    Grid,
    IconButton,
    InputAdornment,
    List,
    ListItem,
    ListItemSecondaryAction,
    ListItemText,
    TextField,
    Typography,
} from "@material-ui/core";
import CloseIcon from "@material-ui/icons/Close";
import SubdirectoryArrowLeftIcon from "@material-ui/icons/SubdirectoryArrowLeft";
import { AppLoadingIndicatorV1 } from "@startree-ui/platform-ui";
import classnames from "classnames";
import React, {
    FunctionComponent,
    KeyboardEvent,
    useEffect,
    useRef,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useCommonStyles } from "../../utils/material-ui/common.styles";
import { EditableListProps } from "./editable-list.interfaces";
import { useEditableListStyles } from "./editable-list.styles";

export const EditableList: FunctionComponent<EditableListProps> = (
    props: EditableListProps
) => {
    const editableListClasses = useEditableListStyles();
    const commonClasses = useCommonStyles();
    const [list, setList] = useState<string[]>([]);
    const [helperText, setHelperText] = useState("");
    const inputRef = useRef<HTMLInputElement>(null);
    const { t } = useTranslation();

    useEffect(() => {
        // Input list changed, populate list
        setList(props.list || []);
    }, [props.list]);

    const handleInputKeyDown = (
        event: KeyboardEvent<HTMLInputElement>
    ): void => {
        if (event.key === "Enter") {
            handleListItemAdd();
        }
    };

    const handleListItemAdd = (): void => {
        if (!inputRef || !inputRef.current || !inputRef.current.value) {
            return;
        }

        const input = inputRef.current.value;
        const validationResult = props.validateFn && props.validateFn(input);
        if (validationResult && !validationResult.valid) {
            // Validation failed
            setHelperText(
                validationResult.message || t("message.validation-failed")
            );

            return;
        }

        const newList = [input, ...list];
        setList(newList);
        resetInput();

        props.onChange && props.onChange(newList);
    };

    const resetInput = (): void => {
        if (!inputRef || !inputRef.current || !inputRef.current.value) {
            return;
        }

        // Clear input
        inputRef.current.value = "";
        setHelperText("");
        // Set focus
        inputRef.current.focus();
    };

    const handleListItemRemove = (index: number) => (): void => {
        const newList = [...list];
        newList.splice(index, 1);
        setList(newList);

        props.onChange && props.onChange(newList);
    };

    return (
        <Grid container alignItems="center">
            {/* Input label */}
            <Grid item xs={1}>
                <Typography variant="subtitle2">
                    {props.inputLabel || t("label.add")}
                </Typography>
            </Grid>

            {/* Input */}
            <Grid item xs={9}>
                <TextField
                    fullWidth
                    InputProps={{
                        endAdornment: (
                            // Add button
                            <InputAdornment position="end">
                                <IconButton onClick={handleListItemAdd}>
                                    <SubdirectoryArrowLeftIcon fontSize="small" />
                                </IconButton>
                            </InputAdornment>
                        ),
                    }}
                    disabled={props.loading}
                    error={Boolean(helperText)}
                    helperText={helperText}
                    inputRef={inputRef}
                    variant="outlined"
                    onKeyDown={handleInputKeyDown}
                />
            </Grid>

            {/* Add button */}
            <Grid item xs={2}>
                <Button
                    fullWidth
                    className={editableListClasses.addButton}
                    color="primary"
                    disabled={props.loading}
                    variant="outlined"
                    onClick={handleListItemAdd}
                >
                    {props.addButtonLabel || t("label.add")}
                </Button>
            </Grid>

            <Grid item xs={12}>
                <Card variant="outlined">
                    <CardContent
                        className={classnames(
                            editableListClasses.list,
                            commonClasses.cardContentBottomPaddingRemoved
                        )}
                    >
                        {/* List */}
                        {!props.loading && (
                            <List disablePadding>
                                {list &&
                                    list.map((listItem, index) => (
                                        <ListItem divider key={index}>
                                            <ListItemText
                                                primary={listItem}
                                                primaryTypographyProps={{
                                                    variant: "body2",
                                                }}
                                            />

                                            {/* Remove button */}
                                            <ListItemSecondaryAction>
                                                <IconButton
                                                    onClick={handleListItemRemove(
                                                        index
                                                    )}
                                                >
                                                    <CloseIcon fontSize="small" />
                                                </IconButton>
                                            </ListItemSecondaryAction>
                                        </ListItem>
                                    ))}
                            </List>
                        )}

                        {/* Loading indicator */}
                        {props.loading && <AppLoadingIndicatorV1 />}
                    </CardContent>
                </Card>
            </Grid>
        </Grid>
    );
};
