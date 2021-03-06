package judgels.uriel.api.contest.scoreboard;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableContestScoreboardConfig.class)
public interface ContestScoreboardConfig {
    boolean getCanViewOfficialAndFrozen();
    boolean getCanViewClosedProblems();

    class Builder extends ImmutableContestScoreboardConfig.Builder {}
}
