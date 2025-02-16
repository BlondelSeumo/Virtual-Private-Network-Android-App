import {AppConst} from "@app/redux/reducers";

export const loadUser = (payload) => {
  return {
    type: AppConst.LOAD_USER_SUCCESS,
    payload
  };
}

export const setUserInfo = (payload) => {
  return {
    type: AppConst.SET_USER_INFO,
    payload
  };
}
