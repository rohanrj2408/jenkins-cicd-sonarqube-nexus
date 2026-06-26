# Setup Guide

End-to-end wiring for the pipeline. Plan for ~30 minutes the first time, mostly
waiting for SonarQube and Nexus to finish their first boot.

## 0. Prerequisites

- Docker + Docker Compose
- On Linux, raise the mmap limit once (SonarQube's embedded Elasticsearch needs it):
  ```bash
  sudo sysctl -w vm.max_map_count=262144
  ```

## 1. Bring up the stack

From the repo root:

```bash
docker compose -f infra/docker-compose.yml up -d --build
```

Give it a few minutes, then check:

- Jenkins  → http://localhost:8080
- SonarQube → http://localhost:9000  (first login `admin` / `admin`, then change it)
- Nexus    → http://localhost:8081

The Nexus initial admin password is generated inside the container:

```bash
docker exec nexus cat /nexus-data/admin.password
```

Log into Nexus, complete the setup wizard, and set a password you'll reuse below.
Nexus 3 ships with `maven-releases` and `maven-snapshots` hosted repos out of the
box — those are exactly the ones `pom.xml` deploys to, so there's nothing to create.

## 2. SonarQube: token + webhook

1. **Token** — in SonarQube, *My Account → Security → Generate Token*. Copy it.
2. **Webhook** — *Administration → Configuration → Webhooks → Create*:
   - Name: `jenkins`
   - URL: `http://jenkins:8080/sonarqube-webhook/`

   The webhook is what makes the pipeline's `waitForQualityGate` return instead of
   hanging until timeout.

## 3. Jenkins: agent node

The build runs on an agent labelled `maven`. Create the node so the agent
container can connect:

1. *Manage Jenkins → Nodes → New Node*
2. Name `maven-agent`, type *Permanent Agent*
3. Remote root dir `/home/jenkins/agent`, **Labels: `maven`**, launch method
   *Launch agent by connecting it to the controller*
4. Save, open the node page, and copy the **secret** string
5. Put it where compose can read it and recreate the agent:
   ```bash
   export JENKINS_AGENT_SECRET=<the-secret>
   docker compose -f infra/docker-compose.yml up -d jenkins-agent
   ```

The node should flip to online within a few seconds.

## 4. Jenkins: credentials

*Manage Jenkins → Credentials → System → Global → Add Credentials*:

| Kind                     | ID       | Value                                  |
|--------------------------|----------|----------------------------------------|
| Secret text              | `sonar`  | the SonarQube token from step 2        |
| Username with password   | `nexus`  | your Nexus username + password         |

## 5. Jenkins: SonarQube server

1. Install nothing extra — the `sonar` plugin is already in the image.
2. *Manage Jenkins → System → SonarQube servers → Add*:
   - Name: **`SonarQube`** (must match `withSonarQubeEnv('SonarQube')`)
   - Server URL: `http://sonarqube:9000`
   - Server authentication token: the `sonar` credential from step 4

## 6. Jenkins: Slack

1. In Slack, add the *Jenkins CI* app and copy the token / workspace.
2. *Manage Jenkins → System → Slack*: set the workspace, default channel
   (e.g. `#builds`), and the credential. Use **Test Connection** to confirm.

## 7. Create the pipeline job

1. *New Item → Pipeline*, name it `ci-demo`.
2. Under *Pipeline*, choose **Pipeline script from SCM**, SCM **Git**, point it at
   your repo URL and branch `main`. Script path: `Jenkinsfile`.
3. Save and **Build Now**.

You should see the stages run in order, the SonarQube quality gate pass, the
artifact land in Nexus under `maven-snapshots`, and a Slack message arrive.

## Troubleshooting

- **SonarQube container restarts / exits** → the `vm.max_map_count` step (0) was
  skipped.
- **`waitForQualityGate` hangs** → the webhook URL in step 2 is wrong, or
  SonarQube can't reach `jenkins:8080` (they must share the compose network).
- **Nexus deploy 401** → the `nexus` credential is wrong, or the server `id`s in
  `ci/settings.xml` don't match `pom.xml` (`nexus-snapshots` / `nexus-releases`).
- **`mvn: not found` on the agent** → you're connected to the default JNLP agent
  rather than the `maven-agent` image; confirm the node label is `maven`.
