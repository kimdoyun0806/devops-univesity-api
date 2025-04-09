pipeline {
    agent {
        kubernetes {
            yaml '''
            apiVersion: v1
            kind: Pod
            metadata:
              name: jenkins-agent
            spec:
              containers:
              - name: maven
                image: maven:3.9.9-eclipse-temurin-21-alpine
                command:
                - cat      
                tty: true
              - name: docker
                image: docker:27.2.0-alpine3.20
                command:
                - cat
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
            '''
        }
    }

    environment {
        DOCKER_IMAGE_NAME = 'kimdoyun/university-api'
    }

    stages {
        stage('Maven Build') {
            steps {
                container('maven') {
                    sh 'pwd'
                    sh 'ls -al'
                    sh 'mvn -v'
                    // sh 'mvn clean'
                    sh 'mvn package'
                    sh 'ls -al' 
                    sh 'ls -al ./target'
                }
            }               
        }
        stage('Image Build & Push') {
            steps {
                container('docker') {
                    script {
                        def dockerImageVersion = "${env.BUILD_NUMBER}"
                      
                        // 파이프라인 단계에서 환경 변수를 설정하는 역할을 한다.
                        withEnv(["DOCKER_IMAGE_VERSION=${dockerImageVersion}"]) {
                            sh 'docker -v'
                            sh 'echo DOCKER_IMAGE_NAME'
                            sh 'echo DOCKER_IMAGE_VERSION'
                            // sh 'docker images university-api'
                            // sh 'docker build --no-cache -t university-api:2.5 ./'
                            // sh 'docker images university-api'
                        }
                    }
                }
            }
        }


    }
}
