export const parseDataFromServer = ({ columns = [], data = [] }) => {
  return data.reduce((arr, item) => {
    return [
      ...arr,
      {
        ...columns.reduce((tempObject, colum) => {
          return {
            ...tempObject,
            [colum]: (item?.get?.(colum) || ""),
          }
        }, {}),
        id: item?.id || "",
        avatar: (item?.get?.('avatar')?.url?.() || "")
      }
    ]
  }, [])
}