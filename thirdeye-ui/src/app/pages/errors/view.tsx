import React from "react"
import { useParams } from "react-router-dom";
import { getErrorObj } from "./util";

export const ErrorView = () => {
  const {id} = useParams();
  // const errorObj = getErrorObj(Number(id))
  const errorObj = {
    "user": "test@test.com",
    "os": "MacOS",
    "userAgentInfo": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
    "errorType": "TypeError",
    "errorMessage": "Cannot read properties of null (reading 'name')",
    "errorInfo": {
        "componentStack": "\n    in HomePage (created by GeneralAuthenticatedRouter)\n    in CancelAPICallsOnPageUnload (created by GeneralAuthenticatedRouter)\n    in RenderedRoute (created by Routes)\n    in Routes (created by GeneralAuthenticatedRouter)\n    in Suspense (created by GeneralAuthenticatedRouter)\n    in GeneralAuthenticatedRouter (created by AppRouter)\n    in RenderedRoute (created by Routes)\n    in Routes (created by AppRouter)\n    in Suspense (created by AppRouter)\n    in AppRouter (created by App)\n    in div (created by AppBarConfigProvider)\n    in div (created by AppBarConfigProvider)\n    in AppBarConfigProvider (created by App)\n    in ErrorBoundary (created by App)\n    in ErrorBoundary (created by App)\n    in div (created by AppContainerV1)\n    in HelmetProvider (created by AppContainerV1)\n    in AppContainerV1 (created by App)\n    in IntercomProvider (created by AnalyticsAndErrorReportingProviderV1)\n    in AnalyticsAndErrorReportingProviderV1 (created by App)\n    in QueryClientProvider (created by App)\n    in App\n    in DialogProviderV1\n    in TimeRangeProvider\n    in AuthProviderV1 (created by AuthProviderWrapper)\n    in AuthProviderWrapper\n    in NotificationProviderV1\n    in Router (created by BrowserRouter)\n    in BrowserRouter\n    in ThemeProvider\n    in StrictMode"
    },
    "url": "http://localhost:7004/home",
    "project": "ThirdEye Ui"
  }

  const renderStack = (stackObj) => {
    const componentStack = stackObj.componentStack.split('\n')
    return (
      <div>
      {componentStack.map(stackFrame=> <div>{stackFrame}</div>)}
      </div>
    )
  }
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        gap: '20px',
        padding: '20px',
        border: '1px solid black',
        borderTop: 0
      }}
    >
      {Object.entries(errorObj).map(entry => {
        return (
          <div style={{display: 'flex', gap: '50px'}}>
            <div style={{width: '15%'}}>
              {entry[0]}
            </div>
            <div>
              {typeof entry[1] === 'object' ? renderStack(entry[1]) : entry[1]}
            </div>
          </div>
        )
      })}
    </div>
  )
}