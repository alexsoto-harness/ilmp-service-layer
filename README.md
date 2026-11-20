# ilmp-service-layer.

Java 21 / Spring Boot 3 service layer for the ILMP Index Studio demo (Harness for S&P DJI). It runs on ECS Fargate behind `ilmp-demo-alb` in Dev and QA.

| Endpoint | Purpose |
|---|---|
| `GET /actuator/health` | ALB health check |
| `GET /api/version` | Build identity. `image_digest` comes from ECS task metadata (FR-008) |
| `GET /api/indices` | Static index list |
| `GET /api/indices/{id}` | One index, or 404 |

```bash
mvn test
docker build --build-arg IMAGE_TAG=dev -t service-layer .
```

The same `ilmp_build_scan_push` template in [ilmp-delivery](https://github.com/alexsoto-harness/ilmp-delivery) builds this repo and the calc engine. `deploy/ecs/` holds the task and service definitions, owned by Harness CD.
