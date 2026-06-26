# Jenkins CI/CD Pipeline — SonarQube + Nexus + Slack

A complete, multi-stage Jenkins pipeline for a Java/Maven application, with a
master/agent setup and the supporting tools wired in:

- **SonarQube** for static analysis with a **quality gate** that can fail the build
- **Nexus** for artifact management (the built jar is published there)
- **Slack** for real-time build/deploy notifications

Everything needed to run it locally — Jenkins controller, a build agent,
SonarQube (+ Postgres), and Nexus — is defined in Docker Compose.


## Pipeline

```
   Checkout ─▶ Build ─▶ Unit Tests ─▶ Code Analysis ─▶ Quality Gate ─▶ Publish to Nexus
   (git)       (mvn)    (+ JaCoCo)     (SonarQube)      (gate or fail)   (mvn deploy)
                                                                              │
                          Slack notification on success / failure ◀───────────┘
```

Each stage runs on an agent node labelled `maven` (the controller delegates the
work — that's the master/agent part). The quality gate uses SonarQube's webhook,
so `waitForQualityGate` actually blocks on the real result instead of polling.

## Tech stack

| Piece            | Choice                                             |
|------------------|----------------------------------------------------|
| App              | Spring Boot 3.5.x, Java 17, Maven                  |
| Tests / coverage | JUnit 5 + Spring MockMvc, JaCoCo                    |
| CI engine        | Jenkins LTS (JDK 17), declarative pipeline         |
| Agent            | `inbound-agent` + Maven 3.9 (custom image)         |
| Code quality     | SonarQube (Community) + Postgres                   |
| Artifacts        | Sonatype Nexus 3 (`maven-snapshots`)               |
| Notifications    | Slack                                              |

## What's in here

```
.
├── Jenkinsfile                 # the pipeline
├── pom.xml                     # Maven build (JaCoCo + Sonar + Nexus deploy)
├── ci/settings.xml             # Maven settings for the Nexus deploy (env creds)
├── src/                        # the sample Spring Boot app + tests
├── infra/
│   ├── docker-compose.yml      # Jenkins + agent + SonarQube + Postgres + Nexus
│   ├── jenkins/                # controller image + plugins.txt
│   └── agent/                  # agent image (JDK 17 + Maven)
├── docs/SETUP.md               # step-by-step wiring guide
└── .github/workflows/build.yml # quick mvn verify sanity check
```

## Quickstart

```bash
# Linux only, once: SonarQube's Elasticsearch needs a higher mmap limit
sudo sysctl -w vm.max_map_count=262144

# Bring up the whole stack
docker compose -f infra/docker-compose.yml up -d --build
```

Then follow **[docs/SETUP.md](docs/SETUP.md)** to connect the agent and wire the
SonarQube token, Nexus credentials, Slack, and the pipeline job. Once that's
done, **Build Now** on the `ci-demo` job runs the full pipeline.

## Running the app on its own

It's an ordinary Spring Boot service, so you don't need the whole stack to poke
at it:

```bash
mvn spring-boot:run
curl "http://localhost:8080/api/greeting?name=Iftekar"
# {"id":1,"content":"Hello, Iftekar!"}
curl http://localhost:8080/api/health
# {"status":"UP"}
```

`mvn verify` runs the tests and writes the JaCoCo report to
`target/site/jacoco/jacoco.xml`, which is what SonarQube reads for coverage.

## Pushing to GitHub

```bash
git init
git add .
git commit -m "Jenkins CI/CD pipeline with SonarQube, Nexus and Slack"
git branch -M main
git remote add origin https://github.com/<your-username>/jenkins-cicd-sonarqube-nexus.git
git push -u origin main
```

The GitHub Actions **Build** workflow runs `mvn verify` on every push — a fast
confirmation that the app compiles and the tests pass. The SonarQube gate, Nexus
publish, and Slack steps are Jenkins's job (they need those servers running).

## A note on versions

- **Spring Boot 3.5.x** (latest 3.x, Java 17 baseline) is used for maximum build
  stability. Spring Boot 4.x reorganised the test starters; moving up is a small
  change when you want it.
- **JaCoCo** and the **SonarScanner for Maven** are pinned in `pom.xml`. The
  Sonar scanner is pinned in `pluginManagement`, so it's only fetched when the
  analysis stage actually runs.
- **Jenkins plugins** are installed without pinned versions so the plugin CLI
  resolves versions compatible with the bundled Jenkins LTS — which is what
  prevents controller/plugin mismatches.

## License

MIT — see [LICENSE](LICENSE).
