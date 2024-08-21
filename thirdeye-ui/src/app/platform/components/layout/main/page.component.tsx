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
import React, { FunctionComponent } from "react";
import { Helmet } from "react-helmet-async";
import { PageV1Props } from "./page.interfaces";
import { useStyles } from "./page.styles";

export const MainLayout: FunctionComponent<PageV1Props> = ({
    documentTitle,
    children,
}) => {
    const pageStyles = useStyles();

    return (
        <div className={pageStyles.container}>
            {/* Document title */}
            <Helmet>
                <title>{documentTitle}</title>
            </Helmet>

            {children}
        </div>
    );
};

// const PageV1Context = createContext<PageV1ContextProps>(
//     {} as PageV1ContextProps
// );

// export const usePageV1 = (): PageV1ContextProps => {
//     return useContext(PageV1Context);
// };
