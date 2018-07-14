package judgels.jophiel.hibernate;

import java.time.Clock;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import judgels.jophiel.persistence.UserRegistrationEmailDao;
import judgels.jophiel.persistence.UserRegistrationEmailModel;
import judgels.jophiel.persistence.UserRegistrationEmailModel_;
import judgels.persistence.ActorProvider;
import judgels.persistence.hibernate.HibernateDao;
import org.hibernate.SessionFactory;

@Singleton
public class UserRegistrationEmailHibernateDao extends HibernateDao<UserRegistrationEmailModel>
        implements UserRegistrationEmailDao {

    @Inject
    public UserRegistrationEmailHibernateDao(SessionFactory sessionFactory, Clock clock, ActorProvider actorProvider) {
        super(sessionFactory, clock, actorProvider);
    }

    @Override
    public Optional<UserRegistrationEmailModel> selectByUserJid(String userJid) {
        return selectByUniqueColumn(UserRegistrationEmailModel_.userJid, userJid);
    }

    @Override
    public Optional<UserRegistrationEmailModel> selectByEmailCode(String emailCode) {
        return selectByUniqueColumn(UserRegistrationEmailModel_.emailCode, emailCode);
    }
}