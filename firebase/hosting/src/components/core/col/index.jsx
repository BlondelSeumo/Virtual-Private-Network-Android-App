import * as React from "react";

const Col = ({style, children, styles, className, ...rest}) => (
  <div className={`${className || ''}`} style={style || {}} {...rest}>
    { children || null }
    {styles && <style jsx>{styles}</style>}
  </div>
)

Col.defaultProps = {
  className: 'col',
  styles: undefined,
  style: {},
}

export default Col;
