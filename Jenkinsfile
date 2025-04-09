pipeline {
    agent {
        kubernetes {
            yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: maven
    image: maven:3.9.9-eclipse-temurin-21-alpine
    command: [ 'cat' ]
    tty: true
  - name: docker
    image: docker:27.2.0-alpine3.20
    command: [ 'cat' ]
    tty: true
    resources:
      requests:
        memory: "2Gi"
        cpu: "1"
      limits:
        memory: "4Gi"
        cpu: "2"
    volumeMounts:
      - mountPath: "/var/run/docker.sock"
        name: docker-socket
  volumes:
    - name: docker-socket
      hostPath:
        path: "/var/run/docker.sock"
"""
        }
    }

    environment {
        DOCKER_IMAGE_NAME = 'kimdoyun0806/university-api'
        DOCKER_CREDENTIALS_ID = 'dockerhub-access'
    }

    stages {
        stage('Maven Build') {
            steps {
                container('maven') {
                    sh 'mvn -v'
                    sh 'mvn package'
                    sh 'ls -al ./target'
                }
            }
        }

        stage('Image Build & Push') {
            steps {
                container('docker') {
                    script {
                        def dockerImageVersion = "${env.BUILD_NUMBER}"

                        withCredentials([usernamePassword(
                            credentialsId: DOCKER_CREDENTIALS_ID,
                            usernameVariable: 'DOCKER_USERNAME',
                            passwordVariable: 'DOCKER_PASSWORD'
                        )]) {
                            sh 'echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin'
                        }

                        withEnv(["DOCKER_IMAGE_VERSION=${dockerImageVersion}"]) {
                            sh 'docker -v'
                            sh 'docker build --no-cache -t $DOCKER_IMAGE_NAME:$DOCKER_IMAGE_VERSION ./'
                            sh 'docker image inspect $DOCKER_IMAGE_NAME:$DOCKER_IMAGE_VERSION'
                            sh 'docker push $DOCKER_IMAGE_NAME:$DOCKER_IMAGE_VERSION'
                        }
                    }
                }
            }
        }
    }
}
