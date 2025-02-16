import '@babel/polyfill';

import { ConnectedRouter } from 'connected-react-router';
import React from 'react';
import { ConfigProvider } from 'antd';
import ReactDOM from 'react-dom';
import {Provider} from 'react-redux';
import {I18nextProvider} from 'react-i18next';
import App from '@app/app'
import i18n from './services/language';
import configureStore from './redux/configureStore';
import history from '@app/utils/history';

const MOUNT_NODE = document.getElementById('root-app');

import '@app/components/core/css/antd.less';
import '@app/components/core/css/core.scss';

import vi from 'antd/es/locale/en_US'

const render = () => {
  ReactDOM.render(
    <Provider store={configureStore}>
      <ConnectedRouter history={history}>
        <ConfigProvider locale={vi}>
          <I18nextProvider i18n={i18n}>
            <App/>
          </I18nextProvider>
        </ConfigProvider>
      </ConnectedRouter>
    </Provider>,
    MOUNT_NODE,
  );
};

if (module.hot) {
  module.hot.accept(['./app'], () => {
    ReactDOM.unmountComponentAtNode(MOUNT_NODE);
    render();
  });
}

render();

if (process.env.NODE_ENV === 'stg') {
  require('offline-plugin/runtime').install(); // eslint-disable-line global-require
}
