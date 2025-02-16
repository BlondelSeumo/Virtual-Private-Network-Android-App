export const AppConst = {
  LOAD_USER_SUCCESS: 'App/LOAD_USER_SUCCESS',
  LOAD_USER: 'App/LOAD_USER',
  SET_USER_INFO: 'SET_USER_INFO',
}

// The initial state of the App
export const initialState = {
  loading: false,
  error: false,
  user: undefined,
  userInfo: {}
};

/* eslint-disable default-case, no-param-reassign */
const appReducer = (state = initialState, action) => {
  switch (action.type) {
    case AppConst.LOAD_USER: return ({
      ...state,
    })

    case AppConst.LOAD_USER_SUCCESS: return ({
      ...state,
      user: action.payload,
    })

    case AppConst.SET_USER_INFO: return ({
      ...state,
      userInfo: action.payload,
    })

    case AppConst.LOAD_USER_ERROR: return ({
      ...state,
      user: undefined,
    })

    default: return ({
      ...state,
    })
  }
}

export default appReducer;
