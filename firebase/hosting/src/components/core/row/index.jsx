import * as React from "react";

const Row = ({style, children, styles, className, ...rest}) => (
  <div className={`w-full row ${className || ''}`} style={style || {}} {...rest}>
    { children || null }
    {styles && <style jsx>{styles}</style>}
  </div>
)

Row.defaultProps = {
  className: '',
  styles: undefined,
  style: {},
}

export default Row;
