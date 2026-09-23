pipeline {

    agent any

    parameters {
        choice(
            name: 'CUCUMBER_TAG',
            choices: [
                '@authentication',
                '@assemblyFetch'
            ],
            description: 'Select the Cucumber scenario to run'
        )
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Check Simulator') {
            steps {
                bat '''
                    curl --fail http://localhost:8081/actuator/health
                '''
            }
        }

        stage('Run Cucumber Tests') {
            steps {
                bat '''
                    call mvnw.cmd clean test "-Dcucumber.filter.tags=%CUCUMBER_TAG%"
                '''
            }
        }
    }

    post {

        always {
            junit(
                allowEmptyResults: true,
                testResults: 'target/surefire-reports/*.xml'
            )

            archiveArtifacts(
                allowEmptyArchive: true,
                artifacts: 'target/cucumber-report.*'
            )
        }
    }
}