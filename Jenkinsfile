pipeline {
    agent any

    // Docker Hub image format is: DOCKERHUB_USERNAME/REPOSITORY:tag
    // Use username ONLY here — not "user/repo" (repo name is IMAGE_NAME below).
    environment {
        DOCKERHUB_USERNAME = "sorinoraibi575675"
        IMAGE_NAME = "shoppingcart-app"
        IMAGE_TAG = "latest"
        MAVEN_TOOL_NAME = "M3"
        DOCKERHUB_CREDENTIALS_ID = "docker_token"
        SONAR_HOST_URL = "https://sonarcloud.io"
        SONAR_TOKEN_CREDENTIALS_ID = "sonar-token"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                script {
                    def mvnHome = tool name: env.MAVEN_TOOL_NAME, type: 'maven'
                    if (isUnix()) {
                        sh "${mvnHome}/bin/mvn -B -V clean verify"
                    } else {
                        bat "\"${mvnHome}\\bin\\mvn\" -B -V clean verify"
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    def sonarProjectKey = "${env.SONAR_PROJECT_KEY ?: params.SONAR_PROJECT_KEY ?: ''}".trim()
                    def sonarOrganization = "${env.SONAR_ORGANIZATION ?: params.SONAR_ORGANIZATION ?: ''}".trim()
                    def mvnHome = tool name: env.MAVEN_TOOL_NAME, type: 'maven'
                    def runSonar = { String sonarToken ->
                        def sonarCmd = "\"${mvnHome}\\bin\\mvn\" -B -V sonar:sonar " +
                                "-Dsonar.host.url=${env.SONAR_HOST_URL} " +
                                "-Dsonar.token=${sonarToken} " +
                                "-Dsonar.projectKey=${sonarProjectKey} " +
                                "-Dsonar.organization=${sonarOrganization} " +
                                "-Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml"

                        if (isUnix()) {
                            sh "${mvnHome}/bin/mvn -B -V sonar:sonar " +
                                    "-Dsonar.host.url=${env.SONAR_HOST_URL} " +
                                    "-Dsonar.token=${sonarToken} " +
                                    "-Dsonar.projectKey=${sonarProjectKey} " +
                                    "-Dsonar.organization=${sonarOrganization} " +
                                    "-Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml"
                        } else {
                            bat sonarCmd
                        }
                    }

                    if (!sonarProjectKey) {
                        error("Missing required Sonar variable: SONAR_PROJECT_KEY (env var or job parameter)")
                    }
                    if (!sonarOrganization) {
                        error("Missing required Sonar variable: SONAR_ORGANIZATION (env var or job parameter)")
                    }

                    def directToken = "${env.SONAR_TOKEN ?: ''}".trim()
                    if (directToken) {
                        runSonar(directToken)
                    } else {
                        withCredentials([string(credentialsId: env.SONAR_TOKEN_CREDENTIALS_ID, variable: 'SONAR_TOKEN_FROM_CREDENTIAL')]) {
                            def credentialToken = "${env.SONAR_TOKEN_FROM_CREDENTIAL ?: ''}".trim()
                            if (!credentialToken) {
                                error("Missing Sonar token. Set SONAR_TOKEN env var or create Secret Text credential with ID '${env.SONAR_TOKEN_CREDENTIALS_ID}'.")
                            }
                            runSonar(credentialToken)
                        }
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    def hubUser = env.DOCKERHUB_USERNAME ?: ''
                    if (!hubUser?.trim()) {
                        error('DOCKERHUB_USERNAME is not set. Use your Docker Hub username only (e.g. sorinoraibi575675).')
                    }

                    env.FULL_IMAGE_NAME = "${hubUser}/${env.IMAGE_NAME}:${env.IMAGE_TAG}"
                    env.FULL_IMAGE_LATEST = "${hubUser}/${env.IMAGE_NAME}:latest"

                    dockerImage = docker.build(env.FULL_IMAGE_NAME)
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', env.DOCKERHUB_CREDENTIALS_ID) {
                        dockerImage.push()
                        dockerImage.push('latest')
                    }
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true
            archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
        }
        success {
            echo "Pipeline completed successfully."
            echo "Pushed images: ${env.FULL_IMAGE_NAME} and ${env.FULL_IMAGE_LATEST}"
        }
        failure {
            echo "Pipeline failed. Check stage logs for details."
        }
    }
}
