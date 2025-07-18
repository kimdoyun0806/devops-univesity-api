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
        DOCKER_IMAGE_NAME = 'kimdoyun0806/university-api'
        DOCKER_CREDENTIALS_ID = 'dockerhub-access'
    }

    stages {
        //         stage('SonarQube Analysis'){
        //     steps{
        //         container('maven') {
        //             withSonarQubeEnv('sonarqube-server'){
        //                 sh '''mvn clean verify sonar:sonar \
        //                     -Dsonar.projectKey=university-api \
        //                     -Dsonar.projectName=university-api'''
        //             }
        //         }
        //     }
        // }
        
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

                        sh 'docker logout'

                        // withCredentials()
                        //  - 파이프라인에서 자격 증명을 사용할 수 있는 블록을 생성한다.
                        // usernamePassword()
                        //  - 자격 증명 중 사용자 이름과 비밀번호를 가져온다.
                        //  - credentialsId는 자격 증명을 식별할 수 있는 식별자를 작성한다.
                        //  - usernameVariable은 자격 증명에서 가져온 사용자 이름을 저장하는 환경 변수의 이름을 작성한다.
                        //  - passwordVariable은 자격 증명에서 가져온 비밀번호를 저장하는 환경 변수의 이름을 작성한다.
                        withCredentials([usernamePassword(
                            credentialsId: DOCKER_CREDENTIALS_ID,
                            usernameVariable: 'DOCKER_USERNAME',
                            passwordVariable: 'DOCKER_PASSWORD' 
                        )]) {
                            sh 'echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin'
                        }

                        // 파이프라인 단계에서 환경 변수를 설정하는 역할을 한다.
                        withEnv(["DOCKER_IMAGE_VERSION=${dockerImageVersion}"]) {
                            sh 'docker -v'
                            sh 'echo $DOCKER_IMAGE_NAME:$DOCKER_IMAGE_VERSION'
                            // sh 'docker images university-api'
                            sh 'docker build --no-cache -t $DOCKER_IMAGE_NAME:$DOCKER_IMAGE_VERSION ./'
                            sh 'docker image inspect $DOCKER_IMAGE_NAME:$DOCKER_IMAGE_VERSION'
                            sh 'docker push $DOCKER_IMAGE_NAME:$DOCKER_IMAGE_VERSION'
                        }
                    }
                }
            }
        }

        stage('Trigger university-k8s-manifests') {
            steps {
                script {
                    def dockerIMageVersion = "${env.BUILD_NUMBER}"

                    withEnv(['DOCKER_IMAGE_VERSION=${dockerImageVersion}']) {
                        // 다른 잡을 빌드하면서 파라미터 전달달
                        build job: 'university-k8s-manifests',
                            parameters: [
                                string(name: 'DOCKER_IMAGE_VERSION', value: "${DOCKER_IMAGE_VERSION}")
                        ],
                        wait: true // 하위 잡이 끝날 때까지 기다림
                    }
                    build job: 'university-k8s-manifests'
                }
            }
        }
    }
}

    post {
        always {
            withCredentials([string(
                credentialsId: 'discord-webhook', 
                variable: 'DISCORD_WEBHOOK_URL'
            )]) {
                discordSend description: """
                제목 : ${currentBuild.displayName}
                결과 : ${currentBuild.result}
                실행 시간 : ${currentBuild.duration / 1000}s
                """,
                result: currentBuild.currentResult,
                title: "${env.JOB_NAME} : ${currentBuild.displayName}", 
                webhookURL: "${DISCORD_WEBHOOK_URL}"
            }
        }
        // success {
        //     withCredentials([string(
        //         credentialsId: 'discord-webhook', 
        //         variable: 'DISCORD_WEBHOOK_URL'
        //     )]) {
        //         discordSend description: """
        //         제목 : ${currentBuild.displayName}
        //         결과 : ${currentBuild.result}
        //         실행 시간 : ${currentBuild.duration / 1000}s
        //         """,
        //         result: currentBuild.currentResult,
        //         title: "${env.JOB_NAME} : ${currentBuild.displayName} 성공", 
        //         webhookURL: "${DISCORD_WEBHOOK_URL}"
        //     }
        // }
        // failure {
        //     withCredentials([string(
        //         credentialsId: 'discord-webhook', 
        //         variable: 'DISCORD_WEBHOOK_URL'
        //     )]) {
        //         discordSend description: """
        //         제목 : ${currentBuild.displayName}
        //         결과 : ${currentBuild.result}
        //         실행 시간 : ${currentBuild.duration / 1000}s
        //         """,
        //         result: currentBuild.currentResult,
        //         title: "${env.JOB_NAME} : ${currentBuild.displayName} 실패", 
        //         webhookURL: "${DISDISCORD_WEBHOOK_URL}
        // }
    }
