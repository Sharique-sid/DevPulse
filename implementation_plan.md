# DevPulse Backend Bug Report & Action Plan

I've acted as a senior developer and thoroughly reviewed the application's endpoints, services, security configurations, and scheduling logic. Overall, the application has a solid foundation, but I've identified several **critical security vulnerabilities and logical bugs** that need to be addressed before this goes to production.

## User Review Required
Please review the bugs below. If you approve this plan, I will systematically go through and fix each of them.

---

## 1. WebSocket Topic Subscription IDOR (Critical Security Bug)
**The Bug:** In `WebSocketConfig.java`, you successfully authenticate connections using the JWT token via the `CONNECT` frame. However, you don't intercept the `SUBSCRIBE` command.
**The Impact:** Any authenticated user can subscribe to any topic. A user from `org_id=1` can simply send a subscribe frame to `/topic/org/2/pings` and listen to all ping logs and alerts for other organizations in real-time.
**Proposed Fix:** Add logic to the `preSend` channel interceptor to catch `StompCommand.SUBSCRIBE`. Extract the requested destination, parse the `orgId` from it, and ensure it matches the authenticated user's `UserPrincipal.getOrgId()`.

## 2. Server-Side Request Forgery (SSRF) in PingScheduler (Critical Security Bug)
**The Bug:** Users can create an endpoint with URLs pointing to internal networks (e.g., `http://localhost:8082/actuator`, `http://127.0.0.1:3306`, or Cloud Metadata IPs like `http://169.254.169.254/latest/meta-data/`). The `RestTemplate` in `PingScheduler` will execute these requests from the server's internal network.
**The Impact:** Attackers can scan your internal network, bypass firewalls, and potentially extract sensitive data from internal infrastructure.
**Proposed Fix:** Add a URL validation utility that parses the URL and rejects any domains resolving to private IP ranges (e.g., `127.0.0.0/8`, `10.0.0.0/8`, `192.168.0.0/16`, `169.254.169.254`).

## 3. Gemini API Quota Exhaustion / No Rate Limiting (High)
**The Bug:** The `POST /ai-insights/generate` endpoint calls `aiInsightService.generateWeeklyInsight()`, which directly hits the Gemini API. There is no rate limiting or cooldown logic.
**The Impact:** A malicious or impatient user can spam this endpoint thousands of times per minute. This will rapidly exhaust your Gemini API quota and could result in massive API billing charges.
**Proposed Fix:** Implement a cooldown in `AiInsightService`. Before generating a new insight, check if an insight was already generated for that `orgId` within the last X hours. If so, return a 429 Too Many Requests or just return the latest insight.

## 4. Alert Spam / Infinite Alert Creation (Medium/Logic Bug)
**The Bug:** In `PingScheduler.java` (`createAlertIfNeeded`), you search for `findRecentUnresolvedAlerts` using a 5-minute cooldown threshold. If an endpoint is down for *longer* than 5 minutes (e.g., 6 minutes), the query returns empty (because the existing unresolved alert is older than 5 minutes). The system then creates a *brand new* alert.
**The Impact:** If an endpoint is down for an hour, the user will get spammed with a new alert every 5 minutes (or whatever the check interval is), flooding the database.
**Proposed Fix:** Change the query to simply look for *any* unresolved alert for that endpoint (`findByEndpointIdAndIsResolvedFalse`). If one exists, update its `lastTriggeredAt` time. Only create a new alert if no unresolved alert exists at all.

## 5. Synchronous Blocking in PingScheduler (Medium/Performance Bug)
**The Bug:** `pingAllEndpoints()` loops through all active endpoints and uses `RestTemplate.exchange()` synchronously. Furthermore, `RestTemplate` has no timeout configured.
**The Impact:** If a single user's endpoint is a "tarpit" (accepts connections but hangs forever), the thread will freeze. This will delay or completely halt the ping checks for all other endpoints in the system.
**Proposed Fix:** Configure a custom `RestTemplate` bean with strict `ConnectTimeout` (e.g., 5 seconds) and `ReadTimeout` (e.g., 10 seconds). We should also consider using `@Async` for pinging individual endpoints or parallel streams so one slow endpoint doesn't block the rest.

## 6. Weak URL Validation (Low)
**The Bug:** `CreateEndpointRequest.java` only validates that `url` is `@NotBlank` and `@Size(max=500)`. It doesn't verify if it's a valid HTTP/HTTPS URL.
**The Impact:** A user can submit `url: "not-a-real-url"`. The database saves it, and `PingScheduler` will continuously throw `IllegalArgumentException` every time it tries to ping it.
**Proposed Fix:** Add the `@org.hibernate.validator.constraints.URL` annotation or a strict Regex pattern to the `url` field in the DTOs.

---

## Verification Plan
Once approved, I will implement the fixes above and then manually run tests to verify:
1. Connecting via WebSocket with Org A's token and attempting to subscribe to Org B's topic fails.
2. Attempting to create an endpoint pointing to `http://localhost` fails.
3. Repeatedly calling `/ai-insights/generate` gets blocked by the cooldown logic.
4. Downed endpoints only generate exactly one unresolved alert.
