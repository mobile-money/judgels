package judgels.uriel.api.contest.web;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableContestWebConfig.class)
public interface ContestWebConfig {
    Set<ContestTab> getVisibleTabs();
    ContestState getContestState();
    Optional<Duration> getRemainingContestStateDuration();
    long getAnnouncementsCount();
    long getAnsweredClarificationsCount();

    class Builder extends ImmutableContestWebConfig.Builder {}
}