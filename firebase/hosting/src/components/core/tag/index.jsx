import React from "react";
import css from 'styled-jsx/css'

const styles = css`
  .core-tag {
      width: 65px;
      height: 16px;
      border-radius: 4px;
      font-size: 8px;
      line-height: 1.25;
      letter-spacing: 1.28px;
      text-align: center;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      &.primary {
        color: #714fff;
        border: solid 1px #714fff;
        background-color: #d5caff;
      }
      &.second {
        color: #ff2e93;
        border: solid 1px #ff2e93;
        background-color: #ffc1df;
      }
  }
`

const Tag = ({ type = "primary", children, className = "", style = {} }) => (
  <span className={`core-tag ${type} ${className}`} style={style}>
    {children}
    <style jsx>{styles}</style>
  </span>
)

export default Tag