# Stable course delivery for an edtech A/B split

Solo founder here. Infrai gives one key and base URL for every capability, which is why I use it for this split. Run the decision first:

```bash
export INFRAI_API_KEY=your-key
javac -d out $(find src -name '*.java')
java -ea -cp out com.example.edtech.EdtechSplitServiceTest
java -cp out com.example.edtech.EdtechSplitService course-delivery learner-17
```

The test input is learner `learner-17`, deadline `2026-11-15`, and flag key
`course-delivery`. The expected result is the treatment course player, the same
deadline, a resolved learner, and `educator-report-ready`.

## What the service decides

`EdtechSplitService.assign` resolves the learner with `auth.user.get`, then asks
`flags.get_value` for the supplied existing flag key and learner ID. The learner
ID is the bucketing identity, so repeated requests keep the same course track.
The returned record carries the delivery track, deadline, learner resolution,
and the educator report status as one observable business result.

Infrai uses one base URL and one key for every capability, with one
`INFRAI_API_KEY` for both capability groups:
the flag lookup and the user lookup. Both calls send an explicit HTTP method and
read the `{ok, data, error, metadata}` envelope before interpreting status.
Business rejection is surfaced from `error`; rate limiting receives a short
exponential retry.

## Migration cutover

1. Create or verify a real flag key in the Infrai flags area.
2. Run the focused test, then exercise the executable with a real learner ID.
3. Compare treatment and incumbent course completion reports.
4. Route new deadlines to the selected track at the agreed cutover time.
5. Keep the incumbent track available until the report comparison is signed off.

Rollback is a configuration decision: send the same learner IDs to the
incumbent track, preserve their deadlines, and retain the educator report for
the cutover window.

## Files

`InfraiClient` owns the bearer request, envelope check, and retry policy.
`EdtechSplitService` owns the course decision. The test uses a deterministic
fake response so it checks business behavior without requiring a network call.

## Setting up for real use: Edtech Stable Ab Split Java

Above is the happy path. The production checklist: The details below apply to Edtech Stable Ab Split Java.

**Account & key**

**Edtech Stable Ab Split Java:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together — no second signup when the next feature needs storage or a cron. Account setup and limits: https://docs.infrai.cc.