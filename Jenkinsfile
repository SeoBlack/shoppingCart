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
                    def missingVars = []
                    if (!(env.SONAR_TOKEN ?: '').trim()) missingVars << 'SONAR_TOKEN'
                    if (!(env.SONAR_PROJECT_KEY ?: '').trim()) missingVars << 'SONAR_PROJECT_KEY'
                    if (!(env.SONAR_ORGANIZATION ?: '').trim()) missingVars << 'SONAR_ORGANIZATION'
                    if (!missingVars.isEmpty()) {
                        error("Missing required Sonar variables: ${missingVars.join(', ')}")
                    }

                    def mvnHome = tool name: env.MAVEN_TOOL_NAME, type: 'maven'
                    def sonarCmd = "\"${mvnHome}\\bin\\mvn\" -B -V sonar:sonar " +
                            "-Dsonar.host.url=${env.SONAR_HOST_URL} " +
                            "-Dsonar.token=${env.SONAR_TOKEN} " +
                            "-Dsonar.projectKey=${env.SONAR_PROJECT_KEY} " +
                            "-Dsonar.organization=${env.SONAR_ORGANIZATION} " +
                            "-Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml"

                    if (isUnix()) {
                        sh "${mvnHome}/bin/mvn -B -V sonar:sonar " +
                                "-Dsonar.host.url=${env.SONAR_HOST_URL} " +
                                "-Dsonar.token=${env.SONAR_TOKEN} " +
                                "-Dsonar.projectKey=${env.SONAR_PROJECT_KEY} " +
                                "-Dsonar.organization=${env.SONAR_ORGANIZATION} " +
                                "-Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml"
                    } else {
                        bat sonarCmd
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
