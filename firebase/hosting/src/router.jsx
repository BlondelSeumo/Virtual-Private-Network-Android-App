import * as React from 'react';
import {Redirect, Route, Switch} from 'react-router-dom';
import {LoadingPage} from '@app/components/core/loading';
import DefaultMain from "@app/components/default-main";

export const routes = [
  {
    name: 'Home',
    component: React.lazy(() => Promise.all([
      import('@app/modules/home'),
      new Promise((resolve) => setTimeout(resolve, 100)),
    ])
      .then(([moduleExports]) => moduleExports)),
    path: '/',
    id: 'home',
    exact: true,
    private: true
  },
  {
    name: 'Server',
    component: React.lazy(() => Promise.all([
      import('@app/modules/server'),
      new Promise((resolve) => setTimeout(resolve, 100)),
    ])
      .then(([moduleExports]) => moduleExports)),
    path: '/server',
    id: 'server',
    exact: true,
    private: true
  },
  {
    name: 'User Detail',
    component: React.lazy(() => Promise.all([
      import('@app/modules/user/component/detail'),
      new Promise((resolve) => setTimeout(resolve, 100)),
    ])
      .then(([moduleExports]) => moduleExports)),
    path: '/users/:id/:type',
    id: 'users-detail',
    exact: true,
  },
  {
    name: 'User',
    component: React.lazy(() => Promise.all([
      import('@app/modules/user'),
      new Promise((resolve) => setTimeout(resolve, 100)),
    ])
      .then(([moduleExports]) => moduleExports)),
    path: '/users',
    id: 'users',
    exact: true,
    private: true
  },
  {
    name: 'My Profile',
    component: React.lazy(() => Promise.all([
      import('@app/modules/user/component/profile'),
      new Promise((resolve) => setTimeout(resolve, 100)),
    ])
      .then(([moduleExports]) => moduleExports)),
    path: '/profile',
    id: 'profile',
    exact: true,
    private: true
  },
  {
    name: 'Ads',
    component: React.lazy(() => Promise.all([
      import('@app/modules/ad'),
      new Promise((resolve) => setTimeout(resolve, 100)),
    ])
      .then(([moduleExports]) => moduleExports)),
    path: '/ads',
    id: 'ads',
    exact: true,
    private: true
  },
  {
    name: 'Packages',
    component: React.lazy(() => Promise.all([
      import('@app/modules/package'),
      new Promise((resolve) => setTimeout(resolve, 100)),
    ])
      .then(([moduleExports]) => moduleExports)),
    path: '/packs',
    id: 'packages',
    exact: true,
    private: true
  },
];

const MakeRoute = () => (
  <DefaultMain>
    <React.Suspense fallback={<LoadingPage/>}>
      <Switch>
        {
          routes.map((route) => (
            <Route
              exact={route.exact || false}
              path={route.path}
              key={route.id}
              render={(props) => {
                return <route.component {...props}/>
              }}
            />
          ))
        }
        ))
        <Route render={() => (
          <div>Not Found</div>
        )}/>
        {/*<Redirect to="/"/>*/}
      </Switch>
    </React.Suspense>
  </DefaultMain>
);

export default MakeRoute;
