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
} from "@material-ui/core";
import { Close, SubdirectoryArrowLeft } from "@material-ui/icons";
import React, {
    createRef,
    FunctionComponent,
    KeyboardEvent,
    useEffect,
    useState,
} from "react";
import { EditableListProps } from "./editable-list.interfaces";
import { useEditableListStyles } from "./editable-list.styles";

export const EditableList: FunctionComponent<EditableListProps> = (
    props: EditableListProps
) => {
    const editableListClasses = useEditableListStyles();
    const [list, setList] = useState<string[]>([]);
    const [helperText, setHelperText] = useState("");
    const inputRef = createRef<HTMLInputElement>();

    useEffect(() => {
        // Input changed, populate list
        setList(props.list || []);
    }, [props.list]);

    const onInputKeyPress = (event: KeyboardEvent<HTMLInputElement>): void => {
        if (event.key === "Enter") {
            onAddListItem();
        }
    };

    const onAddListItem = (): void => {
        if (!inputRef || !inputRef.current || !inputRef.current.value) {
            return;
        }

        const input = inputRef.current.value;
        let validationResult;
        if (
            (validationResult = props.validateFn && props.validateFn(input)) &&
            !validationResult.valid
        ) {
            // Validation failed
            setHelperText(validationResult.message || "");

            return;
        }

        setList((list) => [input, ...list]);
        reset();

        // Notify
        props.onChange && props.onChange(list);
    };

    const onRemoveListItem = (index: number) => (): void => {
        setList((list) => {
            list.splice(index, 1);

            return [...list];
        });

        // Notify
        props.onChange && props.onChange(list);
    };

    const reset = (): void => {
        if (!inputRef || !inputRef.current || !inputRef.current.value) {
            return;
        }

        // Clear input
        setHelperText("");
        inputRef.current.value = "";

        // Set focus
        inputRef.current.focus();
    };

    return (
        <Grid container>
            {/* Input */}
            <Grid item md={9}>
                <TextField
                    fullWidth
                    InputProps={{
                        endAdornment: (
                            // Add button
                            <InputAdornment position="end">
                                <IconButton onClick={onAddListItem}>
                                    <SubdirectoryArrowLeft />
                                </IconButton>
                            </InputAdornment>
                        ),
                    }}
                    error={Boolean(helperText)}
                    helperText={helperText}
                    inputRef={inputRef}
                    label={props.inputLabel}
                    variant="outlined"
                    onKeyPress={onInputKeyPress}
                />
            </Grid>

            {/* Add button */}
            <Grid item md={3}>
                <Button
                    fullWidth
                    className={editableListClasses.addButton}
                    color="primary"
                    size="large"
                    variant="outlined"
                    onClick={onAddListItem}
                >
                    {props.buttonLabel}
                </Button>
            </Grid>

            {/* List */}
            <Grid item md={12}>
                <Card variant="outlined">
                    <CardContent className={editableListClasses.listContainer}>
                        <List dense>
                            {list &&
                                list.map((listItem, index) => (
                                    <ListItem
                                        button
                                        key={index}
                                        onClick={(): void => {
                                            onRemoveListItem(index);
                                        }}
                                    >
                                        <ListItemText primary={listItem} />

                                        {/* Remove button */}
                                        <ListItemSecondaryAction>
                                            <IconButton
                                                onClick={onRemoveListItem(
                                                    index
                                                )}
                                            >
                                                <Close />
                                            </IconButton>
                                        </ListItemSecondaryAction>
                                    </ListItem>
                                ))}
                        </List>
                    </CardContent>
                </Card>
            </Grid>
        </Grid>
    );
};
