import classNames from "classnames";
import React, {
    createContext,
    FunctionComponent,
    ReactNode,
    useContext,
    useEffect,
    useState,
} from "react";
import { Helmet } from "react-helmet-async";
import { useAppContainerV1 } from "../../app-container-v1/app-container-v1.component";
import { PageV1ContextProps, PageV1Props } from "./page-v1.interfaces";
import { usePageV1Styles } from "./page-v1.styles";

export const PageV1: FunctionComponent<PageV1Props> = ({
    className,
    children,
    ...otherProps
}) => {
    const pageV1Classes = usePageV1Styles();
    const [headerVisible, setHeaderVisible] = useState(true);
    const [headerText, setHeaderText] = useState<ReactNode>("");
    const [currentHeaderTab, setCurrentHeaderTab] = useState<ReactNode>("");
    const [documentTitle, setDocumentTitle] = useState("");
    const { name } = useAppContainerV1();

    useEffect(() => {
        // Construct document title
        let documentTitle = "";

        if (headerText && typeof headerText === "string") {
            // Document title to contain header text
            documentTitle = headerText;
        }

        if (currentHeaderTab && typeof currentHeaderTab === "string") {
            // Document title to contain current header tab
            documentTitle = documentTitle
                ? `${documentTitle}: ${currentHeaderTab}`
                : currentHeaderTab;
        }

        documentTitle = documentTitle ? `${documentTitle} - ${name}` : name;
        setDocumentTitle(documentTitle);
    }, [headerText, currentHeaderTab, name]);

    const pageV1Context = {
        headerVisible: headerVisible,
        setHeaderVisible: setHeaderVisible,
        setHeaderText: setHeaderText,
        setCurrentHeaderTab: setCurrentHeaderTab,
    };

    return (
        <PageV1Context.Provider value={pageV1Context}>
            {/* Document title */}
            <Helmet>
                <title>{documentTitle}</title>
            </Helmet>

            <div
                {...otherProps}
                className={classNames(pageV1Classes.page, className, "page-v1")}
            >
                {children}
            </div>
        </PageV1Context.Provider>
    );
};

const PageV1Context = createContext<PageV1ContextProps>(
    {} as PageV1ContextProps
);

export const usePageV1 = (): PageV1ContextProps => {
    return useContext(PageV1Context);
};
