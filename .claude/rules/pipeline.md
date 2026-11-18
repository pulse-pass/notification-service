---
description: CI, main ruleset, SonarQube and branch conventions
paths:
  - ".github/**"
---

# Pipeline

- **NTF-143** Polyrepo in the `pulse-pass` GitHub organization, `main` only.
- **NTF-144** PRs come from `feature/<ID>-<slug>`; merge commits only (squash and rebase disabled).
- **NTF-145** `main` ruleset: active on the default branch, no bypass, blocks deletion and force push.
- **NTF-146** The ruleset requires a PR (0 approvals, merge method `merge`).
- **NTF-147** The ruleset requires every CI job to pass (`lint`, `unit-tests`, `integration-tests`, `sonar`, `build`).
- **NTF-148** It can be created with `gh api -X POST repos/pulse-pass/<repo>/rulesets`.
- **NTF-149** SonarQube Cloud: organization `pulse-pass` (Free plan), project `pulse-pass_<repo>` with GitHub Actions analysis.
- **NTF-150** `SONAR_TOKEN` is an organization secret shared by all repositories.
- **NTF-151** Workflows: `pull-request.yml` (name `CI`) and `publish.yml` (name `Publish`, never "release").
- **NTF-152** Third-party actions are pinned by full commit SHA (Sonar rule S7637) with the version in a comment; `actions/*` may use tags. That version is the only comment allowed in workflows.
- **NTF-153** The `sonar` job runs with `-Dsonar.qualitygate.wait=true`, so a failed quality gate fails the job and skips `build`.
- **NTF-154** Pipeline: PR with lint (Checkstyle) → unit tests (JaCoCo) → integration tests (through `service-start.sh test`) → SonarQube Cloud → image build.
- **NTF-155** On merge: version from the commits, `vX.Y.Z` tag and image on GHCR.
