pipelineJob('Multi-Platform-Container-Image') {
    definition {
        cps {
            script("""
                pipeline {
                    agent any
                    triggers {
                      githubPush()
                    } 
                    environment {
                        DOCKER_CREDENTIALS_ID = 'dockerhub'
                        DOCKERHUB_REPO = 'chlokesh1306/lokesh'
                        GITHUB_CREDENTIALS_ID = 'github'
                    }
                    stages {
                        stage('Checkout') {
                            steps {
                                git branch: 'main', url: 'https://github.com/cyse7125-su24-team15/static-site', credentialsId: env.GITHUB_CREDENTIALS_ID
                            }
                        }
                        stage('Build and Publish') {
                            steps {
                                script {
                                    docker.withRegistry('', env.DOCKER_CREDENTIALS_ID) {
                                        sh 'docker run --rm --privileged multiarch/qemu-user-static --reset -p yes'
                                        sh 'docker buildx create --name csye7125 --driver docker-container --use || docker buildx use csye7125'
                                        sh 'docker buildx inspect csye7125 --bootstrap'
                                        sh 'docker buildx build --platform linux/amd64,linux/arm64 -t "\${DOCKERHUB_REPO}:latest" --push .'
                                    }
                                }
                            }
                        }
                    }
                    post {
                        always {
                            deleteDir()
                        }
                    }
                }
            """.stripIndent())
            sandbox(false)
        }
    }
    triggers {
        githubPush()
    }
}
