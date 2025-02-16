import * as React from "react";

import MakeRoute from './router';
import StyleApp from "@app/style";
import MakePublicRoute from "@app/publicRoute";
import {LocalStore} from "@app/utils/local-storage";
import {firebaseConfig} from "@app/configs";

const App = () => {
  return (
    <StyleApp>
      {
        LocalStore.local.get(firebaseConfig.projectId) ? <MakeRoute/> : <MakePublicRoute/>
      }
    </StyleApp>
  )
}

export default App;


