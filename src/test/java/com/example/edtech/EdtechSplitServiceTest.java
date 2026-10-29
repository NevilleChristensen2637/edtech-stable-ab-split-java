package com.example.edtech;

import java.io.IOException;

public final class EdtechSplitServiceTest {
    public static void main(String[] args) throws Exception {
        EdtechSplitService service = new EdtechSplitService(new FakeClient());
        EdtechSplitService.DeliveryDecision decision = service.assign(
                new EdtechSplitService.Learner("learner-17", "2026-11-15"), "course-delivery");
        assert decision.deliveryTrack().equals("new-course-player");
        assert decision.deadline().equals("2026-11-15");
        assert decision.learnerResolved();
        assert decision.reportStatus().equals("educator-report-ready");
        System.out.println("business decision: treatment track with deadline and report status");
    }

    private static final class FakeClient extends InfraiClient {
        FakeClient() { super("test-key"); }
        @Override String get(String path) throws IOException {
            if (path.startsWith("/v1/auth/user/get/")) return "{\"ok\":true,\"data\":{}}";
            return "{\"ok\":true,\"data\":{\"value\":\"treatment\"}}";
        }
    }
}
