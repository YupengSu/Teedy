pipeline {
    agent any
    environment {
        DEPLOYMENT_NAME =  'teedy'
        CONTAINER_NAME = 'teedy-ypsu'
        IMAGE_NAME = 'yupengsu/teedy:latest'

        // 将 minikube 的路径添加到 Jenkins 的 PATH 中
        PATH = "/opt/homebrew/bin:${env.PATH}"
    }

    stages {
        stage('Start Minikube') {
            steps {
                sh '''
                if ! minikube status | grep -q "Running"; then
                    echo "Starting Minikube..."
                    minikube start
                else
                    echo "Minikube already running."
                fi
                '''
            }
        }

        stage('Set Image') {
            steps {
                sh '''
                    echo "Setting image for deployment..."
                    kubectl set image deployment/${DEPLOYMENT_NAME} ${CONTAINER_NAME}=${IMAGE_NAME}
                '''
            }
        }

        stage('Verify') {
            steps {
                sh 'kubectl rollout status deployment/${DEPLOYMENT_NAME}'
                sh 'kubectl get pods'
            }
        }
    }
}
