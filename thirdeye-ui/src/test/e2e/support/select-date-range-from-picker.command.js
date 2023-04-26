/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { DateTime } from "luxon";
import { TIME_SELECTOR_TEST_IDS } from "../../../app/components/time-range/time-range-selector/time-range-selector/time-range-selector.interfaces";

const BASE_DATE_CONTAINER_SELECTOR = ".MuiPickersBasePicker-container";

function clickButtons(scope, dateToChangeTo) {
    scope.within(() => {
        if (dateToChangeTo?.year) {
            cy.getByDataTestId(TIME_SELECTOR_TEST_IDS.YEAR_LINK).click();
            cy.get("div[role='button']")
                .contains(dateToChangeTo?.year.toString())
                .click();
        }

        if (dateToChangeTo?.month) {
            const monthToChange = DateTime.now()
                .set({ month: dateToChangeTo.month })
                .toLocal()
                .toFormat("MMM");

            // Skip selecting the month if it matches the desired month
            cy.getByDataTestId(TIME_SELECTOR_TEST_IDS.MONTH_LINK).then(
                ($el) => {
                    const text = $el.text();

                    if (text !== monthToChange) {
                        cy.getByDataTestId(
                            TIME_SELECTOR_TEST_IDS.MONTH_LINK
                        ).click();
                        cy.get("div[role='button']")
                            .contains(monthToChange)
                            .click();
                    }
                }
            );
        }

        if (dateToChangeTo?.date) {
            cy.getByDataTestId(TIME_SELECTOR_TEST_IDS.DATE_LINK).click();
            cy.get("button.MuiPickersDay-day")
                .contains(dateToChangeTo?.date.toString())
                .click();
        }
    });
}

/**
 * Start and end are expected to be objects with year, month, and day properties
 */
Cypress.Commands.add("selectDateRangeFromPicker", (start, end) => {
    cy.getByDataTestId(TIME_SELECTOR_TEST_IDS.OPEN_BUTTON).click();

    start && clickButtons(cy.get(BASE_DATE_CONTAINER_SELECTOR).first(), start);
    end && clickButtons(cy.get(BASE_DATE_CONTAINER_SELECTOR).last(), end);
    cy.getByDataTestId(TIME_SELECTOR_TEST_IDS.APPLY_BUTTON).click();
});
