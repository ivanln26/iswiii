def app

pipeline {
    agent any

    tools {
        maven 'M3'
    }

    stages {
        stage('Clone repository') {
            steps {
                git branch: 'main', url: 'https://github.com/ivanln26/spring-boot.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn -Dmaven.test.failure.ignore=true clean package'
            }

            post {
                success {
                    junit '**/target/surefire-reports/TEST-*.xml'
                    archiveArtifacts 'target/*.jar'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    app = docker.build('ivanln26/spring-boot:latest')
                }
            }
        }

        stage('Publish Docker Image') {
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', 'docker-hub-credentials') {
                        app.push("${env.BUILD_NUMBER}")
                        app.push("latest")
                    }
                }
            }
        }

        stage('Publish Heroku') {
            steps {
                script {
                    sh('heroku container:push web --app=secret-stream-12271')
                    sh('heroku container:release web --app=secret-stream-12271')
                }
            }
        }
    }
}
