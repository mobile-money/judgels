package judgels.jophiel.user;

import static judgels.service.ServiceUtils.checkAllowed;
import static judgels.service.ServiceUtils.checkFound;

import io.dropwizard.hibernate.UnitOfWork;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import judgels.jophiel.api.user.User;
import judgels.jophiel.api.user.UserData;
import judgels.jophiel.api.user.UserService;
import judgels.jophiel.role.RoleChecker;
import judgels.jophiel.session.SessionStore;
import judgels.persistence.api.OrderDir;
import judgels.persistence.api.Page;
import judgels.service.actor.ActorChecker;
import judgels.service.api.actor.AuthHeader;

public class UserResource implements UserService {
    private final ActorChecker actorChecker;
    private final RoleChecker roleChecker;
    private final UserStore userStore;
    private final SessionStore sessionStore;

    @Inject
    public UserResource(
            ActorChecker actorChecker,
            RoleChecker roleChecker,
            UserStore userStore,
            SessionStore sessionStore) {

        this.actorChecker = actorChecker;
        this.roleChecker = roleChecker;
        this.userStore = userStore;
        this.sessionStore = sessionStore;
    }

    @Override
    @UnitOfWork(readOnly = true)
    public User getUser(AuthHeader authHeader, String userJid) {
        String actorJid = actorChecker.check(authHeader);
        checkAllowed(roleChecker.canViewUser(actorJid, userJid));

        return checkFound(userStore.getUserByJid(userJid));
    }

    @Override
    @UnitOfWork(readOnly = true)
    public Page<User> getUsers(
            AuthHeader authHeader,
            Optional<Integer> page,
            Optional<String> orderBy,
            Optional<OrderDir> orderDir) {

        String actorJid = actorChecker.check(authHeader);
        checkAllowed(roleChecker.canViewUserList(actorJid));

        return userStore.getUsers(page, orderBy, orderDir);
    }

    @Override
    @UnitOfWork
    public User createUser(AuthHeader authHeader, UserData data) {
        String actorJid = actorChecker.check(authHeader);
        checkAllowed(roleChecker.canCreateUser(actorJid));

        return userStore.createUser(data);
    }

    @Override
    @UnitOfWork
    public void updateUserPasswords(AuthHeader authHeader, Map<String, String> jidToPasswordMap) {
        String actorJid = actorChecker.check(authHeader);
        checkAllowed(roleChecker.canUpdateUserList(actorJid));
        jidToPasswordMap.forEach(userStore::updateUserPassword);
        jidToPasswordMap.keySet().forEach(sessionStore::deleteSessionsByUserJid);
    }
}
