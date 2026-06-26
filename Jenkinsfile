// Multi-stage declarative pipeline.
//
// Runs on a labelled build agent (master/agent architecture), builds and tests
// the Maven app, gates on a SonarQube quality gate, publishes the artifact to
// Nexus, and pings Slack with the outcome.
//
// Expected Jenkins configuration (see docs/SETUP.md):
//   - An agent node labelled 'maven' with JDK 17 + Maven on PATH
//   - A SonarQube server named 'SonarQube' (Manage Jenkins > System)
//   - Username/password credential 'nexus'
//   - Slack configured with a default channel

pipeline {
    agent { label 'maven' }

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    environment {
        // -B = batch mode (no colour codes / progress spam in the log).
        MAVEN_CLI_OPTS = '-B -ntp'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh "mvn ${MAVEN_CLI_OPTS} clean compile"
            }
        }

        stage('Unit Tests') {
            steps {
                // verify runs the tests and produces the JaCoCo coverage report.
                sh "mvn ${MAVEN_CLI_OPTS} verify"
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Code Analysis') {
            steps {
                // withSonarQubeEnv injects SONAR_HOST_URL and the auth token from
                // the Jenkins SonarQube server config.
                withSonarQubeEnv('SonarQube') {
                    sh "mvn ${MAVEN_CLI_OPTS} sonar:sonar"
                }
            }
        }

        stage('Quality Gate') {
            steps {
                // Blocks until SonarQube reports the gate result via webhook.
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Publish to Nexus') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'nexus',
                    usernameVariable: 'NEXUS_USERNAME',
                    passwordVariable: 'NEXUS_PASSWORD')]) {
                    // Tests already ran in the verify stage; skip them on deploy.
                    sh "mvn ${MAVEN_CLI_OPTS} deploy -s ci/settings.xml -DskipTests"
                }
            }
        }
    }

    post {
        success {
            slackSend(
                color: 'good',
                message: "✅ SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}\n${env.BUILD_URL}"
            )
        }
        failure {
            slackSend(
                color: 'danger',
                message: "❌ FAILURE: ${env.JOB_NAME} #${env.BUILD_NUMBER}\n${env.BUILD_URL}"
            )
        }
        unstable {
            slackSend(
                color: 'warning',
                message: "⚠️ UNSTABLE: ${env.JOB_NAME} #${env.BUILD_NUMBER}\n${env.BUILD_URL}"
            )
        }
        always {
            cleanWs()
        }
    }
}
