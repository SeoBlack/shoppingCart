pipeline {
    agent any

    environment {
        IMAGE_NAME = "shoppingcart-app"
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        MAVEN_TOOL_NAME = "Maven3"
        DOCKERHUB_CREDENTIALS_ID = "sorinoraibi575675"
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
                        sh "${mvnHome}/bin/mvn -B -V clean test package"
                    } else {
                        bat "\"${mvnHome}\\bin\\mvn\" -B -V clean test package"
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    def dockerRepo = env.DOCKERHUB_REPO ?: ''
                    if (!dockerRepo?.trim()) {
                        error('DOCKERHUB_REPO is not set. Configure it as a Jenkins environment variable, for example: your-dockerhub-username/shoppingcart')
                    }

                    env.FULL_IMAGE_NAME = "${dockerRepo}/${env.IMAGE_NAME}:${env.IMAGE_TAG}"
                    env.FULL_IMAGE_LATEST = "${dockerRepo}/${env.IMAGE_NAME}:latest"

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
