import * as React from "react";
import 'styled-jsx/css';

const Container = ({style, children, className, ...rest}) => (
  <div className={`container ${className || ''}`} style={style || {}} {...rest}>
    { children || null }
  </div>
)

Container.defaultProps = {
  className: '',
  style: {},
}

export default Container;
