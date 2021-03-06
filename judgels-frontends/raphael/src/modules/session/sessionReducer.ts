import { setWith, TypedAction, TypedReducer } from 'redoodle';

import { User } from 'modules/api/jophiel/user';

export interface SessionState {
  isLoggedIn: boolean;
  token?: string;
  user?: User;
}

// Somehow redux-persist won't work after dispatching DelSession if the next state is an empty object...
export const INITIAL_STATE: SessionState = { isLoggedIn: false };

export const PutToken = TypedAction.define('session/PUT_TOKEN')<string>();
export const PutUser = TypedAction.define('session/PUT_USER')<User>();
export const DelSession = TypedAction.defineWithoutPayload('session/DEL')();

const createSessionReducer = () => {
  const builder = TypedReducer.builder<SessionState>();

  builder.withHandler(PutToken.TYPE, (state, payload) => ({
    isLoggedIn: true,
    token: payload,
  }));
  builder.withHandler(PutUser.TYPE, (state, payload) => setWith(state, { user: payload }));
  builder.withHandler(DelSession.TYPE, () => INITIAL_STATE);
  builder.withDefaultHandler(state => (state !== undefined ? state : INITIAL_STATE));

  return builder.build();
};

export const sessionReducer = createSessionReducer();
