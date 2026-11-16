# PURPOSE: Operator/developer documentation for SWARM.
# 19 — Analytics and AI architecture

## Exists today

| Piece | Mechanism | Honest name |
|---|---|---|
| Analytics charts | SQL history + Recharts | Working |
| Plant health | `AiHealthService` rules on pH, methane, flow, temp, alert count | Rule engine |
| AI recommendations UI | Persisted rows from that engine | Working |
| Empty UI health | Hardcoded 85/GOOD | Bug |
| Predictive RUL | 365 × health% | Heuristic — do not call ML |
| Reports | Fake path | Stub |
| NOVA | Search slots → optional LLM think → skill → narrate + AnswerGuard | Working subset |
| Nova stub tools | `telemetry.history`, `plant.maintenance` | Missing skills |
| LLM | Groq → OpenRouter → Gemini; off on standby | Optional |

## Rules for all AI surfaces

1. Never fabricate telemetry.
2. Every numeric claim has evidence: instrument/node, time, quality, source_kind.
3. Health score and RUL are `AI_ESTIMATE` / `CALCULATED`, never `MEASURED`.
4. If quality is STALE/BAD, say so.
5. NOVA cannot command HMI or change setpoints.

## Analytics (J)

Keep `/analytics/{plantId}` filters. Add:

- Series metadata `{ quality, sourceKind }`.
- Skip or gap BAD points.
- Daily production from `daily_aggregates` when flow mapped.
- Compare plants: same metric, same quality rules.

Reports: use iText/POI already on classpath. Job table. Types already in UI: DAILY, WEEKLY, MONTHLY, PLANT_SUMMARY, GAS_PRODUCTION, ALERTS, PLANT_HEALTH — implement those, don’t invent ten more.

## Rule engine (retain)

Keep `AiHealthService` as `PlantHealthRules`. Output:

```
score, status, issues[], evidence[], generatedAt
```

Tune per `plant_type` templates (BIO_CNG vs SANITATION). Do not apply digester pH rules to a plant with no pH instrument.

## NOVA (K)

Retain architecture in `docs/NOVA_SPACE_OP_ARCHITECTURE.md` and `com.biopower.novaspace`.

Implement missing skills with `TelemetryQuery` and ops/maintenance facades.

Think/Narrate LLM stays optional. AnswerGuard tests already exist — extend them with “must not invent numbers”.

Space analyser fan-out stays read-only.

Port patterns from `NOVA_DNA_source_for_SWARM/` **selectively** (evidence, soft-fail). Do not import ERP skills.

## Predictive

Rename UI to **Equipment condition**. Inputs: last seen, alert rate, operator downtime. Output: index 0–100 + evidence. No failure date unless a documented method is added later.
