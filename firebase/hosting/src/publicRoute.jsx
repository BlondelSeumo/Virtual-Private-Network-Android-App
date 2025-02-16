import * as React from 'react';
import {Route, Switch} from 'react-router-dom';
import {LoadingPage} from '@app/components/core/loading';
import {Redirect} from "react-router";

export const routes = [
  {
    name: 'Login',
    component: React.lazy(() => Promise.all([
      import('@app/modules/auth/components/login'),
      new Promise((resolve) => setTimeout(resolve, 100)),
    ])
      .then(([moduleExports]) => moduleExports)),
    path: '/login',
    id: 'login',
    exact: true,
  },
  {
    name: 'Request password',
    component: React.lazy(() => Promise.all([
      import('@app/modules/auth/components/request-pass'),
      new Promise((resolve) => setTimeout(resolve, 100)),
    ])
      .then(([moduleExports]) => moduleExports)),
    path: '/request-password',
    id: 'request-password',
    exact: true,
  },
  {
    name: 'Reset password',
    component: React.lazy(() => Promise.all([
      import('@app/modules/auth/components/reset-password'),
      new Promise((resolve) => setTimeout(resolve, 100)),
    ])
      .then(([moduleExports]) => moduleExports)),
    path: '/reset-password',
    id: 'reset-password',
    exact: true,
  },
  {
    name: 'Auth',
    component: React.lazy(() => Promise.all([
      import('@app/modules/auth'),
      new Promise((resolve) => setTimeout(resolve, 100)),
    ])
      .then(([moduleExports]) => moduleExports)),
    path: '/auth',
    id: 'auth',
    exact: true,
  },
  {
    name: 'Auth Success',
    component: React.lazy(() => Promise.all([
      import('@app/modules/auth/components/success'),
      new Promise((resolve) => setTimeout(resolve, 100)),
    ])
      .then(([moduleExports]) => moduleExports)),
    path: '/auth/success',
    id: 'auth-success',
    exact: true,
  },
  {
    name: 'Admin',
    component: React.lazy(() => Promise.all([
      import('@app/modules/auth/components/admin'),
      new Promise((resolve) => setTimeout(resolve, 100)),
    ])
      .then(([moduleExports]) => moduleExports)),
    path: '/',
    id: 'auth-admin',
    exact: true,
  },
];

const MakePublicRoute = ({}) => (
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
      <Redirect to={`/`}/>
    </Switch>
  </React.Suspense>
);

export default MakePublicRoute;
