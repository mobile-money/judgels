package judgels.uriel.contest.scoreboard.icpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import judgels.gabriel.api.Verdicts;
import judgels.sandalphon.api.submission.programming.Grading;
import judgels.sandalphon.api.submission.programming.Submission;
import judgels.uriel.api.contest.Contest;
import judgels.uriel.api.contest.ContestStyle;
import judgels.uriel.api.contest.module.IcpcStyleModuleConfig;
import judgels.uriel.api.contest.module.StyleModuleConfig;
import judgels.uriel.api.contest.scoreboard.IcpcScoreboard;
import judgels.uriel.api.contest.scoreboard.IcpcScoreboard.IcpcScoreboardContent;
import judgels.uriel.api.contest.scoreboard.IcpcScoreboard.IcpcScoreboardEntry;
import judgels.uriel.api.contest.scoreboard.IcpcScoreboard.IcpcScoreboardProblemState;
import judgels.uriel.api.contest.scoreboard.ScoreboardState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class IcpcScoreboardProcessorTests {
    @Mock private ObjectMapper mapper;
    private IcpcScoreboardProcessor scoreboardProcessor = new IcpcScoreboardProcessor();

    @BeforeEach
    void before() {
        initMocks(this);
    }


    @Test
    void test_pagination() {
        ScoreboardState state = new ScoreboardState.Builder()
                .addContestantJids("c1", "c2")
                .addProblemJids("p1", "p2")
                .addProblemAliases("A", "B")
                .build();

        List<IcpcScoreboard.IcpcScoreboardEntry> fakeEntries = new ArrayList<>(134);
        for (int i = 0; i < 134; i++) {
            fakeEntries.add(mock(IcpcScoreboard.IcpcScoreboardEntry.class));
        }

        IcpcScoreboard icpcScoreboard = new IcpcScoreboard.Builder()
                .state(state)
                .content(new IcpcScoreboardContent.Builder()
                        .entries(fakeEntries)
                        .build())
                .build();

        IcpcScoreboard pagedScoreboard = (IcpcScoreboard) scoreboardProcessor.paginate(icpcScoreboard, 1, 50);
        assertThat(pagedScoreboard.getContent().getEntries()).isEqualTo(fakeEntries.subList(0, 50));

        pagedScoreboard = (IcpcScoreboard) scoreboardProcessor.paginate(icpcScoreboard, 2, 50);
        assertThat(pagedScoreboard.getContent().getEntries()).isEqualTo(fakeEntries.subList(50, 100));

        pagedScoreboard = (IcpcScoreboard) scoreboardProcessor.paginate(icpcScoreboard, 3, 50);
        assertThat(pagedScoreboard.getContent().getEntries()).isEqualTo(fakeEntries.subList(100, 134));
    }

    @Nested
    class ComputeToString {
        private ScoreboardState state = new ScoreboardState.Builder()
                .addContestantJids("c1", "c2")
                .addProblemJids("p1", "p2")
                .addProblemAliases("A", "B")
                .build();

        private Contest contest = new Contest.Builder()
                .beginTime(Instant.ofEpochSecond(60))
                .duration(Duration.ofMinutes(100))
                .id(1)
                .jid("JIDC")
                .name("contest-name")
                .slug("contest-slug")
                .style(ContestStyle.ICPC)
                .build();

        private StyleModuleConfig styleModuleConfig = new IcpcStyleModuleConfig.Builder()
                .wrongSubmissionPenalty(1000)
                .build();

        private Map<String, Optional<Instant>> contestantStartTimesMap = ImmutableMap.of(
                "c1", Optional.empty(),
                "c2", Optional.of(Instant.ofEpochSecond(300)),
                "c3", Optional.empty()
        );

        @BeforeEach
        void before() throws JsonProcessingException {
            when(mapper.writeValueAsString(any())).thenReturn("scoreboard-string");
        }

        @Test
        void show_only_contestant() throws JsonProcessingException {
            List<Submission> submissions = ImmutableList.of(
                    new Submission.Builder()
                            .containerJid("JIDC")
                            .id(1)
                            .jid("JIDS-1")
                            .gradingEngine("ENG")
                            .gradingLanguage("ASM")
                            .time(Instant.ofEpochSecond(300))
                            .userJid("c3")
                            .problemJid("p1")
                            .latestGrading(new Grading.Builder()
                                    .id(1)
                                    .jid("JIDG-2")
                                    .score(0)
                                    .verdict(Verdicts.TIME_LIMIT_EXCEEDED)
                                    .build())
                            .build());

            scoreboardProcessor.computeToString(
                    mapper,
                    state,
                    contest,
                    styleModuleConfig,
                    contestantStartTimesMap,
                    submissions,
                    ImmutableList.of(),
                    Optional.empty());

            verify(mapper).writeValueAsString(new IcpcScoreboard.Builder()
                    .state(state)
                    .content(new IcpcScoreboardContent.Builder()
                            .addEntries(new IcpcScoreboardEntry.Builder()
                                    .rank(1)
                                    .contestantJid("c1")
                                    .totalAccepted(0)
                                    .totalPenalties(0)
                                    .lastAcceptedPenalty(0)
                                    .addAttemptsList(0, 0)
                                    .addPenaltyList(0, 0)
                                    .addProblemStateList(
                                            IcpcScoreboardProblemState.NOT_ACCEPTED,
                                            IcpcScoreboardProblemState.NOT_ACCEPTED
                                    )
                                    .build())
                            .addEntries(new IcpcScoreboardEntry.Builder()
                                    .rank(1)
                                    .contestantJid("c2")
                                    .totalAccepted(0)
                                    .totalPenalties(0)
                                    .lastAcceptedPenalty(0)
                                    .addAttemptsList(0, 0)
                                    .addPenaltyList(0, 0)
                                    .addProblemStateList(
                                            IcpcScoreboardProblemState.NOT_ACCEPTED,
                                            IcpcScoreboardProblemState.NOT_ACCEPTED
                                    )
                                    .build())
                            .build())
                    .build());
        }

        @Test
        void show_only_contest_problem() throws JsonProcessingException {
            List<Submission> submissions = ImmutableList.of(
                    new Submission.Builder()
                            .containerJid("JIDC")
                            .id(1)
                            .jid("JIDS-1")
                            .gradingEngine("ENG")
                            .gradingLanguage("ASM")
                            .time(Instant.ofEpochSecond(300))
                            .userJid("c1")
                            .problemJid("p4")
                            .latestGrading(new Grading.Builder()
                                    .id(1)
                                    .jid("JIDG-2")
                                    .score(0)
                                    .verdict(Verdicts.TIME_LIMIT_EXCEEDED)
                                    .build())
                            .build());

            scoreboardProcessor.computeToString(
                    mapper,
                    state,
                    contest,
                    styleModuleConfig,
                    contestantStartTimesMap,
                    submissions,
                    ImmutableList.of(),
                    Optional.empty());

            verify(mapper).writeValueAsString(new IcpcScoreboard.Builder()
                    .state(state)
                    .content(new IcpcScoreboardContent.Builder()
                            .addEntries(new IcpcScoreboardEntry.Builder()
                                    .rank(1)
                                    .contestantJid("c1")
                                    .totalAccepted(0)
                                    .totalPenalties(0)
                                    .lastAcceptedPenalty(0)
                                    .addAttemptsList(0, 0)
                                    .addPenaltyList(0, 0)
                                    .addProblemStateList(
                                            IcpcScoreboardProblemState.NOT_ACCEPTED,
                                            IcpcScoreboardProblemState.NOT_ACCEPTED
                                    )
                                    .build())
                            .addEntries(new IcpcScoreboardEntry.Builder()
                                    .rank(1)
                                    .contestantJid("c2")
                                    .totalAccepted(0)
                                    .totalPenalties(0)
                                    .lastAcceptedPenalty(0)
                                    .addAttemptsList(0, 0)
                                    .addPenaltyList(0, 0)
                                    .addProblemStateList(
                                            IcpcScoreboardProblemState.NOT_ACCEPTED,
                                            IcpcScoreboardProblemState.NOT_ACCEPTED
                                    )
                                    .build())
                            .build())
                    .build());
        }

        @Test
        void ignore_submission_with_no_grade() throws JsonProcessingException {
            List<Submission> submissions = ImmutableList.of(
                    new Submission.Builder()
                            .containerJid("JIDC")
                            .id(1)
                            .jid("JIDS-1")
                            .gradingEngine("ENG")
                            .gradingLanguage("ASM")
                            .time(Instant.ofEpochSecond(300))
                            .userJid("c1")
                            .problemJid("p1")
                            .build(),
                    new Submission.Builder()
                            .containerJid("JIDC")
                            .id(2)
                            .jid("JIDS-1")
                            .gradingEngine("ENG")
                            .gradingLanguage("ASM")
                            .time(Instant.ofEpochSecond(600))
                            .userJid("c1")
                            .problemJid("p1")
                            .latestGrading(new Grading.Builder()
                                    .id(1)
                                    .jid("JIDG-2")
                                    .score(100)
                                    .verdict(Verdicts.ACCEPTED)
                                    .build())
                            .build(),
                    new Submission.Builder()
                            .containerJid("JIDC")
                            .id(3)
                            .jid("JIDS-1")
                            .gradingEngine("ENG")
                            .gradingLanguage("ASM")
                            .time(Instant.ofEpochSecond(620))
                            .userJid("c2")
                            .problemJid("p2")
                            .build());

            scoreboardProcessor.computeToString(
                    mapper,
                    state,
                    contest,
                    styleModuleConfig,
                    contestantStartTimesMap,
                    submissions,
                    ImmutableList.of(),
                    Optional.empty());

            verify(mapper).writeValueAsString(new IcpcScoreboard.Builder()
                    .state(state)
                    .content(new IcpcScoreboardContent.Builder()
                            .addEntries(new IcpcScoreboardEntry.Builder()
                                    .rank(1)
                                    .contestantJid("c1")
                                    .totalAccepted(1)
                                    .totalPenalties(9)
                                    .lastAcceptedPenalty(540000)
                                    .addAttemptsList(1, 0)
                                    .addPenaltyList(9, 0)
                                    .addProblemStateList(
                                            IcpcScoreboardProblemState.FIRST_ACCEPTED,
                                            IcpcScoreboardProblemState.NOT_ACCEPTED
                                    )
                                    .build())
                            .addEntries(new IcpcScoreboardEntry.Builder()
                                    .rank(2)
                                    .contestantJid("c2")
                                    .totalAccepted(0)
                                    .totalPenalties(0)
                                    .lastAcceptedPenalty(0)
                                    .addAttemptsList(0, 0)
                                    .addPenaltyList(0, 0)
                                    .addProblemStateList(
                                            IcpcScoreboardProblemState.NOT_ACCEPTED,
                                            IcpcScoreboardProblemState.NOT_ACCEPTED
                                    )
                                    .build())
                            .build())
                    .build());
        }

        @Test
        void time_calculation() throws JsonProcessingException {
            List<Submission> submissions = ImmutableList.of(
                    new Submission.Builder()
                            .containerJid("JIDC")
                            .id(1)
                            .jid("JIDS-1")
                            .gradingEngine("ENG")
                            .gradingLanguage("ASM")
                            .time(Instant.ofEpochSecond(300))
                            .userJid("c1")
                            .problemJid("p1")
                            .latestGrading(new Grading.Builder()
                                    .id(1)
                                    .jid("JIDG-2")
                                    .score(100)
                                    .verdict(Verdicts.ACCEPTED)
                                    .build())
                            .build(),
                    new Submission.Builder()
                            .containerJid("JIDC")
                            .id(2)
                            .jid("JIDS-2")
                            .gradingEngine("ENG")
                            .gradingLanguage("ASM")
                            .time(Instant.ofEpochSecond(360))
                            .userJid("c2")
                            .problemJid("p2")
                            .latestGrading(new Grading.Builder()
                                    .id(3)
                                    .jid("JIDG-4")
                                    .score(100)
                                    .verdict(Verdicts.ACCEPTED)
                                    .build())
                            .build(),
                    new Submission.Builder()
                            .containerJid("JIDC")
                            .id(3)
                            .jid("JIDS-3")
                            .gradingEngine("ENG")
                            .gradingLanguage("ASM")
                            .time(Instant.ofEpochSecond(400))
                            .userJid("c1")
                            .problemJid("p2")
                            .latestGrading(new Grading.Builder()
                                    .id(2)
                                    .jid("JIDG-3")
                                    .score(0)
                                    .verdict(Verdicts.TIME_LIMIT_EXCEEDED)
                                    .build())
                            .build(),
                    new Submission.Builder()
                            .containerJid("JIDC")
                            .id(4)
                            .jid("JIDS-4")
                            .gradingEngine("ENG")
                            .gradingLanguage("ASM")
                            .time(Instant.ofEpochSecond(410))
                            .userJid("c1")
                            .problemJid("p2")
                            .latestGrading(new Grading.Builder()
                                    .id(2)
                                    .jid("JIDG-3")
                                    .score(100)
                                    .verdict(Verdicts.ACCEPTED)
                                    .build())
                            .build(),
                    new Submission.Builder()
                            .containerJid("JIDC")
                            .id(5)
                            .jid("JIDS-5")
                            .gradingEngine("ENG")
                            .gradingLanguage("ASM")
                            .time(Instant.ofEpochSecond(900))
                            .userJid("c2")
                            .problemJid("p1")
                            .latestGrading(new Grading.Builder()
                                    .id(1)
                                    .jid("JIDG-1")
                                    .score(100)
                                    .verdict(Verdicts.ACCEPTED)
                                    .build())
                            .build()
            );

            scoreboardProcessor.computeToString(
                    mapper,
                    state,
                    contest,
                    styleModuleConfig,
                    contestantStartTimesMap,
                    submissions,
                    ImmutableList.of(),
                    Optional.empty());

            verify(mapper).writeValueAsString(new IcpcScoreboard.Builder()
                    .state(state)
                    .content(new IcpcScoreboardContent.Builder()
                            .addEntries(new IcpcScoreboardEntry.Builder()
                                    .rank(1)
                                    .contestantJid("c2")
                                    .totalAccepted(2)
                                    .totalPenalties(11)
                                    .lastAcceptedPenalty(600000)
                                    .addAttemptsList(1, 1)
                                    .addPenaltyList(10, 1)
                                    .addProblemStateList(
                                            IcpcScoreboardProblemState.ACCEPTED,
                                            IcpcScoreboardProblemState.FIRST_ACCEPTED
                                    )
                                    .build())
                            .addEntries(new IcpcScoreboardEntry.Builder()
                                    .rank(2)
                                    .contestantJid("c1")
                                    .totalAccepted(2)
                                    .totalPenalties(1010)
                                    .lastAcceptedPenalty(350000)
                                    .addAttemptsList(1, 2)
                                    .addPenaltyList(4, 6)
                                    .addProblemStateList(
                                            IcpcScoreboardProblemState.FIRST_ACCEPTED,
                                            IcpcScoreboardProblemState.ACCEPTED
                                    )
                                    .build())
                            .build())
                    .build());
        }

        @Nested
        class ProblemOrdering {
            private List<Submission> submissions = ImmutableList.of(
                    new Submission.Builder()
                            .containerJid("JIDC")
                            .id(1)
                            .jid("JIDS-1")
                            .gradingEngine("ENG")
                            .gradingLanguage("ASM")
                            .time(Instant.ofEpochSecond(900))
                            .userJid("c2")
                            .problemJid("p1")
                            .latestGrading(new Grading.Builder()
                                    .id(1)
                                    .jid("JIDG-1")
                                    .score(100)
                                    .verdict(Verdicts.ACCEPTED)
                                    .build())
                            .build(),
                    new Submission.Builder()
                            .containerJid("JIDC")
                            .id(2)
                            .jid("JIDS-2")
                            .gradingEngine("ENG")
                            .gradingLanguage("ASM")
                            .time(Instant.ofEpochSecond(300))
                            .userJid("c1")
                            .problemJid("p2")
                            .latestGrading(new Grading.Builder()
                                    .id(1)
                                    .jid("JIDG-2")
                                    .score(100)
                                    .verdict(Verdicts.ACCEPTED)
                                    .build())
                            .build()
            );

            @Test
            void base_case() throws JsonProcessingException {
                scoreboardProcessor.computeToString(
                        mapper,
                        state,
                        contest,
                        styleModuleConfig,
                        contestantStartTimesMap,
                        submissions,
                        ImmutableList.of(),
                        Optional.empty());

                verify(mapper).writeValueAsString(new IcpcScoreboard.Builder()
                        .state(state)
                        .content(new IcpcScoreboardContent.Builder()
                                .addEntries(new IcpcScoreboardEntry.Builder()
                                        .rank(1)
                                        .contestantJid("c1")
                                        .totalAccepted(1)
                                        .totalPenalties(4)
                                        .lastAcceptedPenalty(240000)
                                        .addAttemptsList(0, 1)
                                        .addPenaltyList(0, 4)
                                        .addProblemStateList(
                                                IcpcScoreboardProblemState.NOT_ACCEPTED,
                                                IcpcScoreboardProblemState.FIRST_ACCEPTED
                                        )
                                        .build())
                                .addEntries(new IcpcScoreboardEntry.Builder()
                                        .rank(2)
                                        .contestantJid("c2")
                                        .totalAccepted(1)
                                        .totalPenalties(10)
                                        .lastAcceptedPenalty(600000)
                                        .addAttemptsList(1, 0)
                                        .addPenaltyList(10, 0)
                                        .addProblemStateList(
                                                IcpcScoreboardProblemState.FIRST_ACCEPTED,
                                                IcpcScoreboardProblemState.NOT_ACCEPTED
                                        )
                                        .build())
                                .build())
                        .build());
            }

            @Test
            void reversed_case() throws JsonProcessingException {
                state = new ScoreboardState.Builder()
                        .addContestantJids("c1", "c2")
                        .addProblemJids("p2", "p1")
                        .addProblemAliases("B", "A")
                        .build();

                scoreboardProcessor.computeToString(
                        mapper,
                        state,
                        contest,
                        styleModuleConfig,
                        contestantStartTimesMap,
                        submissions,
                        ImmutableList.of(),
                        Optional.empty());

                verify(mapper).writeValueAsString(new IcpcScoreboard.Builder()
                        .state(state)
                        .content(new IcpcScoreboardContent.Builder()
                                .addEntries(new IcpcScoreboardEntry.Builder()
                                        .rank(1)
                                        .contestantJid("c1")
                                        .totalAccepted(1)
                                        .totalPenalties(4)
                                        .lastAcceptedPenalty(240000)
                                        .addAttemptsList(1, 0)
                                        .addPenaltyList(4, 0)
                                        .addProblemStateList(
                                                IcpcScoreboardProblemState.FIRST_ACCEPTED,
                                                IcpcScoreboardProblemState.NOT_ACCEPTED
                                        )
                                        .build())
                                .addEntries(new IcpcScoreboardEntry.Builder()
                                        .rank(2)
                                        .contestantJid("c2")
                                        .totalAccepted(1)
                                        .totalPenalties(10)
                                        .lastAcceptedPenalty(600000)
                                        .addAttemptsList(0, 1)
                                        .addPenaltyList(0, 10)
                                        .addProblemStateList(
                                                IcpcScoreboardProblemState.NOT_ACCEPTED,
                                                IcpcScoreboardProblemState.FIRST_ACCEPTED
                                        )
                                        .build())
                                .build())
                        .build());
            }
        }

        @Nested
        class Sorting {
            @Test
            void solve_over_penalty() throws JsonProcessingException {
                List<Submission> submissions = ImmutableList.of(
                        new Submission.Builder()
                                .containerJid("JIDC")
                                .id(1)
                                .jid("JIDS-2")
                                .gradingEngine("ENG")
                                .gradingLanguage("ASM")
                                .time(Instant.ofEpochSecond(300))
                                .userJid("c1")
                                .problemJid("p1")
                                .latestGrading(new Grading.Builder()
                                        .id(1)
                                        .jid("JIDG-2")
                                        .score(100)
                                        .verdict(Verdicts.ACCEPTED)
                                        .build())
                                .build(),
                        new Submission.Builder()
                                .containerJid("JIDC")
                                .id(2)
                                .jid("JIDS-4")
                                .gradingEngine("ENG")
                                .gradingLanguage("ASM")
                                .time(Instant.ofEpochSecond(360))
                                .userJid("c2")
                                .problemJid("p2")
                                .latestGrading(new Grading.Builder()
                                        .id(3)
                                        .jid("JIDG-4")
                                        .score(100)
                                        .verdict(Verdicts.ACCEPTED)
                                        .build())
                                .build(),
                        new Submission.Builder()
                                .containerJid("JIDC")
                                .id(3)
                                .jid("JIDS-1")
                                .gradingEngine("ENG")
                                .gradingLanguage("ASM")
                                .time(Instant.ofEpochSecond(900))
                                .userJid("c2")
                                .problemJid("p1")
                                .latestGrading(new Grading.Builder()
                                        .id(1)
                                        .jid("JIDG-1")
                                        .score(100)
                                        .verdict(Verdicts.ACCEPTED)
                                        .build())
                                .build()
                );

                scoreboardProcessor.computeToString(
                        mapper,
                        state,
                        contest,
                        styleModuleConfig,
                        contestantStartTimesMap,
                        submissions,
                        ImmutableList.of(),
                        Optional.empty());

                verify(mapper).writeValueAsString(new IcpcScoreboard.Builder()
                        .state(state)
                        .content(new IcpcScoreboardContent.Builder()
                                .addEntries(new IcpcScoreboardEntry.Builder()
                                        .rank(1)
                                        .contestantJid("c2")
                                        .totalAccepted(2)
                                        .totalPenalties(11)
                                        .lastAcceptedPenalty(600000)
                                        .addAttemptsList(1, 1)
                                        .addPenaltyList(10, 1)
                                        .addProblemStateList(
                                                IcpcScoreboardProblemState.ACCEPTED,
                                                IcpcScoreboardProblemState.FIRST_ACCEPTED
                                        )
                                        .build())
                                .addEntries(new IcpcScoreboardEntry.Builder()
                                        .rank(2)
                                        .contestantJid("c1")
                                        .totalAccepted(1)
                                        .totalPenalties(4)
                                        .lastAcceptedPenalty(240000)
                                        .addAttemptsList(1, 0)
                                        .addPenaltyList(4, 0)
                                        .addProblemStateList(
                                                IcpcScoreboardProblemState.FIRST_ACCEPTED,
                                                IcpcScoreboardProblemState.NOT_ACCEPTED
                                        )
                                        .build())
                                .build())
                        .build());
            }

            @Test
            void penalty_as_tiebreaker() throws JsonProcessingException {
                List<Submission> submissions = ImmutableList.of(
                        new Submission.Builder()
                                .containerJid("JIDC")
                                .id(1)
                                .jid("JIDS-1")
                                .gradingEngine("ENG")
                                .gradingLanguage("ASM")
                                .time(Instant.ofEpochSecond(900))
                                .userJid("c2")
                                .problemJid("p1")
                                .latestGrading(new Grading.Builder()
                                        .id(1)
                                        .jid("JIDG-1")
                                        .score(100)
                                        .verdict(Verdicts.ACCEPTED)
                                        .build())
                                .build(),
                        new Submission.Builder()
                                .containerJid("JIDC")
                                .id(2)
                                .jid("JIDS-2")
                                .gradingEngine("ENG")
                                .gradingLanguage("ASM")
                                .time(Instant.ofEpochSecond(900))
                                .userJid("c1")
                                .problemJid("p1")
                                .latestGrading(new Grading.Builder()
                                        .id(1)
                                        .jid("JIDG-2")
                                        .score(100)
                                        .verdict(Verdicts.ACCEPTED)
                                        .build())
                                .build()
                );

                scoreboardProcessor.computeToString(
                        mapper,
                        state,
                        contest,
                        styleModuleConfig,
                        contestantStartTimesMap,
                        submissions,
                        ImmutableList.of(),
                        Optional.empty());

                verify(mapper).writeValueAsString(new IcpcScoreboard.Builder()
                        .state(state)
                        .content(new IcpcScoreboardContent.Builder()
                                .addEntries(new IcpcScoreboardEntry.Builder()
                                        .rank(1)
                                        .contestantJid("c2")
                                        .totalAccepted(1)
                                        .totalPenalties(10)
                                        .lastAcceptedPenalty(600000)
                                        .addAttemptsList(1, 0)
                                        .addPenaltyList(10, 0)
                                        .addProblemStateList(
                                                IcpcScoreboardProblemState.FIRST_ACCEPTED,
                                                IcpcScoreboardProblemState.NOT_ACCEPTED
                                        )
                                        .build())
                                .addEntries(new IcpcScoreboardEntry.Builder()
                                        .rank(2)
                                        .contestantJid("c1")
                                        .totalAccepted(1)
                                        .totalPenalties(14)
                                        .lastAcceptedPenalty(840000)
                                        .addAttemptsList(1, 0)
                                        .addPenaltyList(14, 0)
                                        .addProblemStateList(
                                                IcpcScoreboardProblemState.ACCEPTED,
                                                IcpcScoreboardProblemState.NOT_ACCEPTED
                                        )
                                        .build())
                                .build())
                        .build());
            }

            @Test
            void same_rank_if_equal() throws JsonProcessingException {
                state = new ScoreboardState.Builder()
                        .addContestantJids("c1", "c2", "c3")
                        .addProblemJids("p1", "p2")
                        .addProblemAliases("A", "B")
                        .build();

                List<Submission> submissions = ImmutableList.of(
                        new Submission.Builder()
                                .containerJid("JIDC")
                                .id(1)
                                .jid("JIDS-2")
                                .gradingEngine("ENG")
                                .gradingLanguage("ASM")
                                .time(Instant.ofEpochSecond(660))
                                .userJid("c1")
                                .problemJid("p1")
                                .latestGrading(new Grading.Builder()
                                        .id(1)
                                        .jid("JIDG-2")
                                        .score(100)
                                        .verdict(Verdicts.ACCEPTED)
                                        .build())
                                .build(),
                        new Submission.Builder()
                                .containerJid("JIDC")
                                .id(2)
                                .jid("JIDS-1")
                                .gradingEngine("ENG")
                                .gradingLanguage("ASM")
                                .time(Instant.ofEpochSecond(900))
                                .userJid("c2")
                                .problemJid("p1")
                                .latestGrading(new Grading.Builder()
                                        .id(1)
                                        .jid("JIDG-1")
                                        .score(100)
                                        .verdict(Verdicts.ACCEPTED)
                                        .build())
                                .build(),
                        new Submission.Builder()
                                .containerJid("JIDC")
                                .id(3)
                                .jid("JIDS-3")
                                .gradingEngine("ENG")
                                .gradingLanguage("ASM")
                                .time(Instant.ofEpochSecond(900))
                                .userJid("c3")
                                .problemJid("p1")
                                .latestGrading(new Grading.Builder()
                                        .id(1)
                                        .jid("JIDG-1")
                                        .score(100)
                                        .verdict(Verdicts.ACCEPTED)
                                        .build())
                                .build()
                );

                scoreboardProcessor.computeToString(
                        mapper,
                        state,
                        contest,
                        styleModuleConfig,
                        contestantStartTimesMap,
                        submissions,
                        ImmutableList.of(),
                        Optional.empty());

                verify(mapper).writeValueAsString(new IcpcScoreboard.Builder()
                        .state(state)
                        .content(new IcpcScoreboardContent.Builder()
                                .addEntries(new IcpcScoreboardEntry.Builder()
                                        .rank(1)
                                        .contestantJid("c1")
                                        .totalAccepted(1)
                                        .totalPenalties(10)
                                        .lastAcceptedPenalty(600000)
                                        .addAttemptsList(1, 0)
                                        .addPenaltyList(10, 0)
                                        .addProblemStateList(
                                                IcpcScoreboardProblemState.FIRST_ACCEPTED,
                                                IcpcScoreboardProblemState.NOT_ACCEPTED
                                        )
                                        .build())
                                .addEntries(new IcpcScoreboardEntry.Builder()
                                        .rank(1)
                                        .contestantJid("c2")
                                        .totalAccepted(1)
                                        .totalPenalties(10)
                                        .lastAcceptedPenalty(600000)
                                        .addAttemptsList(1, 0)
                                        .addPenaltyList(10, 0)
                                        .addProblemStateList(
                                                IcpcScoreboardProblemState.ACCEPTED,
                                                IcpcScoreboardProblemState.NOT_ACCEPTED
                                        )
                                        .build())
                                .addEntries(new IcpcScoreboardEntry.Builder()
                                        .rank(3)
                                        .contestantJid("c3")
                                        .totalAccepted(1)
                                        .totalPenalties(14)
                                        .lastAcceptedPenalty(840000)
                                        .addAttemptsList(1, 0)
                                        .addPenaltyList(14, 0)
                                        .addProblemStateList(
                                                IcpcScoreboardProblemState.ACCEPTED,
                                                IcpcScoreboardProblemState.NOT_ACCEPTED
                                        )
                                        .build())
                                .build())
                        .build());
            }
        }

        @Nested
        class PendingAfterFreeze {
            private Optional<Instant> freezeTime = Optional.of(Instant.ofEpochSecond(500));

            private List<Submission> baseSubmissions = ImmutableList.of(
                    new Submission.Builder()
                            .containerJid("JIDC")
                            .id(1)
                            .jid("JIDS-1")
                            .gradingEngine("ENG")
                            .gradingLanguage("ASM")
                            .time(Instant.ofEpochSecond(100))
                            .userJid("c1")
                            .problemJid("p1")
                            .latestGrading(new Grading.Builder()
                                    .id(1)
                                    .jid("JIDG-2")
                                    .score(100)
                                    .verdict(Verdicts.ACCEPTED)
                                    .build())
                            .build(),
                    new Submission.Builder()
                            .containerJid("JIDC")
                            .id(2)
                            .jid("JIDS-2")
                            .gradingEngine("ENG")
                            .gradingLanguage("ASM")
                            .time(Instant.ofEpochSecond(400))
                            .userJid("c2")
                            .problemJid("p2")
                            .latestGrading(new Grading.Builder()
                                    .id(1)
                                    .jid("JIDG-2")
                                    .score(100)
                                    .verdict(Verdicts.ACCEPTED)
                                    .build())
                            .build(),
                    new Submission.Builder()
                            .containerJid("JIDC")
                            .id(3)
                            .jid("JIDS-3")
                            .gradingEngine("ENG")
                            .gradingLanguage("ASM")
                            .time(Instant.ofEpochSecond(450))
                            .userJid("c2")
                            .problemJid("p1")
                            .latestGrading(new Grading.Builder()
                                    .id(1)
                                    .jid("JIDG-2")
                                    .score(100)
                                    .verdict(Verdicts.ACCEPTED)
                                    .build())
                            .build());

            @Test
            void no_pending() throws JsonProcessingException {
                scoreboardProcessor.computeToString(
                        mapper,
                        state,
                        contest,
                        styleModuleConfig,
                        contestantStartTimesMap,
                        baseSubmissions,
                        ImmutableList.of(),
                        freezeTime);

                verify(mapper).writeValueAsString(new IcpcScoreboard.Builder()
                        .state(state)
                        .content(new IcpcScoreboardContent.Builder()
                                .addEntries(new IcpcScoreboardEntry.Builder()
                                        .rank(1)
                                        .contestantJid("c2")
                                        .totalAccepted(2)
                                        .totalPenalties(5)
                                        .lastAcceptedPenalty(150000)
                                        .addAttemptsList(1, 1)
                                        .addPenaltyList(3, 2)
                                        .addProblemStateList(
                                                IcpcScoreboardProblemState.ACCEPTED,
                                                IcpcScoreboardProblemState.FIRST_ACCEPTED
                                        )
                                        .build())
                                .addEntries(new IcpcScoreboardEntry.Builder()
                                        .rank(2)
                                        .contestantJid("c1")
                                        .totalAccepted(1)
                                        .totalPenalties(1)
                                        .lastAcceptedPenalty(40000)
                                        .addAttemptsList(1, 0)
                                        .addPenaltyList(1, 0)
                                        .addProblemStateList(
                                                IcpcScoreboardProblemState.FIRST_ACCEPTED,
                                                IcpcScoreboardProblemState.NOT_ACCEPTED
                                        )
                                        .build())
                                .build())
                        .build());
            }

            @Test
            void pending_does_not_overwrite_accepted() throws JsonProcessingException {
                List<Submission> submissions = new ImmutableList.Builder<Submission>()
                        .addAll(baseSubmissions)
                        .add(new Submission.Builder()
                                .containerJid("JIDC")
                                .id(4)
                                .jid("JIDS-4")
                                .gradingEngine("ENG")
                                .gradingLanguage("ASM")
                                .time(Instant.ofEpochSecond(501))
                                .userJid("c1")
                                .problemJid("p1")
                                .latestGrading(new Grading.Builder()
                                        .id(1)
                                        .jid("JIDG-2")
                                        .score(100)
                                        .verdict(Verdicts.ACCEPTED)
                                        .build())
                                .build())
                        .build();

                scoreboardProcessor.computeToString(
                        mapper,
                        state,
                        contest,
                        styleModuleConfig,
                        contestantStartTimesMap,
                        submissions,
                        ImmutableList.of(),
                        freezeTime);

                verify(mapper).writeValueAsString(new IcpcScoreboard.Builder()
                        .state(state)
                        .content(new IcpcScoreboardContent.Builder()
                                .addEntries(new IcpcScoreboardEntry.Builder()
                                        .rank(1)
                                        .contestantJid("c2")
                                        .totalAccepted(2)
                                        .totalPenalties(5)
                                        .lastAcceptedPenalty(150000)
                                        .addAttemptsList(1, 1)
                                        .addPenaltyList(3, 2)
                                        .addProblemStateList(
                                                IcpcScoreboardProblemState.ACCEPTED,
                                                IcpcScoreboardProblemState.FIRST_ACCEPTED
                                        )
                                        .build())
                                .addEntries(new IcpcScoreboardEntry.Builder()
                                        .rank(2)
                                        .contestantJid("c1")
                                        .totalAccepted(1)
                                        .totalPenalties(1)
                                        .lastAcceptedPenalty(40000)
                                        .addAttemptsList(1, 0)
                                        .addPenaltyList(1, 0)
                                        .addProblemStateList(
                                                IcpcScoreboardProblemState.FIRST_ACCEPTED,
                                                IcpcScoreboardProblemState.NOT_ACCEPTED
                                        )
                                        .build())
                                .build())
                        .build());
            }

            @Test
            void pending_does_overwrite_not_accepted() throws JsonProcessingException {
                List<Submission> submissions = new ImmutableList.Builder<Submission>()
                        .addAll(baseSubmissions)
                        .add(new Submission.Builder()
                                .containerJid("JIDC")
                                .id(4)
                                .jid("JIDS-4")
                                .gradingEngine("ENG")
                                .gradingLanguage("ASM")
                                .time(Instant.ofEpochSecond(501))
                                .userJid("c1")
                                .problemJid("p2")
                                .latestGrading(new Grading.Builder()
                                        .id(1)
                                        .jid("JIDG-2")
                                        .score(100)
                                        .verdict(Verdicts.ACCEPTED)
                                        .build())
                                .build())
                        .build();

                scoreboardProcessor.computeToString(
                        mapper,
                        state,
                        contest,
                        styleModuleConfig,
                        contestantStartTimesMap,
                        submissions,
                        ImmutableList.of(),
                        freezeTime);

                verify(mapper).writeValueAsString(new IcpcScoreboard.Builder()
                        .state(state)
                        .content(new IcpcScoreboardContent.Builder()
                                .addEntries(new IcpcScoreboardEntry.Builder()
                                        .rank(1)
                                        .contestantJid("c2")
                                        .totalAccepted(2)
                                        .totalPenalties(5)
                                        .lastAcceptedPenalty(150000)
                                        .addAttemptsList(1, 1)
                                        .addPenaltyList(3, 2)
                                        .addProblemStateList(
                                                IcpcScoreboardProblemState.ACCEPTED,
                                                IcpcScoreboardProblemState.FIRST_ACCEPTED
                                        )
                                        .build())
                                .addEntries(new IcpcScoreboardEntry.Builder()
                                        .rank(2)
                                        .contestantJid("c1")
                                        .totalAccepted(1)
                                        .totalPenalties(1)
                                        .lastAcceptedPenalty(40000)
                                        .addAttemptsList(1, 0)
                                        .addPenaltyList(1, 0)
                                        .addProblemStateList(
                                                IcpcScoreboardProblemState.FIRST_ACCEPTED,
                                                IcpcScoreboardProblemState.FROZEN
                                        )
                                        .build())
                                .build())
                        .build());
            }

            @Test
            void pending_counts_on_freeze_time() throws JsonProcessingException {
                List<Submission> submissions = new ImmutableList.Builder<Submission>()
                        .addAll(baseSubmissions)
                        .add(new Submission.Builder()
                                .containerJid("JIDC")
                                .id(4)
                                .jid("JIDS-4")
                                .gradingEngine("ENG")
                                .gradingLanguage("ASM")
                                .time(Instant.ofEpochSecond(500))
                                .userJid("c1")
                                .problemJid("p2")
                                .latestGrading(new Grading.Builder()
                                        .id(1)
                                        .jid("JIDG-2")
                                        .score(100)
                                        .verdict(Verdicts.ACCEPTED)
                                        .build())
                                .build())
                        .build();

                scoreboardProcessor.computeToString(
                        mapper,
                        state,
                        contest,
                        styleModuleConfig,
                        contestantStartTimesMap,
                        submissions,
                        ImmutableList.of(),
                        freezeTime);

                verify(mapper).writeValueAsString(new IcpcScoreboard.Builder()
                        .state(state)
                        .content(new IcpcScoreboardContent.Builder()
                                .addEntries(new IcpcScoreboardEntry.Builder()
                                        .rank(1)
                                        .contestantJid("c2")
                                        .totalAccepted(2)
                                        .totalPenalties(5)
                                        .lastAcceptedPenalty(150000)
                                        .addAttemptsList(1, 1)
                                        .addPenaltyList(3, 2)
                                        .addProblemStateList(
                                                IcpcScoreboardProblemState.ACCEPTED,
                                                IcpcScoreboardProblemState.FIRST_ACCEPTED
                                        )
                                        .build())
                                .addEntries(new IcpcScoreboardEntry.Builder()
                                        .rank(2)
                                        .contestantJid("c1")
                                        .totalAccepted(1)
                                        .totalPenalties(1)
                                        .lastAcceptedPenalty(40000)
                                        .addAttemptsList(1, 0)
                                        .addPenaltyList(1, 0)
                                        .addProblemStateList(
                                                IcpcScoreboardProblemState.FIRST_ACCEPTED,
                                                IcpcScoreboardProblemState.FROZEN
                                        )
                                        .build())
                                .build())
                        .build());
            }
        }
    }

    @Test
    void filter_contestant_jids() {
        IcpcScoreboardEntry entry = new IcpcScoreboardEntry.Builder()
                .rank(0)
                .contestantJid("123")
                .totalAccepted(5)
                .totalPenalties(12)
                .lastAcceptedPenalty(123)
                .build();

        IcpcScoreboard scoreboard = new IcpcScoreboard.Builder()
                .state(new ScoreboardState.Builder()
                        .addContestantJids("c1", "c2", "c3", "c4")
                        .addProblemJids("p1", "p2")
                        .addProblemAliases("A", "B")
                        .build())
                .content(new IcpcScoreboardContent.Builder()
                        .addEntries(
                                new IcpcScoreboardEntry.Builder().from(entry).rank(1).contestantJid("c1").build(),
                                new IcpcScoreboardEntry.Builder().from(entry).rank(2).contestantJid("c2").build(),
                                new IcpcScoreboardEntry.Builder().from(entry).rank(3).contestantJid("c3").build(),
                                new IcpcScoreboardEntry.Builder().from(entry).rank(4).contestantJid("c4").build())
                        .build())
                .build();

        IcpcScoreboard filteredScoreboard = new IcpcScoreboard.Builder()
                .state(new ScoreboardState.Builder()
                        .addContestantJids("c1", "c3")
                        .addProblemJids("p1", "p2")
                        .addProblemAliases("A", "B")
                        .build())
                .content(new IcpcScoreboardContent.Builder()
                        .addEntries(
                                new IcpcScoreboardEntry.Builder().from(entry).rank(-1).contestantJid("c1").build(),
                                new IcpcScoreboardEntry.Builder().from(entry).rank(-1).contestantJid("c3").build())
                        .build())
                .build();

        assertThat(scoreboardProcessor.filterContestantJids(scoreboard, ImmutableSet.of("c1", "c3")))
                .isEqualTo(filteredScoreboard);
    }
}
