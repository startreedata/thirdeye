import React, { Component } from 'react';
import { getOS, getUserAgent } from './util';
class ErrorBoundary extends Component {
  state = {
    hasError: false,
    error: null,
    errorInfo: null,
  };

  static getDerivedStateFromError(error) {
    // Update state to trigger a re-render and show fallback UI
    return { hasError: true };
  }

  componentDidCatch(error, errorInfo) {
    const url = window.location.href
    console.log('Caught an error:', error);
    console.log('er', error.message)
    console.log('Error stack trace:', errorInfo);

    const errorObj = {
      user: {email: 'test@test.com'},
      os: getOS(),
      userAgentInfo: getUserAgent(),
      errorType: error.name,
      errorMessage: error.message,
      errorInfo,
      url,
      project: 'ThirdEye Ui'
    }
    console.log('err', errorObj)
    this.setState({
      error,
      errorInfo,
    });
  }

  render() {
    if (this.state.hasError) {
      return (
        <div>
          <h1>Something went wrong.</h1>
          <details style={{ whiteSpace: 'pre-wrap' }}>
            {this.state.error && this.state.error.toString()}
            <br />
            {/* {this.state.errorInfo.componentStack} */}
          </details>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
