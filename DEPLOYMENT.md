Deployment checklist — Render (backend) + Vercel (frontend)

Summary
-------
This project has a Spring Boot backend (Maven) and a Next.js frontend. Before merging and deploying to Render (backend) and Vercel (frontend), set these environment variables and follow the steps below to ensure auth, payment callbacks, and public APIs work correctly.

Backend (Render) — required environment variables
-----------------------------------------------
Set these in your Render service environment (do NOT commit secrets):

- SPRING_DATASOURCE_URL (or keep spring.datasource.* as individual vars)
  Example: jdbc:postgresql://<host>:5432/postgres?sslmode=require
- SPRING_DATASOURCE_USERNAME
- SPRING_DATASOURCE_PASSWORD
- SUPABASE_JWT_SECRET
- APP_BASE_URL  # e.g. https://<your-render-app>.onrender.com
- SSL_COMMERZ_STORE_ID  # production value when ready
- SSL_COMMERZ_STORE_PASSWORD  # production value when ready
- Any other secrets (e.g., mail providers)

Notes:
- `app.base-url` will be read from `APP_BASE_URL` if present. This is the URL SSLCommerz will use for callbacks. Make sure SSLCommerz is configured with this exact URL.
- Do NOT keep production credentials in `application.properties` in the repo. Use Render env vars and rotate any credentials that were previously committed.
- Confirm network access from Render to your Supabase/Postgres instance.
- Consider setting `SPRING_PROFILES_ACTIVE=prod` and maintaining a `application-prod.properties` for production-specific settings.

Frontend (Vercel) — required environment variables
------------------------------------------------
Set these in your Vercel project (Environment variables > Production):

- NEXT_PUBLIC_API_URL=https://<your-render-app>.onrender.com/api/v1
- NEXT_PUBLIC_SUPABASE_URL=https://<your-supabase-instance>.supabase.co
- NEXT_PUBLIC_SUPABASE_ANON_KEY=<anon-key>

Notes:
- The frontend will use `NEXT_PUBLIC_API_URL` in production. If not set, it falls back to the default Render URL.
- Do not commit the SUPABASE anon key or other secrets to the repository.

SSLCommerz (Payment Gateway)
----------------------------
- For sandbox testing, the application currently uses SSLCommerz sandbox endpoints. For production:
  - Replace sandbox URLs with production endpoints in the backend config or via env vars if you prefer.
  - In the SSLCommerz dashboard, configure the callback/redirect URLs to use `APP_BASE_URL` + callback paths (e.g. `${APP_BASE_URL}/payment/success`, `${APP_BASE_URL}/api/v1/payment/ipn`).

Post-deploy smoke tests (recommended)
-------------------------------------
1. Non-destructive
   - GET frontend root (Vercel) — should return HTML (200).
   - GET backend public endpoints (e.g., `/api/v1/courses/public/all`) — should return JSON 200.
   - POST `/api/v1/payment/success`, `/fail`, `/cancel` — should return 200 with expected body.

2. Auth
   - Sign up or sign in using the frontend. Confirm Supabase auth works (requires NEXT_PUBLIC_SUPABASE_* variables on Vercel).
   - Inspect localStorage or network requests to verify an access token is present.
   - Use the token to call a protected backend endpoint (e.g., `/api/v1/users/me`).

3. Payment flow (staging/sandbox recommended)
   - Initiate a payment from the frontend for a test course (user must be authenticated).
   - In SSLCommerz sandbox, trigger a callback (or simulate IPN) to the `APP_BASE_URL` IPN endpoint and verify the backend updates the transaction to PAID.

4. Monitoring
   - Add alerting for long-pending payments (example: scheduled job or DB query for PENDING rows older than X hours).

Quick commands (useful)
-----------------------
# Backend tests
./mvnw test

# Simulate an IPN (only in sandbox or against a test transaction)
curl -i -X POST "${APP_BASE_URL}/api/v1/payment/ipn" \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'tran_id=TEST-1&status=VALID&amount=1.00&sessionkey=TEST-1'

FAQs / Troubleshooting
----------------------
- If login fails on the frontend: confirm `NEXT_PUBLIC_SUPABASE_URL` and `NEXT_PUBLIC_SUPABASE_ANON_KEY` are correct.
- If IPN callbacks show 404: confirm `APP_BASE_URL` is correct in the backend and that SSLCommerz is configured to hit the same URL.
- If DB connection fails: check Render's outbound network and your DB's accept list or connection string.

If you want, I can push the small code changes I made and open a PR with this checklist attached.
