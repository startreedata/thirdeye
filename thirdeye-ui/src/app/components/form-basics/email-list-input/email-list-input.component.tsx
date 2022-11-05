import { FormHelperText, TextField } from "@material-ui/core";
import { debounce } from "lodash";
import React, {
    FunctionComponent,
    useCallback,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { validateEmail } from "../../../utils/validation/validation.util";
import { EmailListInputProps } from "./email-list-input.interfaces";

export const EmailListInput: FunctionComponent<EmailListInputProps> = ({
    emails,
    onChange,
}) => {
    const { t } = useTranslation();
    const [emailInputString, setEmailInputString] = useState(emails.join(", "));
    const [invalidEmailStrings, setInvalidEmailStrings] = useState<string[]>(
        []
    );

    const validateEmailListInput = useCallback(
        debounce((emailInputStringToValidate: string) => {
            const validEmails: string[] = [];
            const invalidEmails: string[] = [];

            if (emailInputStringToValidate.length > 0) {
                emailInputStringToValidate
                    .split(",")
                    .forEach((emailInputSubstring) => {
                        const trimmed = emailInputSubstring.trim();
                        const validationStatus = validateEmail(trimmed);

                        if (validationStatus.valid) {
                            validEmails.push(emailInputSubstring);
                        } else {
                            invalidEmails.push(emailInputSubstring);
                        }
                    });
            }

            onChange(validEmails);
            setInvalidEmailStrings(invalidEmails);
        }, 500),
        []
    );

    useEffect(() => {
        // Reset the invalid email ist when user first types
        setInvalidEmailStrings([]);

        validateEmailListInput(emailInputString);
    }, [emailInputString]);

    return (
        <>
            <TextField
                fullWidth
                multiline
                minRows={4}
                value={emailInputString}
                variant="outlined"
                onChange={(e) => {
                    setEmailInputString(e.currentTarget.value);
                }}
            />
            {invalidEmailStrings.length > 0 && (
                <FormHelperText error>
                    {invalidEmailStrings.length === 1 && (
                        <span>
                            {invalidEmailStrings[0]} is not a valid email
                            address
                        </span>
                    )}
                    {invalidEmailStrings.length > 1 && (
                        <span>
                            {invalidEmailStrings.join(", ")} are not a valid
                            email address
                        </span>
                    )}
                </FormHelperText>
            )}
            {invalidEmailStrings.length === 0 && (
                <FormHelperText>
                    {t("message.add-emails-example")}
                </FormHelperText>
            )}
        </>
    );
};
