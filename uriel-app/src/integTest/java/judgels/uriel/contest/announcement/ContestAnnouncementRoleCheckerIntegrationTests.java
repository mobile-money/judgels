package judgels.uriel.contest.announcement;

import static judgels.uriel.api.contest.supervisor.SupervisorPermissionType.ANNOUNCEMENT;
import static org.assertj.core.api.Assertions.assertThat;

import judgels.uriel.contest.role.AbstractRoleCheckerIntegrationTests;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContestAnnouncementRoleCheckerIntegrationTests extends AbstractRoleCheckerIntegrationTests {
    private ContestAnnouncementRoleChecker checker;

    @BeforeEach
    void before(SessionFactory sessionFactory) {
        prepare(sessionFactory);
        checker = component.contestAnnouncementRoleChecker();
    }

    @Test
    void view_published_announcements() {
        assertThat(checker.canViewPublishedAnnouncements(ADMIN, contestA)).isTrue();
        assertThat(checker.canViewPublishedAnnouncements(ADMIN, contestB)).isTrue();
        assertThat(checker.canViewPublishedAnnouncements(ADMIN, contestC)).isTrue();

        assertThat(checker.canViewPublishedAnnouncements(USER, contestA)).isTrue();
        assertThat(checker.canViewPublishedAnnouncements(USER, contestB)).isFalse();
        assertThat(checker.canViewPublishedAnnouncements(USER, contestC)).isFalse();

        assertThat(checker.canViewPublishedAnnouncements(CONTESTANT, contestA)).isTrue();
        assertThat(checker.canViewPublishedAnnouncements(CONTESTANT, contestB)).isTrue();
        assertThat(checker.canViewPublishedAnnouncements(CONTESTANT, contestC)).isFalse();

        assertThat(checker.canViewPublishedAnnouncements(SUPERVISOR, contestA)).isTrue();
        assertThat(checker.canViewPublishedAnnouncements(SUPERVISOR, contestB)).isTrue();
        assertThat(checker.canViewPublishedAnnouncements(SUPERVISOR, contestC)).isFalse();
        addSupervisorToContestBWithPermission(ANNOUNCEMENT);
        assertThat(checker.canViewPublishedAnnouncements(SUPERVISOR, contestA)).isTrue();
        assertThat(checker.canViewPublishedAnnouncements(SUPERVISOR, contestB)).isTrue();
        assertThat(checker.canViewPublishedAnnouncements(SUPERVISOR, contestC)).isFalse();

        assertThat(checker.canViewPublishedAnnouncements(MANAGER, contestA)).isTrue();
        assertThat(checker.canViewPublishedAnnouncements(MANAGER, contestB)).isTrue();
        assertThat(checker.canViewPublishedAnnouncements(MANAGER, contestC)).isFalse();
    }
}