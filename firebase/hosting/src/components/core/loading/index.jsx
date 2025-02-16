import React from "react";

import './styles.scss'

export const LoadingPage = () => (
  <div className="page-loading">
    <div className="lds-ring">
      <div/>
      <div/>
    </div>
  </div>
)

export const LoadingPage2 = () => (
  <div className="page-loading opacity">
    <div className="lds-ring">
      <div/>
      <div/>
    </div>
  </div>
)
