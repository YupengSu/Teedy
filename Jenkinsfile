pipeline {
    agent any

    tools {
        maven 'default-maven'
    }

    environment {
        PATH = "/usr/local/bin:$PATH"
        DOCKER_HUB_CREDENTIALS = 'dockerhub_credentials'
        DOCKER_IMAGE = 'yupengsu/teedy'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
    }

    stages {

        stage('Switch Docker Context') {
            steps {
                sh 'docker context use default || true'
            }
        }

        stage('Build') {
            steps {
                checkout scmGit(
                    branches: [[name: '*/master']],
                    extensions: [],
                    userRemoteConfigs: [[url: 'https://github.com/YupengSu/Teedy.git']]
                )
                sh 'mvn -B -DskipTests clean package'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: "${DOCKER_HUB_CREDENTIALS}", usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                    sh '''
                        echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
                        docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                        docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest
                        docker push ${DOCKER_IMAGE}:latest
                    '''
                }
            }
        }

        stage('Run Docker Containers') {
            steps {
                script {
                    def ports = [8081, 8082, 8083]
                    for (port in ports) {
                        sh """
                            docker stop teedy-container-${port} || true
                            docker rm teedy-container-${port} || true
                            docker run --name teedy-container-${port} -d -p ${port}:8080 ${DOCKER_IMAGE}:${DOCKER_TAG}
                        """
                    }

                    sh 'docker ps --filter "name=teedy-container"'
                }
            }
        }
    }
}
