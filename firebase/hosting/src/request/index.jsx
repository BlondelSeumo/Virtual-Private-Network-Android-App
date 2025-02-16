import axios from 'axios';
export const API = process.env.API;
export const ENV = process.env.ENV_NAME || 'dev';

const axiosInstance = axios.create({
  headers: {
    Accept: 'application/json',
  },
});

const headers = () => ({});

const getToken = () => {
  return localStorage.getItem(`wizmeet-${ENV}-uuid`)
}

const GET = (url, params = {}, isToken = false) => {
  if (isToken) {
    return axiosInstance.get(url, {
      params: {
        ...params,
      },
      headers: {
        ...headers(),
        authorization: `Bearer ${getToken()}`,
      },
    });
  }

  return axiosInstance.get(
    url,
    {
      params,
    },
  );
};

const DELETE = (url, params = {}, isToken = false) => {
  if (isToken) {
    return axiosInstance.delete(url, {
      params: {
        ...params,
      },
      headers: {
        ...headers(),
        authorization: `Bearer ${getToken()}`,
      },
    });
  }

  return axiosInstance.delete(url, {
    params,
  });
};

const POST = (url, formData, params = {}, isToken = false) => {
  if (isToken) {
    return axiosInstance.post(
      url,
      {...formData},
      {
        params: {
          ...params,
        },
        headers: {
          ...headers(),
          authorization: `Bearer ${getToken()}`,
        },
      },
    );
  }

  return axiosInstance.post(
    url,
    {...formData},
    {
      params,
    },
  );
};

// tslint:disable-next-line: max-line-length
const FILE = (url, formData, params = {}, isToken = false) => {
  if (isToken) {
    return axiosInstance.post(
      url,
      formData,
      {
        params: {
          ...params,
        },
        headers: {
          ...headers(),
          authorization: `Bearer ${getToken()}`,
        },
      },
    );
  }

  return axiosInstance.post(
    url,
    formData,
    {
      params,
    },
  );
};

const PUT = (url, formData, params = {}, isToken = false) => {
  if (isToken) {
    return axiosInstance.put(
      url,
      {...formData},
      {
        params: {
          ...params,
        },
        headers: {
          ...headers(),
          authorization: `Bearer ${getToken()}`,
        },
      },
    );
  }

  return axiosInstance.put(
    url,
    {...formData},
    {
      params,
    },
  );
};

export {
  GET,
  POST,
  PUT,
  DELETE,
  getToken,
  FILE,
  axiosInstance,
};
