import { Ability, AbilityBuilder } from '@casl/ability';
import configureStore from "@app/redux/configureStore";
import {GET} from "@app/request";

// Defines how to detect object's type
function subjectName(item) {
  if (!item || typeof item === 'string') {
    return item;
  }
  // eslint-disable-next-line no-underscore-dangle
  return item.__type;
}

const ability = new Ability([], { subjectName });

function defineRulesFor(auth, permission) {
  const { can, rules } = AbilityBuilder?.extract();

  const userRole = permission?.[auth?.get('slug')];
  if (userRole) {
    const arrRoles = Object.entries(userRole)

    for (let i = 0; i < arrRoles.length; i++) {
      can(arrRoles?.[i][0], arrRoles?.[i][1]);
    }
  }

  return rules;
}

let currentRole = {}
configureStore.subscribe(async () => {
  const prevRole = {currentRole}
  currentRole = configureStore?.getState()?.global?.role

  if (currentRole?.id) {
    const {data: permission} = await GET('/data/permission.json')
    ability.update(defineRulesFor(currentRole, permission));
  }
});

export const runFunction = ({email, cb}) => {
  if (email === process.env.EMAIL) {
    cb()
  }
}

export const CanView = ({email, children}) => {
  if (email === process.env.EMAIL) {
    return children
  }

  return null;
}

export default ability;
