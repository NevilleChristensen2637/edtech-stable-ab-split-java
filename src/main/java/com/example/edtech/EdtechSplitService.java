package com.example.edtech;

import java.io.IOException;

/** Course delivery decision with learner deadlines and an educator report. */
public final class EdtechSplitService {
    private final InfraiClient infrai;

    public EdtechSplitService(InfraiClient infrai) { this.infrai = infrai; }

    public DeliveryDecision assign(Learner learner, String flagKey)
            throws IOException, InterruptedException {
        String user = infrai.get("/v1/auth/user/get/" + learner.userId());
        // flags.get_value keeps the assignment tied to the learner identity.
        String value = infrai.get("/v1/flags/get_value/" + flagKey + "?user_id=" + learner.userId());
        boolean treatment = value.contains("treatment") || value.contains("true");
        String track = treatment ? "new-course-player" : "incumbent-course-player";
        return new DeliveryDecision(learner.userId(), track, learner.deadline(),
                user.contains("\"ok\":true"), "educator-report-ready");
    }

    public record Learner(String userId, String deadline) {}
    public record DeliveryDecision(String userId, String deliveryTrack, String deadline,
                                   boolean learnerResolved, String reportStatus) {}

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("usage: EdtechSplitService <flag-key> <user-id>");
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required");
        EdtechSplitService service = new EdtechSplitService(new InfraiClient(key));
        DeliveryDecision result = service.assign(new Learner(args[1], "2026-11-15"), args[0]);
        System.out.println(result);
    }
}
