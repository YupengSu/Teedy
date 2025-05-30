pipeline {
    agent any

    tools {
        maven 'default-maven'
    }

    environment { 
        PATH = "/usr/local/bin:/opt/homebrew/bin:/usr/bin:/bin:/usr/sbin:/sbin"
        // define environment variable 
        // Jenkins credentials configuration 
        DOCKER_HUB_CREDENTIALS = credentials('dockerhub_credentials') // Docker Hub credentials ID store in Jenkins 
        // Docker Hub Repository's name 
        DOCKER_IMAGE = 'yupengsu/teedy' // your Docker Hub user name and Repository's name 
        DOCKER_TAG = "${env.BUILD_NUMBER}" // use build number as tag 
    } 
    stages { 
        stage('Build') { 
            steps { 
                checkout scmGit( 
                    branches: [[name: '*/master']],  
                    extensions: [],  
                    userRemoteConfigs: [[url: 'https://github.com/YupengSu/Teedy.git']] // your github Repository 
                ) 
                sh 'mvn -B -DskipTests clean package' 
            } 
        } 
        // Building Docker images 
        stage('Building image') { 
            steps { 
                script { 
                    // assume Dockerfile locate at root  
                    docker.build("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}") 
                } 
            } 
        } 
        // Uploading Docker images into Docker Hub 
        stage('Upload image') { 
            steps { 
                script { 
                    // sign in Docker Hub 
                    docker.withRegistry('https://registry.hub.docker.com', 'DOCKER_HUB_CREDENTIALS') { 
                        // push image 
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push() 
                        // ：optional: label latest 
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push('latest')
                    }
                }
            }
        }

        // Running Docker container 
        stage('Run containers') { 
            steps { 
                script { 
                // stop then remove containers if exists 
                sh 'docker stop teedy-container-8081 || true' 
                sh 'docker rm teedy-container-8081 || true' 
                // run Container 
                docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").run( 
                    '--name teedy-container-8081 -d -p 8081:8080' 
                ) 
                    
                // Optional: list all teedy-containers 
                sh 'docker ps --filter "name=teedy-container"' 
                } 
            } 
        } 
    } 
}
